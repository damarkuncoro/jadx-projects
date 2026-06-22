package dexforge.engine;

import java.util.Objects;

public final class DexForgeHover {
	private final String markdown;

	public DexForgeHover(String markdown) {
		this.markdown = Objects.requireNonNull(markdown, "Markdown cannot be null");
	}

	public String getMarkdown() {
		return markdown;
	}
}
