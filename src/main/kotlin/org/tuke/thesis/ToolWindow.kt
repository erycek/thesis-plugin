package org.tuke.thesis

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import org.tuke.thesis.utils.traverseProjectFiles
import java.awt.BorderLayout
import javax.swing.*


internal class ToolWindow : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = SecurityScannerToolWindowContent(toolWindow)
        val content = toolWindow.contentManager.factory.createContent(
            toolWindowContent.contentPanel, "Actions", false
        )
        toolWindow.contentManager.addContent(content)
        ConsoleManager.initialize(toolWindow, project)
        ConsoleManager.log("Scanning project: ${project.name}", ConsoleViewContentType.NORMAL_OUTPUT)
    }

    private class SecurityScannerToolWindowContent(toolWindow: ToolWindow) {
        val contentPanel: JPanel = JPanel()

        init {
            contentPanel.layout = BorderLayout(0, 20)
            contentPanel.border = BorderFactory.createEmptyBorder(40, 0, 0, 0)
            contentPanel.add(createControlsPanel(toolWindow), BorderLayout.PAGE_START)
        }

        private fun createControlsPanel(toolWindow: ToolWindow): JPanel {
            val controlsPanel = JPanel()
            val analyzeCodeButton = JButton("Analyze code")
            analyzeCodeButton.addActionListener {
                traverseProjectFiles(toolWindow.project)
            }
            controlsPanel.add(analyzeCodeButton)
            return controlsPanel
        }
    }
}