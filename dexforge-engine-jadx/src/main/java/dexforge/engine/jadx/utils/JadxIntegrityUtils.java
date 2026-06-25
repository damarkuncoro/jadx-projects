package dexforge.engine.jadx.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jadx.api.JadxDecompiler;

/**
 * Internal JADX utility for integrity verification.
 * This class is allowed to have JADX imports as it resides in the engine-jadx module.
 */
public final class JadxIntegrityUtils {
	private JadxIntegrityUtils() {
	}

	public static Map<String, String> calculateFingerprint(JadxDecompiler decompiler) {
		Map<String, String> hashes = new HashMap<>();
		List<File> inputFiles = decompiler.getArgs().getInputFiles();
		for (File file : inputFiles) {
			if (file.isFile()) {
				hashes.put(file.getName(), calculateSHA256(file));
			}
		}
		return hashes;
	}

	private static String calculateSHA256(File file) {
		try (InputStream is = new FileInputStream(file)) {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] buffer = new byte[8192];
			int read;
			while ((read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			byte[] hash = digest.digest();
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception e) {
			return "error-" + e.getMessage();
		}
	}
}
