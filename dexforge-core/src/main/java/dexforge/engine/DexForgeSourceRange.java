package dexforge.engine;

import java.util.Objects;

public final class DexForgeSourceRange {
	private final DexForgeSourcePosition start;
	private final DexForgeSourcePosition end;

	public DexForgeSourceRange(DexForgeSourcePosition start, DexForgeSourcePosition end) {
		this.start = Objects.requireNonNull(start, "Start position cannot be null");
		this.end = Objects.requireNonNull(end, "End position cannot be null");
	}

	public DexForgeSourcePosition getStart() {
		return start;
	}

	public DexForgeSourcePosition getEnd() {
		return end;
	}
}
