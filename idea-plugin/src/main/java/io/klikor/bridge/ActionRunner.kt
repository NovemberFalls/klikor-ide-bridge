/*
 * Klikor IDE Bridge -- ground-up rewrite on stable public IntelliJ Platform API.
 * Copyright Bits, LLC. Use of this source code is governed by the Apache 2.0 license.
 */
package io.klikor.bridge

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.IdeFocusManager

/**
 * Executes an IDE action (or a named Run/Debug configuration) triggered by [BridgeServer].
 * Everything here runs on the EDT and uses public IntelliJ Platform API only.
 */
object ActionRunner {

    private val LOG = Logger.getInstance(ActionRunner::class.java)

    // Custom action place -- avoids depending on ActionPlaces for a single constant string.
    private const val PLACE = "KlikorBridge"

    fun run(actionId: String, runConfigName: String?) {
        ApplicationManager.getApplication().invokeLater {
            try {
                dispatch(actionId, runConfigName)
            } catch (t: Throwable) {
                LOG.warn("Klikor IDE Bridge: error executing action '$actionId'", t)
            }
        }
    }

    private fun dispatch(actionId: String, runConfigName: String?) {
        val settings = BridgeSettings.getInstance().state
        if (settings.focusOnly && !ApplicationManager.getApplication().isActive) {
            LOG.info("Klikor IDE Bridge: skipping '$actionId' -- IDE window not focused (focusOnly enabled)")
            return
        }

        val project = resolveProject()

        if ((actionId == "Run" || actionId == "Debug") && !runConfigName.isNullOrBlank()) {
            if (project == null) {
                LOG.info("Klikor IDE Bridge: no open project available for Run/Debug '$runConfigName'")
                return
            }
            runConfiguration(project, runConfigName, runMode = actionId == "Run")
            return
        }

        val action = ActionManager.getInstance().getAction(actionId)
        if (action == null) {
            LOG.info("Klikor IDE Bridge: unknown action id '$actionId' -- ignoring")
            return
        }

        performAction(action, project)
    }

    /** Prefers the project behind the IDE's currently focused frame; else the first open project. */
    private fun resolveProject(): Project? {
        val focused = IdeFocusManager.getGlobalInstance().lastFocusedFrame?.project
        if (focused != null && !focused.isDisposed) return focused
        return ProjectManager.getInstance().openProjects.firstOrNull { !it.isDisposed }
    }

    private fun runConfiguration(project: Project, name: String, runMode: Boolean) {
        val settings = RunManager.getInstance(project).allSettings.firstOrNull { it.name == name }
        if (settings == null) {
            LOG.info("Klikor IDE Bridge: no run configuration named '$name'")
            return
        }
        val executor = if (runMode) {
            DefaultRunExecutor.getRunExecutorInstance()
        } else {
            DefaultDebugExecutor.getDebugExecutorInstance()
        }
        ProgramRunnerUtil.executeConfiguration(settings, executor)
    }

    private fun performAction(action: AnAction, project: Project?) {
        val focusOwner = IdeFocusManager.getGlobalInstance().focusOwner
        val dataContext: DataContext = when {
            focusOwner != null -> DataManager.getInstance().getDataContext(focusOwner)
            project != null -> SimpleDataContext.getProjectContext(project)
            else -> DataContext.EMPTY_CONTEXT
        }

        ActionUtil.invokeAction(action, dataContext, PLACE, null, null)
    }
}
