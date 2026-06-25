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
 * Detects weak cryptographic algorithms and insecure practices.
 */
public final class InsecureCryptoAnalyzer implements DexForgeAnalyzer {
	private static final Pattern WEAK_ALGO_PATTERN = Pattern.compile("(?i)\"(MD5|SHA1|DES|RC4|AES/ECB/PKCS5Padding)\"");
	private static final Pattern STATIC_IV_PATTERN = Pattern.compile("(?i)new\\s+IvParameterSpec\\s*\\(\\s*new\\s+byte\\[\\]\\s*\\{[^}]+\\}\\s*\\)");

	@Override
	public String getName() {
		return "Insecure Cryptography Scanner";
	}

	@Override
	public String getDescription() {
		return "Detects weak algorithms (MD5, SHA1, DES) and insecure encryption modes (ECB).";
	}

	@Override
	public List<DexForgeFinding> analyze(DexForgeProject project) {
		List<DexForgeFinding> findings = new ArrayList<>();

		for (DexForgeClass cls : project.getClasses()) {
			String code = cls.getCode();
			if (code.isEmpty()) continue;

			// Check for weak algorithms
			Matcher algoMatcher = WEAK_ALGO_PATTERN.matcher(code);
			while (algoMatcher.find()) {
				findings.add(new DexForgeFinding(
						"CRYPTO_WEAK_ALGORITHM",
						"Insecure cryptographic algorithm found: " + algoMatcher.group(1),
						DexForgeAnalysisSeverity.HIGH,
						cls
				));
			}

			// Check for static IVs
			Matcher ivMatcher = STATIC_IV_PATTERN.matcher(code);
			while (ivMatcher.find()) {
				findings.add(new DexForgeFinding(
						"CRYPTO_STATIC_IV",
						"Static Initialization Vector (IV) detected. This makes encryption predictable.",
						DexForgeAnalysisSeverity.CRITICAL,
						cls
				));
			}
		}

		return findings;
	}
}
