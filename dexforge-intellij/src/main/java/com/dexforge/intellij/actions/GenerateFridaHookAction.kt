package com.dexforge.intellij.actions

import com.dexforge.intellij.services.LspClientService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.google.gson.JsonObject

class GenerateFridaHookAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val lspClient = LspClientService.getInstance(project)

        val className = Messages.showInputDialog(
            project,
            "Enter class name:",
            "Generate Frida Hook",
            Messages.getQuestionIcon()
        ) ?: return

        val methodName = Messages.showInputDialog(
            project,
            "Enter method name:",
            "Generate Frida Hook",
            Messages.getQuestionIcon()
        ) ?: return

        val params = JsonObject().apply {
            addProperty("className", className)
            addProperty("methodName", methodName)
        }

        val response = lspClient.sendRequest("dexforge/generateFridaHook", params)
        if (response != null && response.has("script")) {
            val script = response.get("script").asString
            Messages.showInfoMessage(
                project,
                "Frida hook generated:\n\n$script",
                "DexForge - Frida Hook"
            )
        } else {
            Messages.showErrorDialog(
                project,
                "Failed to generate Frida hook",
                "DexForge - Error"
            )
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
