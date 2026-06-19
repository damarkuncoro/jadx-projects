package dexforge.plugins.detector.ad;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import dexforge.commons.app.DexforgeCommonFiles;

public class AdDetector {
	private static final Logger LOG = LoggerFactory.getLogger(AdDetector.class);

	private static List<AdNetwork> loadedNetworks = null;

	public static synchronized List<AdNetwork> getLoadedNetworks() {
		if (loadedNetworks != null) {
			return loadedNetworks;
		}
		loadedNetworks = loadRules();
		return loadedNetworks;
	}

	private static List<AdNetwork> loadRules() {
		List<AdNetwork> list = new ArrayList<>();
		Gson gson = new Gson();

		// 1. Try loading from user config dir
		try {
			Path userRules = DexforgeCommonFiles.getConfigDir().resolve("ad_rules.json");
			if (Files.exists(userRules)) {
				try (Reader reader = Files.newBufferedReader(userRules, StandardCharsets.UTF_8)) {
					RulesContainer container = gson.fromJson(reader, RulesContainer.class);
					if (container != null && container.getRules() != null) {
						list.addAll(container.getRules());
						LOG.info("Loaded {} tracker/ad rules from {}", list.size(), userRules);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to load ad rules from user config directory", e);
		}

		// 2. If nothing loaded or file did not exist, load from classpath resource
		if (list.isEmpty()) {
			try (InputStream is = AdDetector.class.getResourceAsStream("/ad_rules.json")) {
				if (is != null) {
					try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
						RulesContainer container = gson.fromJson(reader, RulesContainer.class);
						if (container != null && container.getRules() != null) {
							list.addAll(container.getRules());
							LOG.info("Loaded {} tracker/ad rules from classpath resource /ad_rules.json", list.size());
						}
					}
				}
			} catch (Exception e) {
				LOG.error("Failed to load ad rules from classpath resource", e);
			}
		}

		// 3. Fallback defaults if still empty (so it never crashes)
		if (list.isEmpty()) {
			LOG.warn("No tracker/ad rules found. Using default fallback rules.");
			list.add(new AdNetwork("Google AdMob", "Ads", Arrays.asList("com.google.ads", "com.google.android.gms.ads")));
			list.add(new AdNetwork("Facebook Audience Network", "Ads", Arrays.asList("com.facebook.ads")));
			list.add(new AdNetwork("Firebase Analytics", "Analytics",
					Arrays.asList("com.google.firebase.analytics", "com.google.android.gms.measurement")));
		}
		return list;
	}

	public static List<AdFinding> detectAds(JadxDecompiler decompiler) {
		List<AdFinding> findings = new ArrayList<>();
		for (AdNetwork network : getLoadedNetworks()) {
			AdFinding finding = new AdFinding(network);
			findings.add(finding);
		}

		// Check classes (and extract packages from class names)
		Set<String> foundPackages = new HashSet<>();
		for (JavaClass cls : decompiler.getClasses()) {
			checkClass(cls, findings, foundPackages);
		}

		// Remove empty findings
		List<AdFinding> result = new ArrayList<>();
		for (AdFinding finding : findings) {
			if (!finding.isEmpty()) {
				result.add(finding);
			}
		}
		return result;
	}

	private static void checkClass(JavaClass cls, List<AdFinding> findings, Set<String> foundPackages) {
		String clsName = cls.getFullName();
		// Extract package name (everything before last dot)
		int lastDotIndex = clsName.lastIndexOf('.');
		String pkgName = (lastDotIndex > 0) ? clsName.substring(0, lastDotIndex) : "";

		for (AdFinding finding : findings) {
			for (String prefix : finding.getNetwork().getPackagePrefixes()) {
				if (clsName.startsWith(prefix)) {
					// Add package only once
					if (!foundPackages.contains(pkgName)) {
						finding.addPackage(pkgName);
						foundPackages.add(pkgName);
					}
					finding.addClass(clsName);
					break;
				}
			}
		}
	}

	private static class RulesContainer {
		private List<AdNetwork> rules;

		public List<AdNetwork> getRules() {
			return rules;
		}

		public void setRules(List<AdNetwork> rules) {
			this.rules = rules;
		}
	}
}
