package dexforge.api.analysis;

import java.util.Objects;
import dexforge.api.model.DexForgeNode;

/**
 * Represents a single discovery from a deep analysis pass.
 */
public final class DexForgeFinding {
	private final String type;
	private final String message;
	private final DexForgeAnalysisSeverity severity;
	private final DexForgeNode location;
	private String suggestedFix;

	public DexForgeFinding(String type, String message, DexForgeAnalysisSeverity severity, DexForgeNode location) {
		this.type = Objects.requireNonNull(type);
		this.message = Objects.requireNonNull(message);
		this.severity = Objects.requireNonNull(severity);
		this.location = location;
	}

	public String getSuggestedFix() { return suggestedFix; }
	public void setSuggestedFix(String fix) { this.suggestedFix = fix; }

	public String getType() { return type; }
	public String getMessage() { return message; }
	public DexForgeAnalysisSeverity getSeverity() { return severity; }
	public DexForgeNode getLocation() { return location; }

	@Override
	public String toString() {
		return String.format("[%s] %s: %s at %s", severity, type, message, location != null ? location.getFullName() : "unknown");
	}
}
