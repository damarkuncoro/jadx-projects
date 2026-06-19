package com.dexforge.intellij

import com.dexforge.intellij.services.DexForgeService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class DexForgePlugin : ProjectActivity {
    override suspend fun execute(project: Project) {
        val service = DexForgeService.getInstance(project)
        service.initialize()
    }
}
