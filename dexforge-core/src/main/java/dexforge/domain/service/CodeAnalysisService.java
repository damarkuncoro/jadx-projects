package dexforge.domain.service;

import java.util.ArrayList;
import java.util.List;

import dexforge.domain.model.analysis.CodeAnalysis;
import dexforge.domain.model.analysis.SecurityFinding;
import dexforge.domain.model.source.SourceFile;

/**
 * Domain Service: CodeAnalysisService
 * Handles code analysis operations.
 */
public class CodeAnalysisService {

	/**
	 * Performs security analysis on source file.
	 */
	public CodeAnalysis analyzeSecurity(SourceFile sourceFile) {
		List<SecurityFinding> findings = new ArrayList<>();
		String code = sourceFile.getSourceCode();

		// Check for common security issues
		if (code.contains("WebView") && code.contains("loadUrl")) {
			findings.add(SecurityFinding.webViewLoadUrl(sourceFile.getSourceFileId()));
		}
		if (code.contains("onReceivedSslError") && code.contains("proceed()")) {
			findings.add(SecurityFinding.sslErrorProceed(sourceFile.getSourceFileId()));
		}
		if (code.contains("addJavascriptInterface") && code.contains("@JavascriptInterface")) {
			findings.add(SecurityFinding.javascriptInterface(sourceFile.getSourceFileId()));
		}

		return CodeAnalysis.of(sourceFile.getSourceFileId(), findings);
	}
}
