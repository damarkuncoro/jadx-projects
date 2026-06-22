package dexforge.api.plugins.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jetbrains.annotations.Nullable;

import dexforge.api.plugins.JadxPluginContext;
import dexforge.zip.IZipEntry;
import dexforge.zip.ZipReader;
import dexforge.zip.io.LimitedInputStream;
import dexforge.zip.security.DexforgeZipSecurity;
import dexforge.zip.security.DisabledZipSecurity;
import dexforge.zip.security.IDexforgeZipSecurity;

import jadx.api.JadxDecompiler;
import jadx.core.utils.Utils;

/**
 * Deprecated, migrate to {@link ZipReader}. <br>
 * Prefer already configured instance from {@link JadxDecompiler#getZipReader()} or
 * {@link JadxPluginContext#getZipReader()}.
 */
@Deprecated
public class ZipSecurity {
	private static final boolean DISABLE_CHECKS = Utils.getEnvVarBool("JADX_DISABLE_ZIP_SECURITY", false);

	private static final int MAX_ENTRIES_COUNT = Utils.getEnvVarInt("JADX_ZIP_MAX_ENTRIES_COUNT", 100_000);

	private static final IDexforgeZipSecurity ZIP_SECURITY = buildZipSecurity();

	private static final ZipReader ZIP_READER = new ZipReader(ZIP_SECURITY);

	private static IDexforgeZipSecurity buildZipSecurity() {
		if (DISABLE_CHECKS) {
			return DisabledZipSecurity.INSTANCE;
		}
		DexforgeZipSecurity jadxZipSecurity = new DexforgeZipSecurity();
		jadxZipSecurity.setMaxEntriesCount(MAX_ENTRIES_COUNT);
		return jadxZipSecurity;
	}

	private ZipSecurity() {
	}

	@Deprecated
	public static boolean isInSubDirectory(File baseDir, File file) {
		return ZIP_SECURITY.isInSubDirectory(baseDir, file);
	}

	/**
	 * Checks that entry name contains no any traversals and prevents cases like "../classes.dex",
	 * to limit output only to the specified directory
	 */
	@Deprecated
	public static boolean isValidZipEntryName(String entryName) {
		return ZIP_SECURITY.isValidEntryName(entryName);
	}

	@Deprecated
	public static boolean isZipBomb(IZipEntry entry) {
		return !ZIP_SECURITY.isValidEntry(entry);
	}

	@Deprecated
	public static boolean isValidZipEntry(IZipEntry entry) {
		return ZIP_SECURITY.isValidEntry(entry);
	}

	@Deprecated
	public static InputStream getInputStreamForEntry(ZipFile zipFile, ZipEntry entry) throws IOException {
		if (DISABLE_CHECKS) {
			return new BufferedInputStream(zipFile.getInputStream(entry));
		}
		InputStream in = zipFile.getInputStream(entry);
		LimitedInputStream limited = new LimitedInputStream(in, entry.getSize());
		return new BufferedInputStream(limited);
	}

	/**
	 * Visit valid entries in a zip file.
	 * Return not null value from visitor to stop iteration.
	 */
	@Deprecated
	@Nullable
	public static <R> R visitZipEntries(File file, Function<IZipEntry, R> visitor) {
		return ZIP_READER.visitEntries(file, visitor);
	}

	@Deprecated
	public static void readZipEntries(File file, BiConsumer<IZipEntry, InputStream> visitor) {
		ZIP_READER.readEntries(file, visitor);
	}
}
