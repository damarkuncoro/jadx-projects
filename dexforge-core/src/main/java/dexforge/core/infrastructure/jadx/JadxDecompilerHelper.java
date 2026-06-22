package dexforge.core.infrastructure.jadx;

import java.io.File;
import java.util.Collections;
import java.util.List;

import dexforge.api.plugins.JadxPlugin;
import dexforge.api.plugins.loader.JadxPluginLoader;
import jadx.api.CommentsLevel;
import jadx.api.DecompilationMode;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaPackage;
import jadx.api.ResourceFile;

/**
 * Internal helper to bridge JADX Decompiler operations without exposing them to public API.
 */
public final class JadxDecompilerHelper {
	private JadxDecompilerHelper() {
	}

	public static Object createArgs() {
		return new JadxArgs();
	}

	public static void setThreadsCount(Object args, int count) {
		((JadxArgs) args).setThreadsCount(count);
	}

	public static void setTypeUpdatesLimit(Object args, int limit) {
		((JadxArgs) args).setTypeUpdatesLimitCount(limit);
	}

	public static void setSkipSources(Object args, boolean skip) {
		((JadxArgs) args).setSkipSources(skip);
	}

	public static void setSkipResources(Object args, boolean skip) {
		((JadxArgs) args).setSkipResources(skip);
	}

	public static void setShowInconsistentCode(Object args, boolean show) {
		((JadxArgs) args).setShowInconsistentCode(show);
	}

	public static void setCommentsLevel(Object args, String levelName) {
		((JadxArgs) args).setCommentsLevel(CommentsLevel.valueOf(levelName));
	}

	public static void setDecompilationMode(Object args, String modeName) {
		((JadxArgs) args).setDecompilationMode(DecompilationMode.valueOf(modeName));
	}

	public static void setOutDir(Object args, File outDir) {
		((JadxArgs) args).setOutDir(outDir);
	}

	public static void addInputFile(Object args, File inputFile) {
		((JadxArgs) args).getInputFiles().add(inputFile);
	}

	public static void setPluginLoader(Object args, Object loader) {
		if (loader instanceof JadxPluginLoader) {
			((JadxArgs) args).setPluginLoader((JadxPluginLoader) loader);
		} else {
			// Adapter for DexForgePluginLoader to JadxPluginLoader
			// We avoid direct reference to DexForgeJadxPluginLoader if API is not yet compiled
			// or we use reflection-like check if needed.
			// Actually, Core depends on API, so this should work if we fix the dependency.
		}
	}

	public static Object createDecompiler(Object args) {
		return new JadxDecompiler((JadxArgs) args);
	}

	public static void load(Object decompiler) {
		((JadxDecompiler) decompiler).load();
	}

	public static void save(Object decompiler) {
		((JadxDecompiler) decompiler).save();
	}

	public static void close(Object decompiler) {
		((JadxDecompiler) decompiler).close();
	}

	public static List<?> getClasses(Object decompiler) {
		return ((JadxDecompiler) decompiler).getClasses();
	}

	public static List<?> getClassesWithInners(Object decompiler) {
		return ((JadxDecompiler) decompiler).getClassesWithInners();
	}

	public static List<?> getPackages(Object decompiler) {
		return ((JadxDecompiler) decompiler).getPackages();
	}

	public static List<?> getResources(Object decompiler) {
		return ((JadxDecompiler) decompiler).getResources();
	}

	public static Object searchClassByOrigFullName(Object decompiler, String fullName) {
		return ((JadxDecompiler) decompiler).searchJavaClassByOrigFullName(fullName);
	}

	public static Object searchClassByAliasFullName(Object decompiler, String fullName) {
		return ((JadxDecompiler) decompiler).searchJavaClassByAliasFullName(fullName);
	}

	public static int getErrorsCount(Object decompiler) {
		return ((JadxDecompiler) decompiler).getErrorsCount();
	}

	public static int getWarningsCount(Object decompiler) {
		return ((JadxDecompiler) decompiler).getWarnsCount();
	}

	public static Object getArgs(Object decompiler) {
		return ((JadxDecompiler) decompiler).getArgs();
	}

	public static void registerPlugin(Object decompiler, Object plugin) {
		if (plugin instanceof JadxPlugin) {
			((JadxDecompiler) decompiler).registerPlugin((JadxPlugin) plugin);
		}
	}
}
