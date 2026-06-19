package com.dexforge.intellij.ui

import com.dexforge.intellij.services.DexForgeService
import com.dexforge.intellij.services.LspClientService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JScrollPane
import javax.swing.BoxLayout
import com.intellij.openapi.ui.Messages

class DexForgeToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val lspClient = LspClientService.getInstance(project)
        val dexForgeService = DexForgeService.getInstance(project)

        val contentFactory = ContentFactory.getInstance()

        // Main panel
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)

        // Status label
        val statusButton = JButton("Start DexForge Daemon")
        statusButton.addActionListener {
            if (lspClient.isConnected()) {
                Messages.showInfoMessage(project, "DexForge daemon is running", "DexForge")
            } else {
                val success = lspClient.connect()
                if (success) {
                    statusButton.text = "DexForge Daemon Running"
                    statusButton.isEnabled = false
                } else {
                    Messages.showErrorDialog(project, "Failed to start daemon", "DexForge")
                }
            }
        }
        mainPanel.add(statusButton)

        // Decompile button
        val decompileButton = JButton("Decompile APK/DEX/JAR")
        decompileButton.addActionListener {
            // Trigger decompile action
        }
        mainPanel.add(decompileButton)

        // Device Explorer button
        val deviceButton = JButton("Open Device Explorer")
        deviceButton.addActionListener {
            // Trigger device explorer action
        }
        mainPanel.add(deviceButton)

        // Log area
        val logArea = JTextArea(10, 30)
        logArea.isEditable = false
        mainPanel.add(JScrollPane(logArea))

        val content = contentFactory.createContent(mainPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
