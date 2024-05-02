package org.tuke.thesis

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow

object ConsoleManager {
    private var consoleView: ConsoleView? = null

    fun initialize(toolWindow: ToolWindow, project: Project) {
        if (consoleView == null) {
            consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
            val consoleContent =
                toolWindow.contentManager.factory.createContent(consoleView!!.component, "Security Scanner", false)
            toolWindow.contentManager.addContent(consoleContent)
        }
    }

    fun log(message: String, type: ConsoleViewContentType) {
        consoleView?.print(message + "\n", type)
    }
}
