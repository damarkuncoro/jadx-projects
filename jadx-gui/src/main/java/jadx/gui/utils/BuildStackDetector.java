package jadx.gui.utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jadx.api.ResourceFile;
import jadx.api.ResourcesLoader;
import jadx.core.dex.nodes.ClassNode;

public class BuildStackDetector {
	private static final Logger LOG = LoggerFactory.getLogger(BuildStackDetector.class);

	private static final List<String> FRAMEWORK_ORDER = List.of(
			"Native Android",
			"Flutter",
			"React Native",
			"Unity",
			"Cordova",
			"Capacitor",
			"Xamarin",
			"Kotlin runtime",
			"AndroidX / Jetpack",
			"Jetpack Compose",
			"Room",
			"Firebase",
			"Retrofit",
			"OkHttp",
			"Dagger / Hilt",
			"WebView / Hybrid",
			"R8 / ProGuard");

	private BuildStackDetector() {
	}

	public static BuildStackInfo analyzeExportedProject(File outputDir) {
		File sourcesDir = new File(outputDir, "sources");
		File resourcesDir = new File(outputDir, "resources");
		Map<String, String> resources = collectExportedResources(resourcesDir);
		Set<String> resourceNames = resources.keySet();
		Set<String> classNames = collectExportedClassNames(sourcesDir);
		BuildStackInfo info = analyze(resources::get, resourceNames, classNames);
		return info.withEvidencePrefix("resources/");
	}

	public static BuildStackInfo analyzeLoadedProject(List<ResourceFile> resources, List<ClassNode> classes) {
		Map<String, ResourceFile> resourceMap = resources.stream()
				.collect(Collectors.toMap(
						BuildStackDetector::getResourceName,
						res -> res,
						(first, second) -> first,
						LinkedHashMap::new));
		Set<String> classNames = classes.stream()
				.map(ClassNode::getClassInfo)
				.map(clsInfo -> clsInfo.getRawName().replace('.', '/'))
				.collect(Collectors.toCollection(LinkedHashSet::new));
		return analyze(name -> readResource(resourceMap.get(name)), resourceMap.keySet(), classNames);
	}

	private static BuildStackInfo analyze(
			ResourceReader reader,
			Set<String> resourceNames,
			Set<String> classNames) {
		Map<String, Object> buildMetadata = parseKotlinToolingMetadata(reader.read("kotlin-tooling-metadata.json"));
		Map<String, String> manifest = parseManifest(reader.read("AndroidManifest.xml"));
		Map<String, String> libraryVersions = collectLibraryVersions(reader, resourceNames);
		List<FrameworkDetection> frameworks = detectFrameworks(resourceNames, classNames, libraryVersions);
		Set<String> evidence = frameworks.stream()
				.flatMap(framework -> framework.getEvidence().stream())
				.collect(Collectors.toCollection(LinkedHashSet::new));
		if (!buildMetadata.isEmpty()) {
			evidence.add("kotlin-tooling-metadata.json");
		}
		if (!manifest.isEmpty()) {
			evidence.add("AndroidManifest.xml");
		}
		return new BuildStackInfo(summarizeBuildStack(buildMetadata, manifest, frameworks), buildMetadata, manifest,
				frameworks, libraryVersions, evidence.stream().sorted().collect(Collectors.toList()));
	}

	private static Map<String, String> collectExportedResources(File resourcesDir) {
		Map<String, String> resources = new LinkedHashMap<>();
		if (!resourcesDir.exists()) {
			return resources;
		}
		try (Stream<Path> walk = Files.walk(resourcesDir.toPath())) {
			walk.filter(Files::isRegularFile)
					.sorted()
					.forEach(path -> {
						String name = resourcesDir.toPath().relativize(path).toString().replace(File.separatorChar, '/');
						if (isTextResource(name)) {
							try {
								resources.put(name, Files.readString(path));
							} catch (Exception e) {
								LOG.debug("Failed to read build stack resource: {}", path, e);
							}
						} else {
							resources.put(name, "");
						}
					});
		} catch (Exception e) {
			LOG.warn("Failed to collect exported resources from {}", resourcesDir, e);
		}
		return resources;
	}

