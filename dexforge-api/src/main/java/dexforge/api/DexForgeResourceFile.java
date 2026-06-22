package dexforge.api;

import java.util.Objects;

import jadx.api.ResourceFile;

public final class DexForgeResourceFile {
	private final ResourceFile delegate;

	DexForgeResourceFile(ResourceFile delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	public String getOriginalName() {
		return delegate.getOriginalName();
	}

	public String getDeobfuscatedName() {
		return delegate.getDeobfName();
	}

	public void setDeobfuscatedName(String name) {
		delegate.setDeobfName(name);
	}

	public DexForgeResourceType getType() {
		return DexForgeResourceType.fromJadx(delegate.getType());
	}

	/**
	 * JADX bridge kept for compatibility during migration.
	 * New code should use DexForge API methods instead of unwrapping.
	 */
	@Deprecated(forRemoval = false)
	public ResourceFile unwrap() {
		return delegate;
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
