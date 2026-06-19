package dexforge.plugins.input.apks

import dexforge.api.plugins.input.ICodeLoader
import dexforge.api.plugins.input.JadxCodeInput
import dexforge.api.plugins.utils.CommonFileUtils
import dexforge.plugins.input.dex.DexInputPlugin
import dexforge.zip.ZipReader
import java.io.File
import java.nio.file.Path

class ApksCustomCodeInput(
	private val dexInputPlugin: DexInputPlugin,
	private val zipReader: ZipReader,
) : JadxCodeInput {
	override fun loadFiles(input: List<Path>): ICodeLoader {
		val apkFiles = mutableListOf<File>()
		for (file in input.map { it.toFile() }) {
			if (!ApksUtils.isValidApks(file)) continue

			ApksUtils.extractApkEntries(file, zipReader) { _, inputStream ->
				val tmpFile = CommonFileUtils.saveToTempFile(inputStream, ".apk").toFile()
				apkFiles.add(tmpFile)
			}
		}

		val codeLoader = dexInputPlugin.loadFiles(apkFiles.map { it.toPath() })

		apkFiles.forEach { CommonFileUtils.safeDeleteFile(it) }

		return codeLoader
	}
}
