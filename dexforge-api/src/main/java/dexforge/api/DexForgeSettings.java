package dexforge.api;

import java.util.Objects;

import jadx.api.JadxArgs;

public final class DexForgeSettings {
	public static final int DEFAULT_THREADS_COUNT = 4;
	public static final int DEFAULT_TYPE_UPDATES_LIMIT = 10000;

	private int threadsCount = DEFAULT_THREADS_COUNT;
	private int typeUpdatesLimit = DEFAULT_TYPE_UPDATES_LIMIT;
	private boolean useDexForgeApi;
	private boolean skipSources;
	private boolean skipResources;
	private boolean showInconsistentCode = true;
	private DexForgeCommentsLevel commentsLevel = DexForgeCommentsLevel.INFO;
	private DexForgeDecompilationMode decompilationMode = DexForgeDecompilationMode.AUTO;

	private DexForgeSettings() {
	}

	public static DexForgeSettings create() {
		return new DexForgeSettings();
	}

	public int getThreadsCount() {
		return threadsCount;
	}

	public DexForgeSettings threadsCount(int threadsCount) {
		this.threadsCount = Math.max(1, threadsCount);
		return this;
	}

	public int getTypeUpdatesLimit() {
		return typeUpdatesLimit;
	}

	public DexForgeSettings typeUpdatesLimit(int typeUpdatesLimit) {
		this.typeUpdatesLimit = Math.max(1, typeUpdatesLimit);
		return this;
	}

	public boolean isUseDexForgeApi() {
		return useDexForgeApi;
	}

	public DexForgeSettings useDexForgeApi(boolean useDexForgeApi) {
		this.useDexForgeApi = useDexForgeApi;
		return this;
	}

	public boolean isSkipSources() {
		return skipSources;
	}

	public DexForgeSettings skipSources(boolean skipSources) {
		this.skipSources = skipSources;
		return this;
	}

	public boolean isSkipResources() {
		return skipResources;
	}

	public DexForgeSettings skipResources(boolean skipResources) {
		this.skipResources = skipResources;
		return this;
	}

	public boolean isShowInconsistentCode() {
		return showInconsistentCode;
	}

	public DexForgeSettings showInconsistentCode(boolean showInconsistentCode) {
		this.showInconsistentCode = showInconsistentCode;
		return this;
	}

	public DexForgeCommentsLevel getCommentsLevel() {
		return commentsLevel;
	}

	public DexForgeSettings commentsLevel(DexForgeCommentsLevel commentsLevel) {
		this.commentsLevel = Objects.requireNonNull(commentsLevel);
		return this;
	}

	public DexForgeDecompilationMode getDecompilationMode() {
		return decompilationMode;
	}

	public DexForgeSettings decompilationMode(DexForgeDecompilationMode decompilationMode) {
		this.decompilationMode = Objects.requireNonNull(decompilationMode);
		return this;
	}

	JadxArgs applyTo(JadxArgs args) {
		Objects.requireNonNull(args);
		args.setThreadsCount(threadsCount);
		args.setTypeUpdatesLimitCount(typeUpdatesLimit);
		args.setSkipSources(skipSources);
		args.setSkipResources(skipResources);
		args.setShowInconsistentCode(showInconsistentCode);
		args.setCommentsLevel(commentsLevel.toJadx());
		args.setDecompilationMode(decompilationMode.toJadx());
		return args;
	}
}
