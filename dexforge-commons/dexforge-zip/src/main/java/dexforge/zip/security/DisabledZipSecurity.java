package dexforge.zip.security;

import java.io.File;

import dexforge.zip.IZipEntry;

public class DisabledZipSecurity implements IDexforgeZipSecurity {

	public static final DisabledZipSecurity INSTANCE = new DisabledZipSecurity();

	@Override
	public boolean isValidEntry(IZipEntry entry) {
		return true;
	}

	@Override
	public boolean isValidEntryName(String entryName) {
		return true;
	}

	@Override
	public boolean isInSubDirectory(File baseDir, File file) {
		return true;
	}

	@Override
	public boolean useLimitedDataStream() {
		return false;
	}

	@Override
	public int getMaxEntriesCount() {
		return -1;
	}
}
