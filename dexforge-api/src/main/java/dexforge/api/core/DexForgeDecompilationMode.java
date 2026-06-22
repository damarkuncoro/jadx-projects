package dexforge.api.core;

public enum DexForgeDecompilationMode {
	AUTO,
	RESTRUCTURE,
	SIMPLE,
	FALLBACK;

	public boolean isSpecial() {
		return this == FALLBACK;
	}
}
