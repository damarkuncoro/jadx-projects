package com.dexforge.intellij.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class DexForgeService(private val project: Project) {
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return
        // Initialize DexForge services
        isInitialized = true
    }

    fun isInitialized(): Boolean = isInitialized

    companion object {
        fun getInstance(project: Project): DexForgeService =
            project.getService(DexForgeService::class.java)
    }
}
