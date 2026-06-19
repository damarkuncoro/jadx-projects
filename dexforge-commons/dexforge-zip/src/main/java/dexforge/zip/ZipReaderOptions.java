package dexforge.zip;

import java.util.Set;

import dexforge.zip.security.IDexforgeZipSecurity;
import dexforge.zip.security.DexforgeZipSecurity;

public class ZipReaderOptions {

	public static ZipReaderOptions getDefault() {
		return new ZipReaderOptions(new DexforgeZipSecurity(), ZipReaderFlags.none());
	}

	private final IDexforgeZipSecurity zipSecurity;
	private final Set<ZipReaderFlags> flags;

	public ZipReaderOptions(IDexforgeZipSecurity zipSecurity, Set<ZipReaderFlags> flags) {
		this.zipSecurity = zipSecurity;
		this.flags = flags;
	}

	public IDexforgeZipSecurity getZipSecurity() {
		return zipSecurity;
	}

	public Set<ZipReaderFlags> getFlags() {
		return flags;
	}
}
