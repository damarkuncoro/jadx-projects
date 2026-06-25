package dexforge.core.diagnostic;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.service.intelligence.ProjectIntelligenceService;
import java.io.File;
import java.util.Map;
import java.util.List;

/**
 * REUSEABLE diagnostic application to analyze any Android APK.
 * Usage: java ApkAnalyzerApp <path_to_apk>
 */
public class ApkAnalyzerApp {
	public static void main(String[] args) {
		if (args.length == 0 || args[0].isEmpty()) {
			System.out.println("Usage: java ApkAnalyzerApp <path_to_apk>");
			return;
		}

		String apkPath = args[0];
		System.out.println("--- DexForge Deep Analysis ---");
		System.out.println("Target: " + apkPath);

		try {
			File apkFile = new File(apkPath);
			if (!apkFile.exists()) {
				System.err.println("Error: File not found: " + apkPath);
				return;
			}

			ApkLoader loader = new ApkLoader();
			loader.load(apkFile);

			ProjectIntelligenceService intelligence = new ProjectIntelligenceService(loader);
			Map<String, Object> insights = intelligence.getProjectInsights();

			System.out.println("\n[1] General Insights");
			System.out.println("Framework: " + insights.get("framework"));
			System.out.println("Packer: " + insights.get("packer"));
			System.out.println("Technologies: " + insights.get("technologies"));
			System.out.println("Deobfuscation: " + insights.get("deobfuscationStats"));

			// Show auto-detected deobfuscators if any
			if (insights.get("autoDetectedDeobf") != null) {
				System.out.println("Auto-detected Deobf Methods: " + insights.get("autoDetectedDeobf"));
			}

			System.out.println("\n[2] Security Analysis");
			System.out.println("Security Score: " + insights.get("securityScore"));
			List<?> vulnerabilities = (List<?>) insights.get("vulnerabilities");
			System.out.println("Potential Issues: " + vulnerabilities.size());
			vulnerabilities.stream().limit(10).forEach(v -> System.out.println(" - " + v));

			System.out.println("\n[3] PII Leak Detection (Global Taint)");
			List<?> leaks = (List<?>) insights.get("piiLeaks");
			if (leaks != null && !leaks.isEmpty()) {
				System.out.println("Found " + leaks.size() + " potential PII leaks:");
				leaks.forEach(l -> System.out.println(" [!] " + l));
			} else {
				System.out.println("No PII leaks detected.");
			}

			System.out.println("\n[4] Hot Methods (Highest Fan-in)");
			List<?> hotMethods = (List<?>) insights.get("hotMethods");
			if (hotMethods != null) {
				hotMethods.stream().limit(10).forEach(m -> System.out.println(" - " + m));
			}

		} catch (Exception e) {
			System.err.println("Analysis failed: " + e.getMessage());
		}
	}
}
