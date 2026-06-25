package dexforge.api.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import dexforge.api.engine.DexForgeEngine;

/**
 * Representation of decompiled code along with its metadata.
 */
public final class DexForgeCodeInfo implements ICodeInfo {
	private final Object delegate;
	private final DexForgeEngine engine;
	private DexForgeCodeMetadata cachedMetadata;

	public DexForgeCodeInfo(Object delegate, DexForgeEngine engine) {
		this.delegate = Objects.requireNonNull(delegate);
		this.engine = Objects.requireNonNull(engine);
	}

	@Override
	public String getCodeStr() {
		return engine.getCode(delegate);
	}

	@Override
	public boolean hasMetadata() {
		return true; // Assume true for now
	}

	@Override
	public DexForgeCodeMetadata getCodeMetadata() {
		if (cachedMetadata == null) {
			cachedMetadata = DexForgeNodeFactory.createMetadata(delegate, engine);
		}
		return cachedMetadata;
	}

	@Override
	public Object unwrap() {
		return delegate;
	}

	public String getCode() {
		return getCodeStr();
	}

	public DexForgeCodeMetadata getMetadata() {
		return getCodeMetadata();
	}

	public Optional<Integer> getSourceLine(int decompiledLine) {
		return getMetadata().getSourceLine(decompiledLine);
	}

	public List<Integer> getUsePlacesFor(DexForgeNode node) {
		return Collections.emptyList();
	}
}
