package dexforge.api.model;

import java.util.Map;
import java.util.Optional;

/**
 * Metadata for decompiled code, mapping positions to semantic nodes.
 * Independent of any specific decompiler engine.
 */
public interface DexForgeCodeMetadata {
	/**
	 * Get the node (class, method, or field) at the specified character position.
	 */
	Optional<DexForgeNode> getNodeAt(int position);

	/**
	 * Get all defined nodes in this code block.
	 */
	Map<Integer, DexForgeNode> getAllDefinitions();

	/**
	 * Map a decompiled line number back to the original source line (if available).
	 */
	Optional<Integer> getSourceLine(int decompiledLine);

	/**
	 * Empty metadata implementation.
	 */
	DexForgeCodeMetadata EMPTY = new DexForgeCodeMetadata() {
		@Override
		public Optional<DexForgeNode> getNodeAt(int position) {
			return Optional.empty();
		}

		@Override
		public Map<Integer, DexForgeNode> getAllDefinitions() {
			return java.util.Collections.emptyMap();
		}

		@Override
		public Optional<Integer> getSourceLine(int decompiledLine) {
			return Optional.empty();
		}
	};
}