	private static Set<String> collectExportedClassNames(File sourcesDir) {
		if (!sourcesDir.exists()) {
			return Set.of();
		}
		try (Stream<Path> walk = Files.walk(sourcesDir.toPath())) {
			return walk.filter(Files::isRegularFile)
					.filter(path -> path.toString().endsWith(".java"))
					.map(path -> sourcesDir.toPath().relativize(path).toString())
					.map(path -> path.replace(File.separatorChar, '/'))
					.map(path -> path.substring(0, path.length() - ".java".length()))
					.collect(Collectors.toCollection(LinkedHashSet::new));
		} catch (Exception e) {
			LOG.warn("Failed to collect exported classes from {}", sourcesDir, e);
			return Set.of();
		}
	}

	private static boolean isTextResource(String name) {
		return name.equals("AndroidManifest.xml")
				|| name.equals("kotlin-tooling-metadata.json")
				|| name.endsWith(".version");
	}

	private static String readResource(ResourceFile resource) {
		if (resource == null) {
			return null;
		}
		try {
			return ResourcesLoader.decodeStream(resource, (size, is) -> new String(is.readAllBytes(), StandardCharsets.UTF_8));
		} catch (Exception e) {
			LOG.debug("Failed to read build stack resource: {}", resource.getOriginalName(), e);
			return null;
		}
	}

	private static Map<String, Object> parseKotlinToolingMetadata(String content) {
		Map<String, Object> metadata = new LinkedHashMap<>();
		if (content == null) {
			return metadata;
		}
		try {
			JsonObject root = JsonParser.parseString(content).getAsJsonObject();
			copyJsonString(root, metadata, "buildSystem");
			copyJsonString(root, metadata, "buildSystemVersion");
			copyJsonString(root, metadata, "buildPlugin");
			copyJsonString(root, metadata, "buildPluginVersion");
			if (root.has("projectTargets") && root.get("projectTargets").isJsonArray()
					&& root.getAsJsonArray("projectTargets").size() > 0) {
				JsonObject target = root.getAsJsonArray("projectTargets").get(0).getAsJsonObject();
				copyJsonString(target, metadata, "target");
				copyJsonString(target, metadata, "platformType");
				if (target.has("extras") && target.getAsJsonObject("extras").has("android")) {
					JsonObject android = target.getAsJsonObject("extras").getAsJsonObject("android");
					copyJsonString(android, metadata, "sourceCompatibility");
					copyJsonString(android, metadata, "targetCompatibility");
				}
			}
		} catch (Exception e) {
			LOG.warn("Failed to parse Kotlin tooling metadata", e);
		}
		return metadata;
	}

	private static void copyJsonString(JsonObject root, Map<String, Object> target, String key) {
		if (root.has(key) && root.get(key).isJsonPrimitive()) {
			target.put(key, root.get(key).getAsString());
		}
	}

