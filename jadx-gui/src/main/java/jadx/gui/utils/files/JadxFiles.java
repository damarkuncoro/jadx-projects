package jadx.gui.utils.files;

import java.nio.file.Path;

import dexforge.commons.app.DexforgeCommonFiles;

public class JadxFiles {

	private static final Path CONFIG_DIR = DexforgeCommonFiles.getConfigDir();
	public static final Path GUI_CONF = CONFIG_DIR.resolve("gui.json");
	public static final Path CACHES_LIST = CONFIG_DIR.resolve("caches.json");

	public static final Path CACHE_DIR = DexforgeCommonFiles.getCacheDir();
	public static final Path PROJECTS_CACHE_DIR = CACHE_DIR.resolve("projects");
}
