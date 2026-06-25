package dexforge.api.resource;

import java.util.Objects;
import dexforge.api.engine.DexForgeEngine;

public final class DexForgeResourceFile {
	private final Object delegate;
	private final DexForgeEngine engine;

	public DexForgeResourceFile(Object delegate, DexForgeEngine engine) {
		this.delegate = Objects.requireNonNull(delegate);
		this.engine = Objects.requireNonNull(engine);
	}

	public String getOriginalName() {
		return engine.getName(delegate);
	}

	public String getDeobfuscatedName() {
		return engine.getFullName(delegate);
	}

	public DexForgeResourceType getType() {
		return DexForgeResourceType.UNKNOWN; // Placeholder
	}

	public String getContent() {
		return engine.getResourceText(delegate);
	}

	@Override
	public String toString() {
		return getOriginalName();
	}
}
