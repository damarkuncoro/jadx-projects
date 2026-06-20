package dexforge.engine;

import java.util.Objects;

/**
 * Public diagnostic entry for DexForge API and JSON consumers.
 */
public final class DexForgeDiagnostic {
	private final DexForgeDiagnosticSeverity severity;
	private final String message;
	private final String source;
	private final int line;
	private final int column;

	private DexForgeDiagnostic(Builder builder) {
		this.severity = Objects.requireNonNull(builder.severity, "Severity cannot be null");
		this.message = Objects.requireNonNull(builder.message, "Message cannot be null");
		this.source = builder.source;
		this.line = builder.line;
		this.column = builder.column;
	}

	public DexForgeDiagnosticSeverity getSeverity() {
		return severity;
	}

	public String getMessage() {
		return message;
	}

	public String getSource() {
		return source;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	public static Builder builder(DexForgeDiagnosticSeverity severity, String message) {
		return new Builder(severity, message);
	}

	public static final class Builder {
		private final DexForgeDiagnosticSeverity severity;
		private final String message;
		private String source;
		private int line = -1;
		private int column = -1;

		private Builder(DexForgeDiagnosticSeverity severity, String message) {
			this.severity = severity;
			this.message = message;
		}

		public Builder source(String source) {
			this.source = source;
			return this;
		}

		public Builder position(int line, int column) {
			this.line = line;
			this.column = column;
			return this;
		}

		public DexForgeDiagnostic build() {
			return new DexForgeDiagnostic(this);
		}
	}
}
