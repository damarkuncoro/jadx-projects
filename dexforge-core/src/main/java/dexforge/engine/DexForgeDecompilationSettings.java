package dexforge.engine;

/**
 * Configuration options for DexForge decompilation behavior.
 * Settings are mapped to JADX arguments where available.
 */
public final class DexForgeDecompilationSettings {
	public static final int DEFAULT_THREADS_COUNT = 4;
	private static final int DEFAULT_TYPE_UPDATES_LIMIT = 10000;

	private int threadsCount = DEFAULT_THREADS_COUNT;
	private int typeUpdatesLimit = DEFAULT_TYPE_UPDATES_LIMIT;

	private DexForgeDecompilationSettings() {
	}

	public static DexForgeDecompilationSettings create() {
		return new DexForgeDecompilationSettings();
	}

	public int getThreadsCount() {
		return threadsCount;
	}

	public DexForgeDecompilationSettings threadsCount(int count) {
		this.threadsCount = Math.max(1, count);
		return this;
	}

	public int getTypeUpdatesLimit() {
		return typeUpdatesLimit;
	}

	public DexForgeDecompilationSettings typeUpdatesLimit(int limit) {
		this.typeUpdatesLimit = Math.max(1, limit);
		return this;
	}
}