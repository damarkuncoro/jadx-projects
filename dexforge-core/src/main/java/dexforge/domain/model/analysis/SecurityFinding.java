package dexforge.domain.model.analysis;

import java.util.Objects;

import dexforge.domain.model.source.SourceFileId;

/**
 * Value Object: SecurityFinding
 */
public final class SecurityFinding {
	public enum Severity {
		LOW, MEDIUM, HIGH, CRITICAL
	}

	private final String type;
	private final String description;
	private final Severity severity;
	private final SourceFileId sourceFileId;
	private final String location;

	private SecurityFinding(String type, String description, Severity severity, SourceFileId sourceFileId, String location) {
		this.type = Objects.requireNonNull(type);
		this.description = Objects.requireNonNull(description);
		this.severity = Objects.requireNonNull(severity);
		this.sourceFileId = sourceFileId;
		this.location = location != null ? location : "";
	}

	public static SecurityFinding webViewLoadUrl(SourceFileId sourceFileId) {
		return new SecurityFinding("WEBVIEW_LOAD_URL",
				"WebView.loadUrl() can lead to XSS if URL is not validated",
				Severity.MEDIUM,
				sourceFileId,
				"");
	}

	public static SecurityFinding sslErrorProceed(SourceFileId sourceFileId) {
		return new SecurityFinding("SSL_PROCEED",
				"SSL error proceeding without validation - vulnerable to MITM",
				Severity.CRITICAL,
				sourceFileId,
				"");
	}

	public static SecurityFinding javascriptInterface(SourceFileId sourceFileId) {
		return new SecurityFinding("JAVASCRIPT_INTERFACE",
				"JavascriptInterface may expose sensitive methods to JS",
				Severity.HIGH,
				sourceFileId,
				"");
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public Severity getSeverity() {
		return severity;
	}

	public SourceFileId getSourceFileId() {
		return sourceFileId;
	}

	public String getLocation() {
		return location;
	}
}
