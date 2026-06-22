package dexforge.api.diagnostic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import dexforge.api.model.DexForgeNode;

/**
 * Structured diagnostic information about decompilation issues or code analysis.
 */
public final class DexForgeDiagnostic {
	private final DexForgeDiagnosticSeverity severity;
	private final String message;
	private final String source;
	private final DexForgeNode relatedNode;
	private final int line;
	private final int column;
	private final List<String> suggestions;

	private DexForgeDiagnostic(Builder builder) {
		this.severity = Objects.requireNonNull(builder.severity);
		this.message = Objects.requireNonNull(builder.message);
		this.source = builder.source;
		this.relatedNode = builder.relatedNode;
		this.line = builder.line;
		this.column = builder.column;
		this.suggestions = Collections.unmodifiableList(new ArrayList<>(builder.suggestions));
	}

	public static Builder builder(DexForgeDiagnosticSeverity severity, String message) {
		return new Builder(severity, message);
	}

	public static DexForgeDiagnostic info(String message, String source) {
		return builder(DexForgeDiagnosticSeverity.INFO, message).source(source).build();
	}

	public static DexForgeDiagnostic warning(String message, String source) {
		return builder(DexForgeDiagnosticSeverity.WARNING, message).source(source).build();
	}

	public static DexForgeDiagnostic error(String message, String source) {
		return builder(DexForgeDiagnosticSeverity.ERROR, message).source(source).build();
	}

	public DexForgeDiagnosticSeverity getSeverity() { return severity; }
	public String getMessage() { return message; }
	public String getSource() { return source; }
	public Optional<DexForgeNode> getRelatedNode() { return Optional.ofNullable(relatedNode); }
	public int getLine() { return line; }
	public int getColumn() { return column; }
	public List<String> getSuggestions() { return suggestions; }

	public static final class Builder {
		private final DexForgeDiagnosticSeverity severity;
		private final String message;
		private String source;
		private DexForgeNode relatedNode;
		private int line = -1;
		private int column = -1;
		private final List<String> suggestions = new ArrayList<>();

		private Builder(DexForgeDiagnosticSeverity severity, String message) {
			this.severity = severity;
			this.message = message;
		}

		public Builder source(String source) {
			this.source = source;
			return this;
		}

		public Builder relatedNode(DexForgeNode node) {
			this.relatedNode = node;
			return this;
		}

		public Builder position(int line, int column) {
			this.line = line;
			this.column = column;
			return this;
		}

		public Builder suggest(String suggestion) {
			this.suggestions.add(suggestion);
			return this;
		}

		public DexForgeDiagnostic build() {
			return new DexForgeDiagnostic(this);
		}
	}
}
