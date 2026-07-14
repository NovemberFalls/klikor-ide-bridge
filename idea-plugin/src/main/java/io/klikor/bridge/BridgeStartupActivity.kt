/*
 * Klikor IDE Bridge -- ground-up rewrite on stable public IntelliJ Platform API.
 * Copyright Bits, LLC. Use of this source code is governed by the Apache 2.0 license.
 */
package io.klikor.bridge

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Starts the bridge server when a project opens. BridgeServer.start() is idempotent, so
 * multiple open projects are harmless.
 *
 * This single hook covers every lifecycle case without any listener interfaces:
 *  - normal IDE launch (project reopen fires it),
 *  - dynamic plugin install/enable while projects are open (the platform runs a newly
 *    loaded plugin's startup activities against already-open projects),
 *  - disable/uninstall stops the server via BridgeServer.dispose() (app service disposal).
 *
 * Deliberately NOT AppLifecycleListener/DynamicPluginListener: those are Java interfaces
 * with default methods, and the Kotlin compiler emits synthetic super-delegating bridges
 * for defaults that exist in the compile-time platform (2026.2) but not in the oldest
 * supported IDE (2024.3), which the Plugin Verifier flags as NoSuchMethodError risks.
 * The one behavior tradeoff: with no project open (welcome screen only), the server is
 * not yet running -- acceptable, since IDE actions Klikor triggers need a project anyway.
 */
class BridgeStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        BridgeServer.getInstance().start()
    }
}
