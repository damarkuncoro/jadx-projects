package dexforge.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import dexforge.api.engine.DexForgeEngine;
import dexforge.api.model.metadata.DexForgeAnnotation;

/**
 * Internal implementation of DexForgeCodeMetadata.
 * Bridges to decompiler engine without exposing engine types.
 */
final class DexForgeMetadataImpl implements DexForgeCodeMetadata {
	private final Object codeInfo;
	private final DexForgeEngine engine;

	DexForgeMetadataImpl(Object codeInfo, DexForgeEngine engine) {
		this.codeInfo = Objects.requireNonNull(codeInfo);
		this.engine = Objects.requireNonNull(engine);
	}

	@Override
	public Optional<DexForgeAnnotation> getAt(int position) {
		Object raw = engine.getAnnotationAt(codeInfo, position);
		return Optional.ofNullable(wrapAnnotation(raw));
	}

	@Override
	public Optional<DexForgeNode> getNodeAt(int position) {
		// This usually requires the class as well, but we'll try to find it from codeInfo if possible
		return Optional.empty();
	}

	@Override
	public Map<Integer, DexForgeAnnotation> getAnnotations() {
		Map<Integer, Object> rawMap = engine.getAnnotations(codeInfo);
		if (rawMap.isEmpty()) return Collections.emptyMap();

		Map<Integer, DexForgeAnnotation> result = new HashMap<>();
		for (Map.Entry<Integer, Object> entry : rawMap.entrySet()) {
			result.put(entry.getKey(), wrapAnnotation(entry.getValue()));
		}
		return Collections.unmodifiableMap(result);
	}

	@Override
	public Map<Integer, DexForgeNode> getAllDefinitions() {
		return Collections.emptyMap();
	}

	@Override
	public Optional<Integer> getSourceLine(int decompiledLine) {
		return Optional.empty();
	}

	private DexForgeAnnotation wrapAnnotation(Object raw) {
		if (raw == null) return null;
		String typeName = engine.getAnnotationType(raw);
		String data = engine.getAnnotationData(raw);

		DexForgeAnnotation.AnnType type;
		try {
			type = DexForgeAnnotation.AnnType.valueOf(typeName);
		} catch (Exception e) {
			type = DexForgeAnnotation.AnnType.OFFSET; // Default
		}

		return new DexForgeAnnotationImpl(type, data);
	}
}
