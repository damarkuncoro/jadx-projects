package dexforge.api.resource;

import java.util.Objects;

import dexforge.core.infrastructure.jadx.JadxResourceHelper;

public final class DexForgeResourceFile {
	private final Object delegate;

	public DexForgeResourceFile(Object delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	public String getOriginalName() {
		return JadxResourceHelper.getOriginalName(delegate);
	}

	public String getDeobfuscatedName() {
		return JadxResourceHelper.getDeobfName(delegate);
	}

	public void setDeobfuscatedName(String name) {
		JadxResourceHelper.setDeobfName(delegate, name);
	}

	public DexForgeResourceType getType() {
		return DexForgeResourceType.valueOf(JadxResourceHelper.getTypeName(delegate));
	}

	/**
	 * Load and decode the content of this resource.
	 */
	public DexForgeResourceContent loadContent() {
		Object resContainer = JadxResourceHelper.loadContent(delegate);
		return new DexForgeResourceContentImpl(resContainer);
	}

	/**
	 * bridge kept for internal use.
	 */
	@Deprecated(forRemoval = false)
	public Object unwrap() {
		return delegate;
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
