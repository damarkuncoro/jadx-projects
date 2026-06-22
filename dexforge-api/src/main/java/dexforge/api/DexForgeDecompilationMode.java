package dexforge.api;

import jadx.api.DecompilationMode;

public enum DexForgeDecompilationMode {
	AUTO,
	RESTRUCTURE,
	SIMPLE,
	FALLBACK;

	DecompilationMode toJadx() {
		return DecompilationMode.valueOf(name());
	}

	public boolean isSpecial() {
		return toJadx().isSpecial();
	}
}
