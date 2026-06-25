package dexforge.core.parser.apk;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.nio.file.Files;

/**
 * Foundation for rebuilding an APK from modified components.
 */
public final class ApkRebuilder {

	public void rebuild(File outputApk, File sourceDir) throws Exception {
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputApk))) {
			Files.walk(sourceDir.toPath()).forEach(path -> {
				if (Files.isRegularFile(path)) {
					try {
						String name = sourceDir.toPath().relativize(path).toString();
						zos.putNextEntry(new ZipEntry(name));
						zos.write(Files.readAllBytes(path));
						zos.closeEntry();
					} catch (Exception e) {
						// Handle error
					}
				}
			});
		}
	}
}
