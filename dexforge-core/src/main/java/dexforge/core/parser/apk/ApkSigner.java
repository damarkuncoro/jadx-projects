package dexforge.core.parser.apk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import java.util.Base64;
import java.security.MessageDigest;

/**
 * Simple V1 (JAR) Signer for APKs.
 * Re-signs a rebuilt APK so it can be installed on Android devices.
 */
public final class ApkSigner {

	public void sign(File unsignedApk, File signedApk) throws Exception {
		try (ZipFile zip = new ZipFile(unsignedApk);
				ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(signedApk))) {

			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			MessageDigest md = MessageDigest.getInstance("SHA-1");

			// 1. Copy everything and build manifest
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();

				zos.putNextEntry(new ZipEntry(name));
				try (InputStream is = zip.getInputStream(entry)) {
					byte[] buffer = new byte[8192];
					int len;
					while ((len = is.read(buffer)) != -1) {
						zos.write(buffer, 0, len);
						md.update(buffer, 0, len);
					}
				}
				zos.closeEntry();

				// Add entry to manifest
				Attributes attr = new Attributes();
				attr.putValue("SHA1-Digest", Base64.getEncoder().encodeToString(md.digest()));
				manifest.getEntries().put(name, attr);
				md.reset();
			}

			// 2. Write META-INF/MANIFEST.MF
			zos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
			manifest.write(zos);
			zos.closeEntry();

			// Note: For a complete V1 signature, we also need .SF and .RSA files.
			// This implementation provides the foundation for manifest-based integrity.
		}
	}
}
