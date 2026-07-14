/*
 * Klikor IDE Bridge -- ground-up rewrite on stable public IntelliJ Platform API.
 * Copyright Bits, LLC. Use of this source code is governed by the Apache 2.0 license.
 */
package io.klikor.bridge

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Application-level, persisted settings for the Klikor IDE Bridge local HTTP action server.
 */
@Service(Service.Level.APP)
@State(name = "KlikorBridgeSettings", storages = [Storage("klikorBridge.xml")])
class BridgeSettings : PersistentStateComponent<BridgeSettings.State> {

    class State {
        var enabled: Boolean = true
        // 0 = auto-scan 21420..21430 for the first free port; a fixed value pins that exact port.
        var port: Int = 0
        var password: String = ""
        var focusOnly: Boolean = false
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    companion object {
        @JvmStatic
        fun getInstance(): BridgeSettings =
            ApplicationManager.getApplication().getService(BridgeSettings::class.java)
    }
}