	private static Map<String, String> parseManifest(String content) {
		Map<String, String> manifest = new LinkedHashMap<>();
		if (content == null) {
			return manifest;
		}
		putXmlAttr(content, manifest, "package", "package");
		putXmlAttr(content, manifest, "versionName", "android:versionName");
		putXmlAttr(content, manifest, "versionCode", "android:versionCode");
		putXmlAttr(content, manifest, "compileSdkVersion", "android:compileSdkVersion");
		putXmlAttr(content, manifest, "platformBuildVersionName", "platformBuildVersionName");
		putXmlAttr(content, manifest, "minSdkVersion", "android:minSdkVersion");
		putXmlAttr(content, manifest, "targetSdkVersion", "android:targetSdkVersion");
		putApplicationName(content, manifest);
		return manifest;
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

	private static Map<String, String> collectLibraryVersions(ResourceReader reader, Set<String> resourceNames) {
		Map<String, String> versions = new LinkedHashMap<>();
		resourceNames.stream()
				.filter(name -> name.startsWith("META-INF/"))
				.filter(name -> name.endsWith(".version"))
				.sorted()
				.forEach(name -> {
					String library = name.substring("META-INF/".length(), name.length() - ".version".length());
					String version = reader.read(name);
					if (version != null) {
						versions.put(library, version.trim());
					}
				});
		return versions;
	}

	private static List<FrameworkDetection> detectFrameworks(
			Set<String> resourceNames,
			Set<String> classNames,
			Map<String, String> libraryVersions) {
		List<FrameworkDetection> frameworks = new ArrayList<>();
		addFramework(frameworks, "Native Android", resourceNames.contains("AndroidManifest.xml"), "HIGH", List.of("AndroidManifest.xml"));
		addFramework(frameworks, "Flutter",
				resourceNames.stream().anyMatch(name -> name.startsWith("flutter_assets/"))
						|| containsResource(resourceNames, "libflutter.so"),
				"HIGH", matchingResources(resourceNames, name -> name.startsWith("flutter_assets/") || name.endsWith("/libflutter.so")));
		addFramework(frameworks, "React Native",
				resourceNames.contains("assets/index.android.bundle")
						|| classNames.contains("com/facebook/react/ReactActivity")
						|| classNames.contains("com/facebook/react/ReactNativeHost"),
				"HIGH", matchingEvidence(resourceNames, classNames, List.of("assets/index.android.bundle"),
						List.of("com/facebook/react/ReactActivity", "com/facebook/react/ReactNativeHost")));
		addFramework(frameworks, "Unity",
				containsResource(resourceNames, "libunity.so") || classNames.contains("com/unity3d/player/UnityPlayerActivity"),
				"HIGH",
				matchingEvidence(resourceNames, classNames, List.of("libunity.so"), List.of("com/unity3d/player/UnityPlayerActivity")));
		addFramework(frameworks, "Cordova",
				resourceNames.contains("assets/www/cordova.js") || classNames.contains("org/apache/cordova/CordovaActivity"),
				"HIGH", matchingEvidence(resourceNames, classNames, List.of("assets/www/cordova.js"),
						List.of("org/apache/cordova/CordovaActivity")));
		addFramework(frameworks, "Capacitor",
				resourceNames.contains("assets/capacitor.config.json") || classNames.contains("com/getcapacitor/BridgeActivity"),
				"HIGH", matchingEvidence(resourceNames, classNames, List.of("assets/capacitor.config.json"),
						List.of("com/getcapacitor/BridgeActivity")));
		addFramework(frameworks, "Xamarin",
				classNames.contains("mono/android/Runtime") || containsResource(resourceNames, "libmonodroid.so"),
				"HIGH", matchingEvidence(resourceNames, classNames, List.of("libmonodroid.so"), List.of("mono/android/Runtime")));
		addFramework(frameworks, "Kotlin runtime",
				resourceNames.contains("kotlin-tooling-metadata.json") || classNames.stream().anyMatch(cls -> cls.startsWith("kotlin/")),
				"MEDIUM", matchingEvidence(resourceNames, classNames, List.of("kotlin-tooling-metadata.json"), List.of("kotlin")));
		addFramework(frameworks, "AndroidX / Jetpack",
				resourceNames.contains("META-INF/androidx.core_core-ktx.version")
						|| classNames.stream().anyMatch(cls -> cls.startsWith("androidx/")),
				"HIGH",
				matchingEvidence(resourceNames, classNames, List.of("META-INF/androidx.core_core-ktx.version"), List.of("androidx")));
		addFramework(frameworks, "Jetpack Compose",
				hasLibrary(libraryVersions, "androidx.compose.")
						|| classNames.stream().anyMatch(cls -> cls.startsWith("androidx/compose/")),
				"HIGH", matchingLibraryAndClassEvidence(libraryVersions, classNames, "androidx.compose.", "androidx/compose"));
		addFramework(frameworks, "Room",
				hasLibrary(libraryVersions, "androidx.room") || classNames.stream().anyMatch(cls -> cls.startsWith("androidx/room/")),
				"HIGH", matchingLibraryAndClassEvidence(libraryVersions, classNames, "androidx.room", "androidx/room"));
		addFramework(frameworks, "Firebase",
				resourceNames.contains("google-services.json")
						|| hasLibrary(libraryVersions, "com.google.firebase")
						|| classNames.stream().anyMatch(cls -> cls.startsWith("com/google/firebase/")),
				"HIGH", matchingFirebaseEvidence(resourceNames, libraryVersions, classNames));
		addFramework(frameworks, "Retrofit",
				hasLibrary(libraryVersions, "com.squareup.retrofit")
						|| classNames.stream().anyMatch(cls -> cls.startsWith("retrofit2/")),
				"HIGH", matchingLibraryAndClassEvidence(libraryVersions, classNames, "com.squareup.retrofit", "retrofit2"));
		addFramework(frameworks, "OkHttp",
				hasLibrary(libraryVersions, "com.squareup.okhttp")
						|| classNames.stream().anyMatch(cls -> cls.startsWith("okhttp3/")),
				"HIGH", matchingLibraryAndClassEvidence(libraryVersions, classNames, "com.squareup.okhttp", "okhttp3"));
		addFramework(frameworks, "Dagger / Hilt",
				hasLibrary(libraryVersions, "com.google.dagger")
						|| classNames.stream().anyMatch(cls -> cls.startsWith("dagger/") || cls.startsWith("javax/inject/")),
				"HIGH", matchingDaggerEvidence(libraryVersions, classNames));
		addFramework(frameworks, "WebView / Hybrid",
				resourceNames.stream().anyMatch(name -> name.startsWith("assets/www/") || name.startsWith("assets/public/"))
						|| classNames.stream().anyMatch(cls -> cls.endsWith("/WebViewActivity") || cls.endsWith("/WebViewFragment")),
				"MEDIUM", matchingWebViewEvidence(resourceNames, classNames));
		addFramework(frameworks, "R8 / ProGuard",
				resourceNames.stream().anyMatch(name -> name.startsWith("META-INF/proguard/") || name.endsWith("proguard-project.txt"))
						|| classNames.stream().anyMatch(cls -> cls.startsWith("com/android/tools/r8/")),
				"MEDIUM", matchingR8Evidence(resourceNames, classNames));
		return frameworks.stream()
				.sorted(Comparator.comparingInt(framework -> FRAMEWORK_ORDER.indexOf(framework.getName())))
				.collect(Collectors.toList());
	}

	private static void addFramework(
			List<FrameworkDetection> frameworks,
			String name,
			boolean detected,
			String confidence,
			List<String> evidence) {
		frameworks.add(new FrameworkDetection(name, detected ? "DETECTED" : "NOT_DETECTED",
				detected ? confidence : "NONE", detected ? evidence : List.of()));
	}

	private static boolean containsResource(Set<String> resourceNames, String fileName) {
		return resourceNames.stream().anyMatch(name -> name.equals(fileName) || name.endsWith('/' + fileName));
	}

	private static List<String> matchingResources(Set<String> resourceNames, Predicate<String> predicate) {
		return resourceNames.stream()
				.filter(predicate)
				.sorted()
				.collect(Collectors.toList());
	}

	private static List<String> matchingEvidence(
			Set<String> resourceNames,
			Set<String> classNames,
			List<String> resourceEvidence,
			List<String> classEvidence) {
		List<String> evidence = new ArrayList<>();
		for (String resource : resourceEvidence) {
			if (resource.contains("/") && resourceNames.contains(resource)) {
				evidence.add(resource);
			} else if (!resource.contains("/")) {
				evidence.addAll(matchingResources(resourceNames, name -> name.equals(resource) || name.endsWith('/' + resource)));
			}
		}
		for (String cls : classEvidence) {
			if (classNames.contains(cls)) {
				evidence.add("class:" + cls);
			} else if (!cls.contains("/") && classNames.stream().anyMatch(name -> name.startsWith(cls + "/"))) {
				evidence.add("package:" + cls);
			}
		}
		return evidence.stream().distinct().sorted().collect(Collectors.toList());
	}

	private static boolean hasLibrary(Map<String, String> libraryVersions, String prefix) {
		return libraryVersions.keySet().stream().anyMatch(name -> name.startsWith(prefix));
	}

	private static List<String> matchingLibraryAndClassEvidence(
			Map<String, String> libraryVersions,
			Set<String> classNames,
			String libraryPrefix,
			String classPrefix) {
		List<String> evidence = new ArrayList<>();
		libraryVersions.keySet().stream()
				.filter(name -> name.startsWith(libraryPrefix))
				.map(name -> "META-INF/" + name + ".version")
				.forEach(evidence::add);
		if (classNames.stream().anyMatch(name -> name.startsWith(classPrefix + "/"))) {
			evidence.add("package:" + classPrefix);
		}
		return evidence.stream().distinct().sorted().collect(Collectors.toList());
	}

	private static List<String> matchingFirebaseEvidence(
			Set<String> resourceNames,
			Map<String, String> libraryVersions,
			Set<String> classNames) {
		List<String> evidence = new ArrayList<>(
				matchingLibraryAndClassEvidence(libraryVersions, classNames, "com.google.firebase", "com/google/firebase"));
		if (resourceNames.contains("google-services.json")) {
			evidence.add("google-services.json");
		}
		return evidence.stream().distinct().sorted().collect(Collectors.toList());
	}

	private static List<String> matchingDaggerEvidence(Map<String, String> libraryVersions, Set<String> classNames) {
		List<String> evidence =
				new ArrayList<>(matchingLibraryAndClassEvidence(libraryVersions, classNames, "com.google.dagger", "dagger"));
		if (classNames.stream().anyMatch(name -> name.startsWith("javax/inject/"))) {
			evidence.add("package:javax/inject");
		}
		return evidence.stream().distinct().sorted().collect(Collectors.toList());
	}

	private static List<String> matchingWebViewEvidence(Set<String> resourceNames, Set<String> classNames) {
		List<String> evidence = new ArrayList<>();
		resourceNames.stream()
				.filter(name -> name.startsWith("assets/www/") || name.startsWith("assets/public/"))
				.sorted()
				.forEach(evidence::add);
		classNames.stream()
				.filter(cls -> cls.endsWith("/WebViewActivity") || cls.endsWith("/WebViewFragment"))
				.map(cls -> "class:" + cls)
				.sorted()
				.forEach(evidence::add);
		return evidence.stream().distinct().sorted().collect(Collectors.toList());
	}

	private static List<String> matchingR8Evidence(Set<String> resourceNames, Set<String> classNames) {
		List<String> evidence = new ArrayList<>();
		resourceNames.stream()
				.filter(name -> name.startsWith("META-INF/proguard/") || name.endsWith("proguard-project.txt"))
				.sorted()
				.forEach(evidence::add);
		if (classNames.stream().anyMatch(name -> name.startsWith("com/android/tools/r8/"))) {
			evidence.add("package:com/android/tools/r8");
		}
		return evidence.stream().distinct().sorted().collect(Collectors.toList());
	}

	private static String summarizeBuildStack(
			Map<String, Object> buildMetadata,
			Map<String, String> manifest,
			List<FrameworkDetection> frameworks) {
		String primaryFramework = frameworks.stream()
				.filter(FrameworkDetection::isDetected)
				.map(FrameworkDetection::getName)
				.findFirst()
				.orElse("Unknown framework");
		String buildSystem = String.valueOf(buildMetadata.getOrDefault("buildSystem", "Unknown build system"));
		String buildPlugin = String.valueOf(buildMetadata.getOrDefault("buildPlugin", "unknown plugin"));
		String pluginVersion = String.valueOf(buildMetadata.getOrDefault("buildPluginVersion", "unknown version"));
		String compileSdk = manifest.getOrDefault("compileSdkVersion", "unknown");
		String targetSdk = manifest.getOrDefault("targetSdkVersion", "unknown");
		return String.format("%s app built with %s using %s %s (compileSdk %s, targetSdk %s)",
				primaryFramework, buildSystem, buildPlugin, pluginVersion, compileSdk, targetSdk);
	}

	private static String getResourceName(ResourceFile resource) {
		String name = resource.getOriginalName();
		int zipSeparator = name.indexOf(':');
		if (zipSeparator != -1 && zipSeparator + 1 < name.length()) {
			return name.substring(zipSeparator + 1);
		}
		return name;
	}

	public static boolean isImportantLibrary(String name) {
		return name.startsWith("androidx.core")
				|| name.startsWith("androidx.appcompat")
				|| name.startsWith("androidx.databinding")
				|| name.startsWith("androidx.navigation")
				|| name.startsWith("androidx.room")
				|| name.startsWith("androidx.compose")
				|| name.startsWith("androidx.camera")
				|| name.startsWith("kotlinx_coroutines")
				|| name.startsWith("com.google.android.material")
				|| name.startsWith("com.google.firebase")
				|| name.startsWith("com.squareup.retrofit")
				|| name.startsWith("com.squareup.okhttp")
				|| name.startsWith("com.google.dagger");
	}

	@FunctionalInterface
	private interface ResourceReader {
		String read(String name);
	}

	public static class BuildStackInfo {
		private final String summary;
		private final Map<String, Object> buildMetadata;
		private final Map<String, String> manifest;
		private final List<FrameworkDetection> frameworks;
		private final Map<String, String> libraryVersions;
		private final List<String> evidence;

		private BuildStackInfo(
				String summary,
				Map<String, Object> buildMetadata,
				Map<String, String> manifest,
				List<FrameworkDetection> frameworks,
				Map<String, String> libraryVersions,
				List<String> evidence) {
			this.summary = summary;
			this.buildMetadata = buildMetadata;
			this.manifest = manifest;
			this.frameworks = frameworks;
			this.libraryVersions = libraryVersions;
			this.evidence = evidence;
		}

		private BuildStackInfo withEvidencePrefix(String prefix) {
			List<FrameworkDetection> prefixedFrameworks = frameworks.stream()
					.map(framework -> framework.withEvidencePrefix(prefix))
					.collect(Collectors.toList());
			List<String> prefixedEvidence = evidence.stream()
					.map(item -> toExportedEvidence(item, prefix))
					.collect(Collectors.toList());
			return new BuildStackInfo(summary, buildMetadata, manifest, prefixedFrameworks, libraryVersions, prefixedEvidence);
		}

		public String getSummary() {
			return summary;
		}

		public Map<String, Object> getBuildMetadata() {
			return buildMetadata;
		}

		public Map<String, String> getManifest() {
			return manifest;
		}

		public List<FrameworkDetection> getFrameworks() {
			return frameworks;
		}

		public List<FrameworkDetection> getDetectedFrameworks() {
			return frameworks.stream()
					.filter(FrameworkDetection::isDetected)
					.collect(Collectors.toList());
		}

		public List<FrameworkDetection> getNotDetectedFrameworks() {
			return frameworks.stream()
					.filter(framework -> !framework.isDetected())
					.collect(Collectors.toList());
		}

		public Map<String, String> getLibraryVersions() {
			return libraryVersions;
		}

		public boolean isEmpty() {
			return buildMetadata.isEmpty()
					&& manifest.isEmpty()
					&& libraryVersions.isEmpty()
					&& getDetectedFrameworks().isEmpty();
		}

		public Map<String, Object> toMap() {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("summary", summary);
			map.put("buildMetadata", buildMetadata);
			map.put("manifest", manifest);
			map.put("frameworks", frameworks.stream().map(FrameworkDetection::toMap).collect(Collectors.toList()));
			map.put("libraryVersions", libraryVersions);
			map.put("evidence", evidence);
			return map;
		}
	}

	public static class FrameworkDetection {
		private final String name;
		private final String status;
		private final String confidence;
		private final List<String> evidence;

		private FrameworkDetection(String name, String status, String confidence, List<String> evidence) {
			this.name = name;
			this.status = status;
			this.confidence = confidence;
			this.evidence = evidence;
		}

		private FrameworkDetection withEvidencePrefix(String prefix) {
			List<String> prefixedEvidence = evidence.stream()
					.map(item -> toExportedEvidence(item, prefix))
					.collect(Collectors.toList());
			return new FrameworkDetection(name, status, confidence, prefixedEvidence);
		}

		public String getName() {
			return name;
		}

		public String getStatus() {
			return status;
		}

		public String getConfidence() {
			return confidence;
		}

		public List<String> getEvidence() {
			return evidence;
		}

		public boolean isDetected() {
			return "DETECTED".equals(status);
		}

		private Map<String, Object> toMap() {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("name", name);
			map.put("status", status);
			map.put("confidence", confidence);
			map.put("evidence", evidence);
			return map;
		}
	}

	private static String toExportedEvidence(String item, String resourcePrefix) {
		if (item.startsWith("class:")) {
			return "sources/" + item.substring("class:".length()) + ".java";
		}
		if (item.startsWith("package:")) {
			return "sources/" + item.substring("package:".length());
		}
		return resourcePrefix + item;
	}
}
