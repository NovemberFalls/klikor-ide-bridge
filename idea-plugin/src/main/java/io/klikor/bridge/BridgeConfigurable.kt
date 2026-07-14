/*
 * Klikor IDE Bridge -- ground-up rewrite on stable public IntelliJ Platform API.
 * Copyright Bits, LLC. Use of this source code is governed by the Apache 2.0 license.
 */
package io.klikor.bridge

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JLabel

/**
 * Settings -> Tools -> Klikor IDE Bridge. Deliberately minimal: no action browser, no keymap
 * UI (both dropped in v2.0.0). Applying settings restarts the local HTTP server so changes take
 * effect immediately, without an IDE restart.
 */
class BridgeConfigurable :
    BoundSearchableConfigurable("Klikor IDE Bridge", CONFIGURABLE_ID, CONFIGURABLE_ID) {

    private var statusLabel: JLabel? = null

    override fun createPanel(): DialogPanel {
        val settings = BridgeSettings.getInstance().state

        return panel {
            row("") {
                checkBox("Enabled").bindSelected(settings::enabled)
            }
            row("Port (0 = automatic, scans 21420-21430):") {
                intTextField(0..65535).bindIntText(settings::port)
            }
            row("Password (optional):") {
                passwordField().bindText(settings::password)
            }
            row("") {
                checkBox("Perform actions only when IDE window is focused").bindSelected(settings::focusOnly)
            }
            row("Status:") {
                statusLabel = label(statusText()).component
            }
        }
    }

    override fun apply() {
        super.apply()
        BridgeServer.getInstance().restart()
        refreshStatus()
    }

    override fun reset() {
        super.reset()
        refreshStatus()
    }

    private fun refreshStatus() {
        statusLabel?.text = statusText()
    }

    private fun statusText(): String {
        val port = BridgeServer.getInstance().boundPort
        return if (port != null) "Running on 127.0.0.1:$port" else "Stopped"
    }

    companion object {
        const val CONFIGURABLE_ID = "io.klikor.bridge.settings"
    }
}
