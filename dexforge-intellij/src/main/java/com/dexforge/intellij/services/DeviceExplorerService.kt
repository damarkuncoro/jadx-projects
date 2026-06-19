package com.dexforge.intellij.services

import com.intellij.openapi.project.Project
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class DeviceExplorerService(private val project: Project) {
    private val logger = com.intellij.openapi.diagnostic.Logger.getInstance(DeviceExplorerService::class.java)
    private val lspClient = LspClientService.getInstance(project)

    fun listDevices(): List<DeviceInfo> {
        val response = lspClient.sendRequest("dexforge/device-list")
        val devices = mutableListOf<DeviceInfo>()

        response?.getAsJsonArray("devices")?.forEach { element ->
            val obj = element.asJsonObject
            devices.add(
                DeviceInfo(
                    serial = obj.get("serial")?.asString ?: "",
                    model = obj.get("model")?.asString ?: "Unknown",
                    androidVersion = obj.get("androidVersion")?.asString ?: "Unknown",
                    isRooted = obj.get("isRooted")?.asBoolean ?: false
                )
            )
        }

        return devices
    }

    fun listPackages(deviceSerial: String, userId: Int = 0): List<PackageInfo> {
        val params = JsonObject().apply {
            addProperty("deviceSerial", deviceSerial)
            addProperty("userId", userId)
        }
        val response = lspClient.sendRequest("dexforge/device-packages", params)
        val packages = mutableListOf<PackageInfo>()

        response?.getAsJsonArray("packages")?.forEach { element ->
            val obj = element.asJsonObject
            packages.add(
                PackageInfo(
                    packageName = obj.get("packageName")?.asString ?: "",
                    apkPath = obj.get("apkPath")?.asString ?: "",
                    isSplit = obj.get("isSplit")?.asBoolean ?: false,
                    splitType = obj.get("splitType")?.asString
                )
            )
        }

        return packages
    }

    fun pullAndDecompile(deviceSerial: String, packageName: String, outputDir: String, userId: Int = 0): DecompileResult {
        val params = JsonObject().apply {
            addProperty("deviceSerial", deviceSerial)
            addProperty("packageName", packageName)
            addProperty("outputDir", outputDir)
            addProperty("userId", userId)
        }
        val response = lspClient.sendRequest("dexforge/device-pull-decompile", params)

        return if (response != null) {
            DecompileResult(
                success = response.get("success")?.asBoolean ?: false,
                outputPath = response.get("outputPath")?.asString,
                classesCount = response.get("classesCount")?.asInt ?: 0,
                error = response.get("error")?.asString
            )
        } else {
            DecompileResult(success = false, error = "Failed to communicate with daemon")
        }
    }

    data class DeviceInfo(
        val serial: String,
        val model: String,
        val androidVersion: String,
        val isRooted: Boolean
    )

    data class PackageInfo(
        val packageName: String,
        val apkPath: String,
        val isSplit: Boolean,
        val splitType: String?
    )

    data class DecompileResult(
        val success: Boolean,
        val outputPath: String? = null,
        val classesCount: Int = 0,
        val error: String? = null
    )
}
