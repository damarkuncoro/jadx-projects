package dexforge.api;

import java.util.Objects;

import jadx.api.ICodeInfo;

public final class DexForgeCodeInfo {
	public static final DexForgeCodeInfo EMPTY = new DexForgeCodeInfo(ICodeInfo.EMPTY);

	private final ICodeInfo delegate;

	DexForgeCodeInfo(ICodeInfo delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	public String getCode() {
		return delegate.getCodeStr();
	}

	public boolean hasMetadata() {
		return delegate.hasMetadata();
	}

	/**
	 * JADX bridge kept for compatibility during migration.
	 * New code should use DexForge API methods instead of unwrapping.
	 */
	@Deprecated(forRemoval = false)
	public ICodeInfo unwrap() {
		return delegate;
	}

	ICodeInfo delegate() {
		return delegate;
	}
}
