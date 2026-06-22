package dexforge.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import dexforge.core.infrastructure.jadx.JadxCodeHelper;

/**
 * Internal implementation of DexForgeCodeMetadata.
 * Bridges to decompiler engine without exposing engine types.
 */
final class DexForgeMetadataImpl implements DexForgeCodeMetadata {
	private final Object codeInfo;
	private Map<Integer, DexForgeNode> definitions;

	DexForgeMetadataImpl(Object codeInfo) {
		this.codeInfo = Objects.requireNonNull(codeInfo);
	}

	@Override
	public Optional<DexForgeNode> getNodeAt(int position) {
		Object node = JadxCodeHelper.getNodeAt(codeInfo, position);
		return Optional.ofNullable(DexForgeNodeFactory.wrap(node));
	}

	@Override
	public Map<Integer, DexForgeNode> getAllDefinitions() {
		if (definitions == null) {
			Map<Integer, Object> rawMap = JadxCodeHelper.getDefinitions(codeInfo);
			if (rawMap.isEmpty()) {
				definitions = Collections.emptyMap();
			} else {
				Map<Integer, DexForgeNode> wrappedMap = new HashMap<>(rawMap.size());
				rawMap.forEach((pos, node) -> wrappedMap.put(pos, DexForgeNodeFactory.wrap(node)));
				definitions = Collections.unmodifiableMap(wrappedMap);
			}
		}
		return definitions;
	}

	@Override
	public Optional<Integer> getSourceLine(int decompiledLine) {
		return Optional.ofNullable(JadxCodeHelper.getSourceLine(codeInfo, decompiledLine));
	}
}
