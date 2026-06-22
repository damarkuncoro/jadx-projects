package dexforge.engine;

import java.util.Objects;

public final class DexForgeSourceLocation {
	private final String uri;
	private final DexForgeSourceRange range;

	public DexForgeSourceLocation(String uri, DexForgeSourceRange range) {
		this.uri = Objects.requireNonNull(uri, "URI cannot be null");
		this.range = Objects.requireNonNull(range, "Range cannot be null");
	}

	public String getUri() {
		return uri;
	}

	public DexForgeSourceRange getRange() {
		return range;
	}
}
