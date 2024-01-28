package org.tuke.thesis

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import javax.swing.*

internal class ToolWindow : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = CalendarToolWindowContent(toolWindow)
        val content = ContentFactory.getInstance().createContent(
            toolWindowContent.contentPanel, "", false
        )
        toolWindow.contentManager.addContent(content)
    }

    private class CalendarToolWindowContent(toolWindow: ToolWindow) {
        val contentPanel: JPanel = JPanel()

        init {
            contentPanel.layout = BorderLayout(0, 20)
            contentPanel.border = BorderFactory.createEmptyBorder(40, 0, 0, 0)
            contentPanel.add(createControlsPanel(toolWindow), BorderLayout.PAGE_START)
            contentPanel.add(createCalendarPanel(), BorderLayout.CENTER)
        }

        private fun createCalendarPanel(): JPanel {
            val panel = JPanel()
            val reportLabel = JLabel("Report")
            panel.add(reportLabel)
            return panel
        }

        private fun createControlsPanel(toolWindow: ToolWindow): JPanel {
            val controlsPanel = JPanel()
            val analyzeCodeButton = JButton("Analyze code")
            controlsPanel.add(analyzeCodeButton)
            return controlsPanel
        }
    }
}