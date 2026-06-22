package dexforge.plugins.input.apkm

import dexforge.api.plugins.CustomResourcesLoader
import dexforge.api.plugins.utils.CommonFileUtils
import dexforge.zip.ZipReader
import jadx.api.ResourceFile
import jadx.api.ResourcesLoader
import java.io.File

class ApkmCustomResourcesLoader(
	private val zipReader: ZipReader,
) : CustomResourcesLoader {
	private val tmpFiles = mutableListOf<File>()

	override fun load(loader: ResourcesLoader, list: MutableList<ResourceFile>, file: File): Boolean {
		if (!ApkmUtils.isValidApkm(file, zipReader)) return false

		ApkmUtils.extractApkEntries(file, zipReader) { entryName, inputStream ->
			val tmpFile = CommonFileUtils.saveToTempFile(inputStream, ".apk").toFile()
			loader.defaultLoadFile(list, tmpFile, "$entryName/")
			tmpFiles += tmpFile
		}
		return true
	}

	override fun close() {
		tmpFiles.forEach(CommonFileUtils::safeDeleteFile)
		tmpFiles.clear()
	}
}
