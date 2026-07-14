/*
 * Klikor IDE Bridge -- ground-up rewrite on stable public IntelliJ Platform API.
 * Copyright Bits, LLC. Use of this source code is governed by the Apache 2.0 license.
 */
package io.klikor.bridge

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.io.IOException
import java.net.BindException
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

/**
 * Owns the local, loopback-only HTTP server that lets the Klikor desktop app trigger IDE
 * actions. One server per IDE instance (settings are application-level).
 *
 * Frozen wire contract -- the Klikor desktop app depends on this exactly:
 *  - `GET /api/action/<ActionID>` (ActionID is a URL-encoded path segment).
 *  - Optional `?name=<url-encoded RunConfigName>`, meaningful only for ActionID "Run"/"Debug".
 *  - Optional password, compared against the RAW `Authorization` header value. Empty configured
 *    password = no auth check.
 *  - 200 = request accepted (fire-and-forget; this includes unknown action ids -- the desktop
 *    probes liveness with the nonexistent action id `klikor.ping` and requires 200 for it).
 *  - 403 = wrong password (only when a password is configured).
 *  - 405 = non-GET.
 *  - Binds 127.0.0.1 ONLY, first free port in 21420..21430. If all are taken, stays stopped.
 */
@Service(Service.Level.APP)
class BridgeServer : Disposable {

    @Volatile
    private var server: HttpServer? = null

    /** The port currently bound, or null if the server is not running. */
    val boundPort: Int?
        get() = server?.address?.port

    @Synchronized
    fun start() {
        if (server != null) return

        val state = BridgeSettings.getInstance().state
        if (!state.enabled) {
            LOG.info("Klikor IDE Bridge is disabled in settings; not starting server.")
            return
        }

        val candidatePorts = if (state.port > 0) listOf(state.port) else (PORT_RANGE_START..PORT_RANGE_END).toList()

        for (port in candidatePorts) {
            try {
                val httpServer = HttpServer.create(InetSocketAddress("127.0.0.1", port), 0)
                httpServer.createContext(ACTION_PATH_PREFIX, ActionHttpHandler())
                httpServer.executor = Executors.newSingleThreadExecutor { runnable ->
                    Thread(runnable, "klikor-bridge-http").apply { isDaemon = true }
                }
                httpServer.start()
                server = httpServer
                LOG.info("Klikor IDE Bridge listening on 127.0.0.1:$port")
                return
            } catch (e: BindException) {
                // Port already in use -- try the next one in range.
            } catch (e: IOException) {
                LOG.warn("Klikor IDE Bridge failed to bind 127.0.0.1:$port", e)
            }
        }

        LOG.warn(
            "Klikor IDE Bridge could not bind any port in $PORT_RANGE_START..$PORT_RANGE_END; " +
                "server not started."
        )
    }

    @Synchronized
    fun stop() {
        server?.stop(0)
        server = null
    }

    @Synchronized
    fun restart() {
        stop()
        start()
    }

    override fun dispose() {
        stop()
    }

    companion object {
        private val LOG = Logger.getInstance(BridgeServer::class.java)
        private const val ACTION_PATH_PREFIX = "/api/action/"
        private const val PORT_RANGE_START = 21420
        private const val PORT_RANGE_END = 21430

        @JvmStatic
        fun getInstance(): BridgeServer =
            ApplicationManager.getApplication().getService(BridgeServer::class.java)
    }

    private class ActionHttpHandler : HttpHandler {
        override fun handle(exchange: HttpExchange) {
            try {
                if (exchange.requestMethod != "GET") {
                    exchange.sendResponseHeaders(405, -1)
                    exchange.close()
                    return
                }

                val password = BridgeSettings.getInstance().state.password
                if (password.isNotEmpty()) {
                    val authHeader = exchange.requestHeaders.getFirst("Authorization") ?: ""
                    if (authHeader != password) {
                        exchange.sendResponseHeaders(403, -1)
                        exchange.close()
                        return
                    }
                }

                val rawPath = exchange.requestURI.rawPath ?: ""
                val actionId = if (rawPath.length > ACTION_PATH_PREFIX.length) {
                    decode(rawPath.substring(ACTION_PATH_PREFIX.length))
                } else {
                    ""
                }
                val runConfigName = parseNameParam(exchange.requestURI.rawQuery)

                // 200 is sent immediately; execution below is fire-and-forget and must never
                // affect the response already sent to the client.
                respondOk(exchange)

                if (actionId.isNotEmpty()) {
                    try {
                        ActionRunner.run(actionId, runConfigName)
                    } catch (t: Throwable) {
                        LOG.warn("Klikor IDE Bridge: error scheduling action '$actionId'", t)
                    }
                }
            } catch (t: Throwable) {
                LOG.warn("Klikor IDE Bridge: error handling request", t)
                try {
                    exchange.close()
                } catch (ignored: IOException) {
                }
            }
        }

        private fun respondOk(exchange: HttpExchange) {
            val bytes = "ok".toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.set("Content-Type", "text/plain")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }

        private fun parseNameParam(rawQuery: String?): String? {
            if (rawQuery.isNullOrEmpty()) return null
            for (pair in rawQuery.split("&")) {
                val idx = pair.indexOf('=')
                if (idx < 0) continue
                if (pair.substring(0, idx) == "name") {
                    return decode(pair.substring(idx + 1))
                }
            }
            return null
        }

        private fun decode(value: String): String = try {
            URLDecoder.decode(value, StandardCharsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            value
        }
    }
}
