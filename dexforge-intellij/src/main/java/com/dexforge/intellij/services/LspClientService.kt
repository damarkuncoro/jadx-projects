package com.dexforge.intellij.services

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class LspClientService(private val project: Project) {
    private val logger = Logger.getInstance(LspClientService::class.java)
    private var socket: Socket? = null
    private var writer: OutputStreamWriter? = null
    private var reader: BufferedReader? = null
    private var isConnected = false
    private var requestId = 0

    fun connect(port: Int = 8080): Boolean {
        if (isConnected) return true

        return try {
            socket = Socket("localhost", port)
            writer = OutputStreamWriter(socket!!.getOutputStream())
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            isConnected = true
            logger.info("Connected to DexForge LSP daemon on port $port")
            true
        } catch (e: Exception) {
            logger.warn("Failed to connect to DexForge LSP daemon: ${e.message}")
            false
        }
    }

    fun disconnect() {
        try {
            writer?.close()
            reader?.close()
            socket?.close()
        } catch (e: Exception) {
            logger.warn("Error disconnecting from LSP daemon: ${e.message}")
        } finally {
            isConnected = false
            socket = null
            writer = null
            reader = null
        }
    }

    fun sendRequest(method: String, params: JsonObject? = null): JsonObject? {
        if (!isConnected && !connect()) {
            return null
        }

        return try {
            requestId++
            val request = JsonObject().apply {
                addProperty("jsonrpc", "2.0")
                addProperty("id", requestId)
                addProperty("method", method)
                if (params != null) add("params", params)
            }

            writer?.write(request.toString() + "\n")
            writer?.flush()

            val responseStr = reader?.readLine() ?: return null
            val response = JsonParser.parseString(responseStr).asJsonObject
            if (response.has("result")) {
                response.getAsJsonObject("result")
            } else if (response.has("error")) {
                logger.error("LSP error: ${response.getAsJsonObject("error")}")
                null
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Error sending LSP request: ${e.message}", e)
            isConnected = false
            null
        }
    }

    fun loadFile(filePath: String): JsonObject? {
        val params = JsonObject().apply {
            addProperty("path", filePath)
        }
        return sendRequest("dexforge/load", params)
    }

    fun decompileClass(className: String): JsonObject? {
        val params = JsonObject().apply {
            addProperty("className", className)
        }
        return sendRequest("decompile", params)
    }

    fun listClasses(): JsonObject? {
        return sendRequest("list-classes")
    }

    fun isConnected(): Boolean = isConnected
}
