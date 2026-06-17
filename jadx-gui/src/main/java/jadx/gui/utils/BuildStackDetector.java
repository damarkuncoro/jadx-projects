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
import java.util.function.Function;
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
			"Tauri",
			"Cocos2d",
			"Unreal Engine",
			"Kotlin runtime",
			"AndroidX / Jetpack",
			"Jetpack Compose",
			"Room",
			"Firebase",
			"Retrofit",
			"OkHttp",
			"Dagger / Hilt",
			"Koin",
			"RxJava",
			"Glide",
			"Lottie",
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
		RuleContext ctx = new RuleContext(resourceNames, classNames, libraryVersions);
		List<FrameworkDetection> frameworks = new ArrayList<>();
		for (FrameworkRule rule : RULES) {
			boolean detected = rule.detect(ctx);
			frameworks.add(new FrameworkDetection(
					rule.getName(),
					detected ? "DETECTED" : "NOT_DETECTED",
					detected ? rule.getConfidence() : "NONE",
					detected ? rule.getEvidence(ctx) : List.of()
			));
		}
		return frameworks.stream()
				.sorted(Comparator.comparingInt(framework -> FRAMEWORK_ORDER.indexOf(framework.getName())))
				.collect(Collectors.toList());
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
			} else if (classNames.stream().anyMatch(name -> name.startsWith(cls + "/"))) {
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
				|| name.startsWith("com.google.dagger")
				|| name.startsWith("org.insert-koin")
				|| name.startsWith("com.airbnb.android")
				|| name.startsWith("io.reactivex")
				|| name.startsWith("com.github.bumptech.glide");
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

	private static class RuleContext {
		private final Set<String> resourceNames;
		private final Set<String> classNames;
		private final Map<String, String> libraryVersions;

		public RuleContext(Set<String> resourceNames, Set<String> classNames, Map<String, String> libraryVersions) {
			this.resourceNames = resourceNames;
			this.classNames = classNames;
			this.libraryVersions = libraryVersions;
		}

		public Set<String> getResourceNames() {
			return resourceNames;
		}

		public Set<String> getClassNames() {
			return classNames;
		}

		public Map<String, String> getLibraryVersions() {
			return libraryVersions;
		}
	}

	private interface FrameworkRule {
		String getName();
		String getConfidence();
		boolean detect(RuleContext ctx);
		List<String> getEvidence(RuleContext ctx);
	}

	private static class DefaultFrameworkRule implements FrameworkRule {
		private final String name;
		private final String confidence;
		private final Predicate<RuleContext> detectFunc;
		private final Function<RuleContext, List<String>> evidenceFunc;

		public DefaultFrameworkRule(String name, String confidence, Predicate<RuleContext> detectFunc, Function<RuleContext, List<String>> evidenceFunc) {
			this.name = name;
			this.confidence = confidence;
			this.detectFunc = detectFunc;
			this.evidenceFunc = evidenceFunc;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getConfidence() {
			return confidence;
		}

		@Override
		public boolean detect(RuleContext ctx) {
			return detectFunc.test(ctx);
		}

		@Override
		public List<String> getEvidence(RuleContext ctx) {
			return evidenceFunc.apply(ctx);
		}
	}

	private static FrameworkRule checkPresence(String name, String confidence, List<String> resourceEvidence, List<String> classEvidence) {
		return new DefaultFrameworkRule(name, confidence,
				ctx -> {
					for (String r : resourceEvidence) {
						if (r.contains("/") && ctx.getResourceNames().contains(r)) {
							return true;
						}
						if (!r.contains("/") && containsResource(ctx.getResourceNames(), r)) {
							return true;
						}
					}
					for (String c : classEvidence) {
						if (ctx.getClassNames().contains(c)) {
							return true;
						}
						if (ctx.getClassNames().stream().anyMatch(clsName -> clsName.startsWith(c + "/"))) {
							return true;
						}
					}
					return false;
				},
				ctx -> matchingEvidence(ctx.getResourceNames(), ctx.getClassNames(), resourceEvidence, classEvidence)
		);
	}

	private static FrameworkRule checkLibraryAndClass(String name, String confidence, String libraryPrefix, String classPrefix) {
		return new DefaultFrameworkRule(name, confidence,
				ctx -> hasLibrary(ctx.getLibraryVersions(), libraryPrefix)
						|| ctx.getClassNames().stream().anyMatch(cls -> cls.startsWith(classPrefix + "/")),
				ctx -> matchingLibraryAndClassEvidence(ctx.getLibraryVersions(), ctx.getClassNames(), libraryPrefix, classPrefix)
		);
	}

	private static final List<FrameworkRule> RULES = new ArrayList<>();
	static {
		RULES.add(checkPresence("Native Android", "HIGH", List.of("AndroidManifest.xml"), List.of()));
		RULES.add(new DefaultFrameworkRule("Flutter", "HIGH",
				ctx -> ctx.getResourceNames().stream().anyMatch(name -> name.startsWith("flutter_assets/"))
						|| containsResource(ctx.getResourceNames(), "libflutter.so"),
				ctx -> matchingResources(ctx.getResourceNames(), name -> name.startsWith("flutter_assets/") || name.endsWith("/libflutter.so"))
		));
		RULES.add(checkPresence("React Native", "HIGH",
				List.of("assets/index.android.bundle"),
				List.of("com/facebook/react/ReactActivity", "com/facebook/react/ReactNativeHost")
		));
		RULES.add(checkPresence("Unity", "HIGH",
				List.of("libunity.so"),
				List.of("com/unity3d/player/UnityPlayerActivity")
		));
		RULES.add(checkPresence("Cordova", "HIGH",
				List.of("assets/www/cordova.js"),
				List.of("org/apache/cordova/CordovaActivity")
		));
		RULES.add(checkPresence("Capacitor", "HIGH",
				List.of("assets/capacitor.config.json"),
				List.of("com/getcapacitor/BridgeActivity")
		));
		RULES.add(checkPresence("Xamarin", "HIGH",
				List.of("libmonodroid.so"),
				List.of("mono/android/Runtime")
		));
		RULES.add(checkPresence("Tauri", "HIGH",
				List.of("assets/tauri.conf.json", "libtauri.so"),
				List.of()
		));
		RULES.add(checkPresence("Cocos2d", "HIGH",
				List.of("libcocos2d.so", "libcocos2djs.so"),
				List.of("org/cocos2dx", "org/cocos2d")
		));
		RULES.add(checkPresence("Unreal Engine", "HIGH",
				List.of("libUE4.so", "libUnreal.so"),
				List.of("com/epicgames/ue4", "com/epicgames/unreal")
		));
		RULES.add(checkPresence("Kotlin runtime", "MEDIUM",
				List.of("kotlin-tooling-metadata.json"),
				List.of("kotlin")
		));
		RULES.add(checkPresence("AndroidX / Jetpack", "HIGH",
				List.of("META-INF/androidx.core_core-ktx.version"),
				List.of("androidx")
		));
		RULES.add(checkLibraryAndClass("Jetpack Compose", "HIGH", "androidx.compose.", "androidx/compose"));
		RULES.add(checkLibraryAndClass("Room", "HIGH", "androidx.room", "androidx/room"));
		RULES.add(new DefaultFrameworkRule("Firebase", "HIGH",
				ctx -> ctx.getResourceNames().contains("google-services.json")
						|| hasLibrary(ctx.getLibraryVersions(), "com.google.firebase")
						|| ctx.getClassNames().stream().anyMatch(cls -> cls.startsWith("com/google/firebase/")),
				ctx -> matchingFirebaseEvidence(ctx.getResourceNames(), ctx.getLibraryVersions(), ctx.getClassNames())
		));
		RULES.add(checkLibraryAndClass("Retrofit", "HIGH", "com.squareup.retrofit", "retrofit2"));
		RULES.add(checkLibraryAndClass("OkHttp", "HIGH", "com.squareup.okhttp", "okhttp3"));
		RULES.add(new DefaultFrameworkRule("Dagger / Hilt", "HIGH",
				ctx -> hasLibrary(ctx.getLibraryVersions(), "com.google.dagger")
						|| ctx.getClassNames().stream().anyMatch(cls -> cls.startsWith("dagger/") || cls.startsWith("javax/inject/")),
				ctx -> matchingDaggerEvidence(ctx.getLibraryVersions(), ctx.getClassNames())
		));
		RULES.add(checkLibraryAndClass("Koin", "HIGH", "org.insert-koin", "org/koin"));
		RULES.add(checkLibraryAndClass("RxJava", "HIGH", "io.reactivex", "io/reactivex"));
		RULES.add(checkLibraryAndClass("Glide", "HIGH", "com.github.bumptech.glide", "com/bumptech/glide"));
		RULES.add(checkLibraryAndClass("Lottie", "HIGH", "com.airbnb.android", "com/airbnb/lottie"));
		RULES.add(new DefaultFrameworkRule("WebView / Hybrid", "MEDIUM",
				ctx -> ctx.getResourceNames().stream().anyMatch(name -> name.startsWith("assets/www/") || name.startsWith("assets/public/"))
						|| ctx.getClassNames().stream().anyMatch(cls -> cls.endsWith("/WebViewActivity") || cls.endsWith("/WebViewFragment")),
				ctx -> matchingWebViewEvidence(ctx.getResourceNames(), ctx.getClassNames())
		));
		RULES.add(new DefaultFrameworkRule("R8 / ProGuard", "MEDIUM",
				ctx -> ctx.getResourceNames().stream().anyMatch(name -> name.startsWith("META-INF/proguard/") || name.endsWith("proguard-project.txt"))
						|| ctx.getClassNames().stream().anyMatch(cls -> cls.startsWith("com/android/tools/r8/")),
				ctx -> matchingR8Evidence(ctx.getResourceNames(), ctx.getClassNames())
		));
	}
}
