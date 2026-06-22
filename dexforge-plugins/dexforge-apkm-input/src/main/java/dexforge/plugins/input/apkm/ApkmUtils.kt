package dexforge.plugins.input.apkm

import dexforge.zip.ZipReader
import jadx.core.utils.GsonUtils.buildGson
import jadx.core.utils.files.FileUtils
import java.io.File
import java.io.InputStreamReader

object ApkmUtils {
	fun getManifest(file: File, zipReader: ZipReader): ApkmManifest? {
		if (!FileUtils.isZipFile(file)) return null
		try {
			zipReader.open(file).use { zip ->
				val manifestEntry = zip.searchEntry("info.json") ?: return null
				return InputStreamReader(manifestEntry.inputStream).use {
					buildGson().fromJson(it, ApkmManifest::class.java)
				}
			}
		} catch (e: Exception) {
			return null
		}
	}

	fun isSupported(manifest: ApkmManifest): Boolean = manifest.apkmVersion != -1

	fun isValidApkm(file: File, zipReader: ZipReader): Boolean {
		if (!file.name.endsWith(".apkm")) return false
		val manifest = getManifest(file, zipReader) ?: return false
		return isSupported(manifest)
	}

	fun extractApkEntries(file: File, zipReader: ZipReader, consumer: (String, java.io.InputStream) -> Unit) {
		zipReader.visitEntries<Any>(file) { entry ->
			if (entry.name.endsWith(".apk")) {
				entry.inputStream.use {
					consumer(entry.name, it)
				}
			}
			null
		}
	}
}
