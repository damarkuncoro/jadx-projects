package dexforge.cli.plugins;

import java.nio.file.Path;

import dexforge.commons.app.DexforgeCommonFiles;
import dexforge.commons.app.DexforgeTempFiles;

import jadx.core.plugins.files.IJadxFilesGetter;

public class DexforgeFilesGetter implements IJadxFilesGetter {

	public static final DexforgeFilesGetter INSTANCE = new DexforgeFilesGetter();

	@Override
	public Path getConfigDir() {
		return DexforgeCommonFiles.getConfigDir();
	}

	@Override
	public Path getCacheDir() {
		return DexforgeCommonFiles.getCacheDir();
	}

	@Override
	public Path getTempDir() {
		return DexforgeTempFiles.getTempRootDir();
	}

	private DexforgeFilesGetter() {
		// singleton
	}
}
