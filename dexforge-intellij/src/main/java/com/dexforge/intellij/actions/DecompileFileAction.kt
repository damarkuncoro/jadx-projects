package com.dexforge.intellij.actions

import com.dexforge.intellij.services.DexForgeService
import com.dexforge.intellij.services.LspClientService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.google.gson.JsonObject

class DecompileFileAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val lspClient = LspClientService.getInstance(project)

        val descriptor = FileChooserDescriptor(
            true, false, false, false, false, false
        ).withTitle("Select APK/DEX/JAR File")
         .withDescription("Choose an Android file to decompile")
         .withFileFilter { file ->
             file.extension in listOf("apk", "dex", "jar", "aar", "zip")
         }

        FileChooser.chooseFile(descriptor, null, project) { file ->
            file?.let { decompileFile(project, lspClient, it) }
        }
    }

    private fun decompileFile(project: Project, lspClient: LspClientService, file: VirtualFile) {
        val response = lspClient.loadFile(file.path)
        if (response != null) {
            val classesCount = response.get("classesCount")?.asInt ?: 0
            com.intellij.openapi.ui.Messages.showInfoMessage(
                project,
                "Successfully loaded $classesCount classes from ${file.name}",
                "DexForge - Decompile Complete"
            )
        } else {
            com.intellij.openapi.ui.Messages.showErrorDialog(
                project,
                "Failed to load file: ${file.name}",
                "DexForge - Error"
            )
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
