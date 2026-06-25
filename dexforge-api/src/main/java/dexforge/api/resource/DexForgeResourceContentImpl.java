package dexforge.api.resource;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import dexforge.api.engine.DexForgeEngine;

/**
 * Internal implementation of DexForgeResourceContent.
 */
final class DexForgeResourceContentImpl implements DexForgeResourceContent {
	private final Object delegate;
	private final DexForgeEngine engine;

	DexForgeResourceContentImpl(Object delegate, DexForgeEngine engine) {
		this.delegate = Objects.requireNonNull(delegate);
		this.engine = Objects.requireNonNull(engine);
	}

	@Override
	public String getName() {
		return engine.getName(delegate);
	}

	@Override
	public DexForgeResourceContentType getContentType() {
		return DexForgeResourceContentType.UNKNOWN;
	}

	@Override
	public Optional<String> getText() {
		return Optional.of(engine.getCode(delegate));
	}

	@Override
	public Optional<byte[]> getData() {
		return Optional.empty();
	}

	@Override
	public List<DexForgeResourceContent> getSubContents() {
		return Collections.emptyList();
	}
}
