package com.jetbrains.ide.streamdeck.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil


@State(name = "ActionServerSettings", storages = [Storage("StreamDeckActionServerSettings.xml")])
internal data class ActionServerSettings(
  var defaultPort: Int = 21420, // Default remote service listen port
  var password: String = "", // Optional password
  var enable: Boolean = true,// Enable action service or not
  // Klikor default: false (was true upstream). Klikor is a desktop app whose buttons should be able to
  // trigger IDE actions without stealing focus from the IDE window first; the setting itself is unchanged
  // and users who want the stricter upstream behavior can still re-enable this checkbox in Settings.
  var focusOnly: Boolean = false,// Perform actions only when IDE window is focused
  var enableRemote: Boolean = false,// Enable action service remote access or not
) : PersistentStateComponent<ActionServerSettings> {

  companion object {
    @JvmStatic
    fun getInstance(): ActionServerSettings = ApplicationManager.getApplication().getService(
      ActionServerSettings::class.java)
  }

  override fun getState(): ActionServerSettings = this

  override fun loadState(state: ActionServerSettings) {
    XmlSerializerUtil.copyBean(state, this)
  }
}
