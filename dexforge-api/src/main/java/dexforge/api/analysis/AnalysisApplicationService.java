package dexforge.api.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dexforge.api.analysis.impl.HardcodedSecretAnalyzer;
import dexforge.api.analysis.impl.InsecureCryptoAnalyzer;
import dexforge.api.analysis.impl.ManifestVulnerabilityAnalyzer;
import dexforge.api.analysis.impl.SecurityVulnerabilityAnalyzer;
import dexforge.api.core.DexForgeProject;

/**
 * Orchestrates deep analysis across multiple analyzers.
 */
public final class AnalysisApplicationService {
	private static final Map<String, String> DEFAULT_QUICK_FIXES = Map.of(
			"MANIFEST_ALLOW_BACKUP",
			"Set android:allowBackup=\"false\" in AndroidManifest.xml to prevent data extraction.",
			"CRYPTO_WEAK_ALGORITHM",
			"Replace weak algorithm with 'AES/GCM/NoPadding' for better security.",
			"CRYPTO_STATIC_IV",
			"Generate a fresh random IV for each encryption operation and store it alongside the ciphertext.",
			"SECURITY_HARDCODED_SECRET",
			"Move this secret to a secure backend or use Android Keystore/EncryptedSharedPreferences.",
			"MANIFEST_DEBUGGABLE",
			"Remove android:debuggable=\"true\" before releasing to production.",
			"MANIFEST_EXPORTED_COMPONENT",
			"Protect exported components with explicit permissions or set android:exported=\"false\" when external access is not required.",
			"SECURITY_SSL_BYPASS",
			"Ensure SSL certificates are validated properly. Do not use empty TrustManager implementations.",
			"SECURITY_ROOT_DETECTION",
			"Consider using hardware-backed security features or Play Integrity API instead of simple 'su' checks.");

	private final List<DexForgeAnalyzer> analyzers = new ArrayList<>();

	public AnalysisApplicationService() {
		this(List.of(
				new HardcodedSecretAnalyzer(),
				new InsecureCryptoAnalyzer(),
				new ManifestVulnerabilityAnalyzer(),
				new SecurityVulnerabilityAnalyzer()));
	}

	public AnalysisApplicationService(List<DexForgeAnalyzer> analyzers) {
		for (DexForgeAnalyzer analyzer : Objects.requireNonNull(analyzers)) {
			registerAnalyzer(analyzer);
		}
	}

	public void registerAnalyzer(DexForgeAnalyzer analyzer) {
		analyzers.add(Objects.requireNonNull(analyzer));
	}

	public List<DexForgeAnalyzer> getAnalyzers() {
		return Collections.unmodifiableList(analyzers);
	}

	public List<DexForgeFinding> runAllAnalyses(DexForgeProject project) {
		List<DexForgeFinding> allFindings = new ArrayList<>();
		for (DexForgeAnalyzer analyzer : List.copyOf(analyzers)) {
			List<DexForgeFinding> findings = analyzer.analyze(project);
			if (findings == null) {
				continue;
			}
			for (DexForgeFinding f : findings) {
				if (f == null) {
					continue;
				}
				enrichWithQuickFix(f);
				allFindings.add(f);
			}
		}
		return Collections.unmodifiableList(allFindings);
	}

	private void enrichWithQuickFix(DexForgeFinding finding) {
		if (finding == null || finding.getSuggestedFix() != null) {
			return;
		}
		String suggestedFix = DEFAULT_QUICK_FIXES.get(finding.getType());
		if (suggestedFix != null) {
			finding.setSuggestedFix(suggestedFix);
		}
	}
}
