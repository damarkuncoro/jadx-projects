package dexforge.plugins.input.apks

import dexforge.zip.ZipReader
import java.io.File
import java.io.InputStream

object ApksUtils {
	fun isValidApks(file: File): Boolean = file.name.endsWith(".apks")

	fun extractApkEntries(file: File, zipReader: ZipReader, consumer: (String, InputStream) -> Unit) {
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
