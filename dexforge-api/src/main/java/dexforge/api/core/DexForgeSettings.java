package dexforge.api.core;

import java.util.Objects;

import dexforge.core.infrastructure.jadx.JadxDecompilerHelper;

/**
 * Immutable settings for DexForge decompiler.
 * Use {@link Builder} to create instances.
 */
public final class DexForgeSettings {
	public static final int DEFAULT_THREADS_COUNT = 4;
	public static final int DEFAULT_TYPE_UPDATES_LIMIT = 10000;

	private final int threadsCount;
	private final int typeUpdatesLimit;
	private final boolean useDexForgeApi;
	private final boolean skipSources;
	private final boolean skipResources;
	private final boolean showInconsistentCode;
	private final DexForgeCommentsLevel commentsLevel;
	private final DexForgeDecompilationMode decompilationMode;

	private DexForgeSettings(Builder builder) {
		this.threadsCount = builder.threadsCount;
		this.typeUpdatesLimit = builder.typeUpdatesLimit;
		this.useDexForgeApi = builder.useDexForgeApi;
		this.skipSources = builder.skipSources;
		this.skipResources = builder.skipResources;
		this.showInconsistentCode = builder.showInconsistentCode;
		this.commentsLevel = builder.commentsLevel;
		this.decompilationMode = builder.decompilationMode;
	}

	public static Builder builder() {
		return new Builder();
	}

	public int getThreadsCount() {
		return threadsCount;
	}

	public int getTypeUpdatesLimit() {
		return typeUpdatesLimit;
	}

	public boolean isUseDexForgeApi() {
		return useDexForgeApi;
	}

	public boolean isSkipSources() {
		return skipSources;
	}

	public boolean isSkipResources() {
		return skipResources;
	}

	public boolean isShowInconsistentCode() {
		return showInconsistentCode;
	}

	public DexForgeCommentsLevel getCommentsLevel() {
		return commentsLevel;
	}

	public DexForgeDecompilationMode getDecompilationMode() {
		return decompilationMode;
	}

	/**
	 * Internal bridge to apply settings to JADX args.
	 */
	@Deprecated(forRemoval = false)
	public void applyTo(Object args) {
		Objects.requireNonNull(args);
		JadxDecompilerHelper.setThreadsCount(args, threadsCount);
		JadxDecompilerHelper.setTypeUpdatesLimit(args, typeUpdatesLimit);
		JadxDecompilerHelper.setSkipSources(args, skipSources);
		JadxDecompilerHelper.setSkipResources(args, skipResources);
		JadxDecompilerHelper.setShowInconsistentCode(args, showInconsistentCode);
		JadxDecompilerHelper.setCommentsLevel(args, commentsLevel.name());
		JadxDecompilerHelper.setDecompilationMode(args, decompilationMode.name());
	}

	public static final class Builder {
		private int threadsCount = DEFAULT_THREADS_COUNT;
		private int typeUpdatesLimit = DEFAULT_TYPE_UPDATES_LIMIT;
		private boolean useDexForgeApi = true;
		private boolean skipSources = false;
		private boolean skipResources = false;
		private boolean showInconsistentCode = true;
		private DexForgeCommentsLevel commentsLevel = DexForgeCommentsLevel.INFO;
		private DexForgeDecompilationMode decompilationMode = DexForgeDecompilationMode.AUTO;

		private Builder() {
		}

		public Builder threadsCount(int threadsCount) {
			this.threadsCount = Math.max(1, threadsCount);
			return this;
		}

		public Builder typeUpdatesLimit(int typeUpdatesLimit) {
			this.typeUpdatesLimit = Math.max(1, typeUpdatesLimit);
			return this;
		}

		public Builder useDexForgeApi(boolean useDexForgeApi) {
			this.useDexForgeApi = useDexForgeApi;
			return this;
		}

		public Builder skipSources(boolean skipSources) {
			this.skipSources = skipSources;
			return this;
		}

		public Builder skipResources(boolean skipResources) {
			this.skipResources = skipResources;
			return this;
		}

		public Builder showInconsistentCode(boolean showInconsistentCode) {
			this.showInconsistentCode = showInconsistentCode;
			return this;
		}

		public Builder commentsLevel(DexForgeCommentsLevel commentsLevel) {
			this.commentsLevel = Objects.requireNonNull(commentsLevel);
			return this;
		}

		public Builder decompilationMode(DexForgeDecompilationMode decompilationMode) {
			this.decompilationMode = Objects.requireNonNull(decompilationMode);
			return this;
		}

		public DexForgeSettings build() {
			return new DexForgeSettings(this);
		}
	}
}
