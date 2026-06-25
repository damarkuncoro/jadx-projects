package dexforge.api.analysis.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dexforge.api.analysis.DexForgeAnalysisSeverity;
import dexforge.api.analysis.DexForgeAnalyzer;
import dexforge.api.analysis.DexForgeFinding;
import dexforge.api.core.DexForgeProject;
import dexforge.api.model.DexForgeClass;

/**
 * Scans decompiled code for potential hardcoded API keys or secrets.
 */
public final class HardcodedSecretAnalyzer implements DexForgeAnalyzer {
	// Simple patterns for common secrets
	private static final Pattern API_KEY_PATTERN = Pattern.compile("(?i)(api_key|secret|password|token)\\s*=\\s*\"([^\"]{10,})\"");

	@Override
	public String getName() {
		return "Hardcoded Secret Scanner";
	}

	@Override
	public String getDescription() {
		return "Detects potential API keys, tokens, and secrets embedded in the code.";
	}

	@Override
	public List<DexForgeFinding> analyze(DexForgeProject project) {
		List<DexForgeFinding> findings = new ArrayList<>();

		for (DexForgeClass cls : project.getClasses()) {
			String code = cls.getCode();
			if (code.isEmpty()) continue;

			Matcher matcher = API_KEY_PATTERN.matcher(code);
			while (matcher.find()) {
				String secretType = matcher.group(1);
				findings.add(new DexForgeFinding(
						"SECURITY_HARDCODED_SECRET",
						"Potential hardcoded " + secretType + " found.",
						DexForgeAnalysisSeverity.HIGH,
						cls
				));
			}
		}

		return findings;
	}
}
