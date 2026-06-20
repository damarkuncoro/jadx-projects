package dexforge.engine;

/**
 * Progress callback used by DexForge engine clients.
 */
public interface DexForgeProgressReporter {
	DexForgeProgressReporter NO_OP = new DexForgeProgressReporter() {
		@Override
		public void onProgress(long done, long total) {
		}

		@Override
		public void clear() {
		}
	};

	void onProgress(long done, long total);

	void clear();
}
