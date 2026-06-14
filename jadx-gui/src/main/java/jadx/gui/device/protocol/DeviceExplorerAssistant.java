package jadx.gui.device.protocol;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DeviceExplorerAssistant {
	private static final Logger LOG = LoggerFactory.getLogger(DeviceExplorerAssistant.class);

	private static final Pattern URL_PATTERN = Pattern.compile(
			"https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?::[0-9]+)?(?:/[a-zA-Z0-9_.-]*)*");

	private static final Pattern BASE64_PATTERN = Pattern.compile(
			"\"([A-Za-z0-9+/]{16,}={0,2})\"");

	private static final Pattern STRINGS_XML_PATTERN = Pattern.compile(
			"<string name=\"([a-zA-Z0-9_]+)\">([^<]+)</string>");

	// JCE and cryptographic signatures
	private static final String[] CRYPTO_KEYWORDS = {
			"javax.crypto", "Cipher", "SecretKeySpec", "IvParameterSpec", "MessageDigest",
			"Signature", "Mac", "KeyGenerator", "AES", "DES", "RSA", "Blowfish", "SHA-256", "MD5"
	};

	public static void runAnalysis(File outputDir, File reportFile) {
		LOG.info("Starting JADX Device Explorer Security Assistant scanning...");
		long startTime = System.currentTimeMillis();

		File sourcesDir = new File(outputDir, "sources");
		File resourcesDir = new File(outputDir, "resources");

		Map<String, Object> assistantReport = new LinkedHashMap<>();
		assistantReport.put("packageName", outputDir.getName());
		assistantReport.put("generatedAt", java.time.Instant.now().toString());

		// 1. Obfuscation Summary
		Map<String, Object> obfuscationSummary = runObfuscationScan(sourcesDir);
		assistantReport.put("obfuscationSummary", obfuscationSummary);

		// 2. Build stack fingerprint
		Map<String, Object> buildStack = runBuildStackScan(outputDir, sourcesDir, resourcesDir);
		assistantReport.put("buildStack", buildStack);

		// Scan variables
		List<Map<String, String>> endpoints = new ArrayList<>();
		Map<String, Object> firebaseConfig = new LinkedHashMap<>();
		List<Map<String, Object>> cryptoUsage = new ArrayList<>();
		List<Map<String, String>> base64Strings = new ArrayList<>();
		List<Map<String, String>> failedMethods = new ArrayList<>();

		// 3. Scan resources for Firebase configurations
		scanResources(resourcesDir, firebaseConfig);

		// 4. Scan Java files
		scanJavaSources(sourcesDir, endpoints, cryptoUsage, base64Strings, failedMethods);

		assistantReport.put("endpoints", endpoints);
		assistantReport.put("firebaseConfig", firebaseConfig);
		assistantReport.put("cryptoUsage", cryptoUsage);
		assistantReport.put("base64Strings", base64Strings);
		assistantReport.put("failedMethods", failedMethods);

		long duration = System.currentTimeMillis() - startTime;
		assistantReport.put("scanDurationMs", duration);

		// Save report to file
		try {
			File parent = reportFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try (FileWriter writer = new FileWriter(reportFile)) {
				gson.toJson(assistantReport, writer);
			}
			LOG.info("Security Assistant analysis finished in {} ms. Saved to: {}", duration, reportFile.getAbsolutePath());
		} catch (Exception e) {
			LOG.error("Failed to write Security Assistant report", e);
		}
	}

	private static Map<String, Object> runObfuscationScan(File sourcesDir) {
		Map<String, Object> summary = new LinkedHashMap<>();
		if (!sourcesDir.exists()) {
			summary.put("totalClasses", 0);
			summary.put("obfuscatedClasses", 0);
			summary.put("obfuscationPercentage", 0.0);
			return summary;
		}

		long totalClasses = 0;
		long obfuscatedClasses = 0;

		try (Stream<Path> walk = Files.walk(sourcesDir.toPath())) {
			List<Path> files = walk.filter(Files::isRegularFile)
					.filter(p -> p.toString().endsWith(".java"))
					.toList();

			totalClasses = files.size();

			for (Path file : files) {
				String filename = file.getFileName().toString();
				String classname = filename.substring(0, filename.length() - 5); // remove .java

				boolean isObfuscated = false;
				// Obfuscated class name check (length <= 2, e.g., a.java, ab.java)
				if (classname.length() <= 2) {
					isObfuscated = true;
				} else {
					// Obfuscated package name check (e.g. sources/a/b/c/...)
					Path relative = sourcesDir.toPath().relativize(file.getParent());
					for (Path part : relative) {
						if (part.toString().length() <= 2) {
							isObfuscated = true;
							break;
						}
					}
				}

				if (isObfuscated) {
					obfuscatedClasses++;
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to execute obfuscation scan", e);
		}

		double percentage = totalClasses > 0 ? (obfuscatedClasses * 100.0 / totalClasses) : 0.0;
		// Round to 1 decimal place
		percentage = Math.round(percentage * 10.0) / 10.0;

		summary.put("totalClasses", totalClasses);
		summary.put("obfuscatedClasses", obfuscatedClasses);
		summary.put("obfuscationPercentage", percentage);
		return summary;
	}

	private static Map<String, Object> runBuildStackScan(File outputDir, File sourcesDir, File resourcesDir) {
		Map<String, Object> buildStack = new LinkedHashMap<>();
		Map<String, Object> buildMetadata = new LinkedHashMap<>();
		Map<String, String> manifest = new LinkedHashMap<>();
		Map<String, String> libraryVersions = new LinkedHashMap<>();
		List<Map<String, Object>> frameworks = new ArrayList<>();
		Set<String> evidence = new HashSet<>();

		parseKotlinToolingMetadata(new File(resourcesDir, "kotlin-tooling-metadata.json"), buildMetadata, evidence);
		parseManifest(new File(resourcesDir, "AndroidManifest.xml"), manifest, evidence);
		collectLibraryVersions(new File(resourcesDir, "META-INF"), libraryVersions);
		detectFrameworks(outputDir, sourcesDir, resourcesDir, frameworks, evidence);

		buildStack.put("summary", summarizeBuildStack(buildMetadata, manifest, frameworks));
		buildStack.put("buildMetadata", buildMetadata);
		buildStack.put("manifest", manifest);
		buildStack.put("frameworks", frameworks);
		buildStack.put("libraryVersions", libraryVersions);
		buildStack.put("evidence", evidence.stream().sorted().toList());
		return buildStack;
	}

	private static void parseKotlinToolingMetadata(File file, Map<String, Object> buildMetadata, Set<String> evidence) {
		if (!file.exists()) {
			return;
		}
		try (FileReader reader = new FileReader(file)) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			copyJsonString(root, buildMetadata, "buildSystem");
			copyJsonString(root, buildMetadata, "buildSystemVersion");
			copyJsonString(root, buildMetadata, "buildPlugin");
			copyJsonString(root, buildMetadata, "buildPluginVersion");
			if (root.has("projectTargets") && root.get("projectTargets").isJsonArray()
					&& root.getAsJsonArray("projectTargets").size() > 0) {
				JsonObject target = root.getAsJsonArray("projectTargets").get(0).getAsJsonObject();
				copyJsonString(target, buildMetadata, "target");
				copyJsonString(target, buildMetadata, "platformType");
				if (target.has("extras")
						&& target.getAsJsonObject("extras").has("android")) {
					JsonObject android = target.getAsJsonObject("extras").getAsJsonObject("android");
					copyJsonString(android, buildMetadata, "sourceCompatibility");
					copyJsonString(android, buildMetadata, "targetCompatibility");
				}
			}
			evidence.add("resources/kotlin-tooling-metadata.json");
		} catch (Exception e) {
			LOG.warn("Failed to parse Kotlin tooling metadata: {}", file, e);
		}
	}

	private static void copyJsonString(JsonObject root, Map<String, Object> target, String key) {
		if (root.has(key) && root.get(key).isJsonPrimitive()) {
			target.put(key, root.get(key).getAsString());
		}
	}

	private static void parseManifest(File manifestFile, Map<String, String> manifest, Set<String> evidence) {
		if (!manifestFile.exists()) {
			return;
		}
		try {
			String content = Files.readString(manifestFile.toPath());
			putXmlAttr(content, manifest, "package", "package");
			putXmlAttr(content, manifest, "versionName", "android:versionName");
			putXmlAttr(content, manifest, "versionCode", "android:versionCode");
			putXmlAttr(content, manifest, "compileSdkVersion", "android:compileSdkVersion");
			putXmlAttr(content, manifest, "platformBuildVersionName", "platformBuildVersionName");
			putXmlAttr(content, manifest, "minSdkVersion", "android:minSdkVersion");
			putXmlAttr(content, manifest, "targetSdkVersion", "android:targetSdkVersion");
			putApplicationName(content, manifest);
			evidence.add("resources/AndroidManifest.xml");
		} catch (Exception e) {
			LOG.warn("Failed to parse AndroidManifest.xml: {}", manifestFile, e);
		}
	}

	private static void putXmlAttr(String content, Map<String, String> target, String key, String attrName) {
		Pattern pattern = Pattern.compile(Pattern.quote(attrName) + "=\"([^\"]+)\"");
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			target.put(key, matcher.group(1));
		}
	}

	private static void putApplicationName(String content, Map<String, String> manifest) {
		Pattern pattern = Pattern.compile("<application\\b[^>]*\\bandroid:name=\"([^\"]+)\"", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			manifest.put("applicationName", matcher.group(1));
		}
	}

	private static void collectLibraryVersions(File metaInfDir, Map<String, String> libraryVersions) {
		if (!metaInfDir.exists()) {
			return;
		}
		try (Stream<Path> walk = Files.walk(metaInfDir.toPath(), 1)) {
			walk.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(".version"))
					.sorted()
					.forEach(path -> {
						try {
							String fileName = path.getFileName().toString();
							String libraryName = fileName.substring(0, fileName.length() - ".version".length());
							libraryVersions.put(libraryName, Files.readString(path).trim());
						} catch (Exception e) {
							LOG.debug("Failed to read library version file: {}", path, e);
						}
					});
		} catch (Exception e) {
			LOG.warn("Failed to collect library versions from {}", metaInfDir, e);
		}
	}

	private static void detectFrameworks(
			File outputDir,
			File sourcesDir,
			File resourcesDir,
			List<Map<String, Object>> frameworks,
			Set<String> evidence) {
		boolean nativeAndroid = resourcesDir.exists() && new File(resourcesDir, "AndroidManifest.xml").exists();
		if (nativeAndroid) {
			List<String> nativeEvidence = new ArrayList<>();
			nativeEvidence.add("resources/AndroidManifest.xml");
			if (new File(resourcesDir, "res/layout").exists()) {
				nativeEvidence.add("resources/res/layout");
			}
			if (new File(resourcesDir, "kotlin-tooling-metadata.json").exists()) {
				nativeEvidence.add("resources/kotlin-tooling-metadata.json");
			}
			addFramework(frameworks, "Native Android", "HIGH", nativeEvidence);
			evidence.addAll(nativeEvidence);
		}

		addFrameworkIfPresent(frameworks, "Flutter", outputDir, List.of(
				"resources/flutter_assets",
				"resources/lib/arm64-v8a/libflutter.so",
				"resources/lib/armeabi-v7a/libflutter.so"));
		addFrameworkIfPresent(frameworks, "React Native", outputDir, List.of(
				"resources/assets/index.android.bundle",
				"sources/com/facebook/react/ReactActivity.java",
				"sources/com/facebook/react/ReactNativeHost.java"));
		addFrameworkIfPresent(frameworks, "Unity", outputDir, List.of(
				"resources/lib/arm64-v8a/libunity.so",
				"sources/com/unity3d/player/UnityPlayerActivity.java"));
		addFrameworkIfPresent(frameworks, "Cordova", outputDir, List.of(
				"resources/assets/www/cordova.js",
				"sources/org/apache/cordova/CordovaActivity.java"));
		addFrameworkIfPresent(frameworks, "Capacitor", outputDir, List.of(
				"resources/assets/capacitor.config.json",
				"sources/com/getcapacitor/BridgeActivity.java"));
		addFrameworkIfPresent(frameworks, "Xamarin", outputDir, List.of(
				"sources/mono/android/Runtime.java",
				"resources/lib/arm64-v8a/libmonodroid.so"));

		if (sourcesDir.exists()) {
			if (new File(sourcesDir, "kotlin").exists()) {
				addFramework(frameworks, "Kotlin runtime", "MEDIUM", List.of("sources/kotlin"));
			}
			if (new File(resourcesDir, "META-INF/androidx.core_core-ktx.version").exists()) {
				addFramework(frameworks, "AndroidX / Jetpack", "HIGH", List.of("resources/META-INF/androidx.core_core-ktx.version"));
			}
		}
	}

	private static void addFrameworkIfPresent(
			List<Map<String, Object>> frameworks,
			String name,
			File outputDir,
			List<String> relativeEvidence) {
		List<String> found = relativeEvidence.stream()
				.filter(path -> new File(outputDir, path).exists())
				.toList();
		if (!found.isEmpty()) {
			addFramework(frameworks, name, "HIGH", found);
		}
	}

	private static void addFramework(
			List<Map<String, Object>> frameworks,
			String name,
			String confidence,
			List<String> evidence) {
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("name", name);
		item.put("confidence", confidence);
		item.put("evidence", evidence);
		frameworks.add(item);
	}

	private static String summarizeBuildStack(
			Map<String, Object> buildMetadata,
			Map<String, String> manifest,
			List<Map<String, Object>> frameworks) {
		String buildSystem = (String) buildMetadata.getOrDefault("buildSystem", "Unknown build system");
		String buildPlugin = (String) buildMetadata.getOrDefault("buildPlugin", "unknown plugin");
		String pluginVersion = (String) buildMetadata.getOrDefault("buildPluginVersion", "unknown version");
		String compileSdk = manifest.getOrDefault("compileSdkVersion", "unknown");
		String targetSdk = manifest.getOrDefault("targetSdkVersion", "unknown");
		String primaryFramework = frameworks.isEmpty()
				? "unknown framework"
				: String.valueOf(frameworks.get(0).get("name"));
		return String.format("%s app built with %s using %s %s (compileSdk %s, targetSdk %s)",
				primaryFramework, buildSystem, buildPlugin, pluginVersion, compileSdk, targetSdk);
	}

	private static void scanResources(File resourcesDir, Map<String, Object> firebaseConfig) {
		if (!resourcesDir.exists()) {
			return;
		}

		// Look for google-services.json
		try (Stream<Path> walk = Files.walk(resourcesDir.toPath())) {
			walk.filter(Files::isRegularFile)
					.forEach(p -> {
						String filename = p.getFileName().toString();
						if (filename.equals("google-services.json")) {
							parseGoogleServicesJson(p.toFile(), firebaseConfig);
						} else if (filename.equals("strings.xml")) {
							parseStringsXml(p.toFile(), firebaseConfig);
						}
					});
		} catch (Exception e) {
			LOG.error("Failed to scan resources", e);
		}
	}

	private static void parseGoogleServicesJson(File file, Map<String, Object> config) {
		try (FileReader reader = new FileReader(file)) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			if (root.has("project_info")) {
				JsonObject projInfo = root.getAsJsonObject("project_info");
				if (projInfo.has("project_id")) {
					config.put("projectId", projInfo.get("project_id").getAsString());
				}
				if (projInfo.has("firebase_url")) {
					config.put("databaseUrl", projInfo.get("firebase_url").getAsString());
				}
				if (projInfo.has("storage_bucket")) {
					config.put("storageBucket", projInfo.get("storage_bucket").getAsString());
				}
			}
			if (root.has("client")) {
				JsonObject client = root.getAsJsonArray("client").get(0).getAsJsonObject();
				if (client.has("client_info")) {
					JsonObject clientInfo = client.getAsJsonObject("client_info");
					if (clientInfo.has("mobilesdk_app_id")) {
						config.put("appId", clientInfo.get("mobilesdk_app_id").getAsString());
					}
				}
				if (client.has("api_key")) {
					JsonObject apiKeyObj = client.getAsJsonArray("api_key").get(0).getAsJsonObject();
					if (apiKeyObj.has("current_key")) {
						config.put("apiKey", apiKeyObj.get("current_key").getAsString());
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to parse google-services.json", e);
		}
	}

	private static void parseStringsXml(File file, Map<String, Object> config) {
		try {
			String content = Files.readString(file.toPath());
			Matcher m = STRINGS_XML_PATTERN.matcher(content);
			while (m.find()) {
				String key = m.group(1);
				String val = m.group(2);
				switch (key) {
					case "google_api_key":
						config.put("apiKey", val);
						break;
					case "google_app_id":
						config.put("appId", val);
						break;
					case "firebase_database_url":
						config.put("databaseUrl", val);
						break;
					case "google_storage_bucket":
						config.put("storageBucket", val);
						break;
					case "gcm_defaultSenderId":
						config.put("gcmSenderId", val);
						break;
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to parse strings.xml", e);
		}
	}

	private static void scanJavaSources(
			File sourcesDir,
			List<Map<String, String>> endpoints,
			List<Map<String, Object>> cryptoUsage,
			List<Map<String, String>> base64Strings,
			List<Map<String, String>> failedMethods) {
		if (!sourcesDir.exists()) {
			return;
		}

		// Avoid duplicates
		Set<String> processedUrls = new HashSet<>();

		try (Stream<Path> walk = Files.walk(sourcesDir.toPath())) {
			walk.filter(Files::isRegularFile)
					.filter(p -> p.toString().endsWith(".java"))
					.forEach(p -> {
						try {
							String relativePath = sourcesDir.toPath().relativize(p).toString();
							List<String> lines = Files.readAllLines(p);

							// Crypto findings for this file
							Set<String> cryptoFindings = new HashSet<>();

							for (int i = 0; i < lines.size(); i++) {
								String line = lines.get(i);
								int lineNum = i + 1;

								// 1. Endpoint Scanning
								Matcher urlMatcher = URL_PATTERN.matcher(line);
								while (urlMatcher.find()) {
									String url = urlMatcher.group();
									if (isInterestingUrl(url) && processedUrls.add(url + "|" + relativePath)) {
										Map<String, String> item = new LinkedHashMap<>();
										item.put("url", url);
										item.put("file", relativePath);
										item.put("line", String.valueOf(lineNum));
										endpoints.add(item);
									}
								}

								// 2. Base64/High Entropy Scanner
								Matcher b64Matcher = BASE64_PATTERN.matcher(line);
								while (b64Matcher.find()) {
									String base64 = b64Matcher.group(1);
									if (isProbablyBase64(base64)) {
										Map<String, String> item = new LinkedHashMap<>();
										item.put("string", base64);
										item.put("file", relativePath);
										item.put("line", String.valueOf(lineNum));
										base64Strings.add(item);
									}
								}

								// 3. Crypto Keywords scan
								for (String keyword : CRYPTO_KEYWORDS) {
									if (line.contains(keyword)) {
										cryptoFindings.add(keyword);
									}
								}

								// 4. Failed Methods Scanner (JADX errors / skips)
								if (line.contains("JADX WARNING:") || line.contains("JADX ERROR:")) {
									Map<String, String> item = new LinkedHashMap<>();
									item.put("file", relativePath);
									item.put("line", String.valueOf(lineNum));
									item.put("error", line.trim());
									// Attempt to find surrounding method signature or name
									String context = findContextMethod(lines, i);
									item.put("context", context);
									failedMethods.add(item);
								}
							}

							if (!cryptoFindings.isEmpty()) {
								Map<String, Object> item = new LinkedHashMap<>();
								item.put("file", relativePath);
								item.put("findings", new ArrayList<>(cryptoFindings));
								cryptoUsage.add(item);
							}

						} catch (Exception ex) {
							// Skip single file parsing errors
						}
					});
		} catch (Exception e) {
			LOG.error("Failed to scan Java source files", e);
		}
	}

	private static boolean isInterestingUrl(String url) {
		// Ignore standard XML namespaces
		if (url.contains("schemas.android.com") || url.contains("w3.org") || url.contains("adobe.com")) {
			return false;
		}
		// Ignore local development hosts
		if (url.startsWith("http://localhost") || url.startsWith("http://127.0.0.1")) {
			return false;
		}
		return true;
	}

	private static boolean isProbablyBase64(String s) {
		if (s.length() < 16) {
			return false;
		}
		if (s.contains("/")) {
			String[] parts = s.split("/");
			if (parts.length == 3) {
				return false;
			}
		}
		// Ensure it only contains valid base64 chars
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			boolean valid = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
					|| c == '+' || c == '/' || c == '=';
			if (!valid) {
				return false;
			}
		}
		// Check for some variation in characters (exclude repeat strings like "AAAAAAAAAAAAA...")
		Set<Character> uniq = new HashSet<>();
		for (char c : s.toCharArray()) {
			uniq.add(c);
		}
		return uniq.size() > 4;
	}

	private static String findContextMethod(List<String> lines, int warningIndex) {
		// Look upwards up to 15 lines to identify method signature
		for (int i = warningIndex; i >= Math.max(0, warningIndex - 15); i--) {
			String line = lines.get(i).trim();
			if (line.contains("public ") || line.contains("private ") || line.contains("protected ")
					|| (line.contains("void ") && line.contains("("))) {
				if (!line.startsWith("*") && !line.startsWith("//")) {
					return line;
				}
			}
		}
		return "Unknown Method Context";
	}
}
