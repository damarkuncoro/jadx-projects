package dexforge.api;

import java.util.Objects;

public final class DexForgeDiagnostic {
	private final DexForgeDiagnosticSeverity severity;
	private final String message;
	private final String source;

	private DexForgeDiagnostic(DexForgeDiagnosticSeverity severity, String message, String source) {
		this.severity = Objects.requireNonNull(severity);
		this.message = Objects.requireNonNull(message);
		this.source = source;
	}

	public static DexForgeDiagnostic info(String message, String source) {
		return new DexForgeDiagnostic(DexForgeDiagnosticSeverity.INFO, message, source);
	}

	public static DexForgeDiagnostic warning(String message, String source) {
		return new DexForgeDiagnostic(DexForgeDiagnosticSeverity.WARNING, message, source);
	}

	public static DexForgeDiagnostic error(String message, String source) {
		return new DexForgeDiagnostic(DexForgeDiagnosticSeverity.ERROR, message, source);
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
}
