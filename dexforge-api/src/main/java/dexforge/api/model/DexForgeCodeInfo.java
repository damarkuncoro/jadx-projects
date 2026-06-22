package dexforge.api.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import dexforge.core.infrastructure.jadx.JadxCodeHelper;

/**
 * Representation of decompiled code along with its metadata.
 */
public final class DexForgeCodeInfo {
	public static final DexForgeCodeInfo EMPTY = new DexForgeCodeInfo(JadxCodeHelper.emptyCodeInfo());

	private final Object delegate;
	private DexForgeCodeMetadata cachedMetadata;

	public DexForgeCodeInfo(Object delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	public String getCode() {
		return JadxCodeHelper.getCode(delegate);
	}

	public boolean hasMetadata() {
		return JadxCodeHelper.hasMetadata(delegate);
	}

	/**
	 * Get the metadata for this code block.
	 */
	public DexForgeCodeMetadata getMetadata() {
		if (!hasMetadata()) {
			return DexForgeCodeMetadata.EMPTY;
		}
		if (cachedMetadata == null) {
			cachedMetadata = DexForgeNodeFactory.createMetadata(delegate);
		}
		return cachedMetadata;
	}

	/**
	 * Shortcut to get the original source line.
	 */
	public Optional<Integer> getSourceLine(int decompiledLine) {
		return getMetadata().getSourceLine(decompiledLine);
	}

	public List<Integer> getUsePlacesFor(DexForgeNode node) {
		if (!hasMetadata()) {
			return Collections.emptyList();
		}
		Object internalNode = null;
		if (node instanceof DexForgeClass) {
			internalNode = ((DexForgeClass) node).unwrap();
		} else if (node instanceof DexForgeMethod) {
			internalNode = ((DexForgeMethod) node).unwrap();
		} else if (node instanceof DexForgeField) {
			internalNode = ((DexForgeField) node).unwrap();
		}
		return JadxCodeHelper.getUsePlacesFor(delegate, internalNode);
	}

	/**
	 * bridge kept for internal use.
	 */
	@Deprecated(forRemoval = false)
	public Object unwrap() {
		return delegate;
	}

	public Object delegate() {
		return delegate;
	}
}
