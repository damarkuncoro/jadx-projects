package dexforge.api.model;

import dexforge.api.model.metadata.DexForgeAnnotation;
import java.util.Map;
import java.util.Optional;

/**
 * Metadata for decompiled code, mapping positions to semantic nodes and annotations.
 */
public interface DexForgeCodeMetadata {
	/**
	 * Get the annotation (class, method, field ref, etc.) at the specified character position.
	 */
	Optional<DexForgeAnnotation> getAt(int position);

	/**
	 * Get the node (class, method, or field) at the specified character position.
	 */
	Optional<DexForgeNode> getNodeAt(int position);

	/**
	 * Get all annotations in this code block.
	 */
	Map<Integer, DexForgeAnnotation> getAnnotations();

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
		public Optional<DexForgeAnnotation> getAt(int position) {
			return Optional.empty();
		}

		@Override
		public Optional<DexForgeNode> getNodeAt(int position) {
			return Optional.empty();
		}

		@Override
		public Map<Integer, DexForgeAnnotation> getAnnotations() {
			return java.util.Collections.emptyMap();
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
