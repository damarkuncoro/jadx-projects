package com.dexforge.intellij.actions

import com.dexforge.intellij.services.LspClientService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class StartDaemonAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val lspClient = LspClientService.getInstance(project)

        if (lspClient.isConnected()) {
            Messages.showInfoMessage(
                project,
                "DexForge LSP daemon is already running",
                "DexForge"
            )
            return
        }

        val success = lspClient.connect()
        if (success) {
            Messages.showInfoMessage(
                project,
                "DexForge LSP daemon started successfully",
                "DexForge"
            )
        } else {
            Messages.showErrorDialog(
                project,
                "Failed to start DexForge LSP daemon. Make sure 'dexforge' is in PATH and run 'dexforge lsp' manually.",
                "DexForge - Error"
            )
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
