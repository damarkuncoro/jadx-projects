package dexforge.commons.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DexforgeTempFiles {
	private static final String DEXFORGE_TMP_INSTANCE_PREFIX = "dexforge-instance-";

	private static final Path TEMP_ROOT_DIR = createTempRootDir();

	public static Path getTempRootDir() {
		return TEMP_ROOT_DIR;
	}

	private static Path createTempRootDir() {
		try {
			String tmpDir = System.getenv("DEXFORGE_TMP_DIR");
			if (tmpDir == null) {
				tmpDir = System.getenv("JADX_TMP_DIR");
			}
			Path dir;
			if (tmpDir != null) {
				Path customTmpRootDir = Paths.get(tmpDir);
				Files.createDirectories(customTmpRootDir);
				dir = Files.createTempDirectory(customTmpRootDir, DEXFORGE_TMP_INSTANCE_PREFIX);
			} else {
				dir = Files.createTempDirectory(DEXFORGE_TMP_INSTANCE_PREFIX);
			}
			dir.toFile().deleteOnExit();
			return dir;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create temp root directory", e);
		}
	}
}
