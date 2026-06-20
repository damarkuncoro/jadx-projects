package dexforge.core.ports.decompile;

public interface DecompileProgressReporter {
	DecompileProgressReporter NO_OP = new DecompileProgressReporter() {
		@Override
		public void onProgress(long done, long total) {
		}

		@Override
		public void clear() {
		}
	};

	void onProgress(long done, long total);

	default void clear() {
	}
}
