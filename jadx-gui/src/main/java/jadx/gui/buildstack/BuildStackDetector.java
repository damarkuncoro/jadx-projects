package jadx.gui.buildstack;

import jadx.api.ResourceFile;
import jadx.api.ResourcesLoader;
import jadx.core.dex.nodes.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Detektor utama untuk build stack.
 */
public class BuildStackDetector {
	private static final Logger LOG = LoggerFactory.getLogger(BuildStackDetector.class);

	private BuildStackDetector() {
	}

	/**
	 * Menganalisis proyek yang diekspor.
	 */
	public static BuildStackInfo analyzeExportedProject(File outputDir) {
		File sourcesDir = new File(outputDir, "sources");
		File resourcesDir = new File(outputDir, "resources");
		Map<String, String> resources = collectExportedResources(resourcesDir);
		Set<String> resourceNames = resources.keySet();
		Set<String> classNames = collectExportedClassNames(sourcesDir);
		BuildStackInfo info = analyze(resources::get, resourceNames, classNames);
		return info.withEvidencePrefix("resources/");
	}

	/**
	 * Menganalisis proyek yang dimuat.
	 */
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

	/**
	 * Metode inti untuk menganalisis build stack.
	 */
	private static BuildStackInfo analyze(
			Function<String, String> resourceReader,
			Set<String> resourceNames,
			Set<String> classNames) {
		Map<String, Object> buildMetadata = KotlinToolingMetadataParser.parse(resourceReader.apply("kotlin-tooling-metadata.json"));
		Map<String, String> manifest = ManifestParser.parse(resourceReader.apply("AndroidManifest.xml"));
		Map<String, String> libraryVersions = LibraryVersionCollector.collect(resourceReader, resourceNames);
		List<FrameworkDetection> frameworks = FrameworkRules.detectAll(resourceNames, classNames, libraryVersions);
		Set<String> evidence = frameworks.stream()
				.flatMap(framework -> framework.getEvidence().stream())
				.collect(Collectors.toCollection(LinkedHashSet::new));
		if (!buildMetadata.isEmpty()) {
			evidence.add("kotlin-tooling-metadata.json");
		}
		if (!manifest.isEmpty()) {
			evidence.add("AndroidManifest.xml");
		}
		return new BuildStackInfo(
				summarizeBuildStack(buildMetadata, manifest, frameworks),
				buildMetadata,
				manifest,
				frameworks,
				libraryVersions,
				evidence.stream().sorted().toList()
		);
	}

	/**
	 * Mengumpulkan resource dari direktori yang diekspor.
	 */
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

	/**
	 * Mengumpulkan nama class dari direktori yang diekspor.
	 */
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

	/**
	 * Mengecek apakah resource adalah teks.
	 */
	private static boolean isTextResource(String name) {
		return name.equals("AndroidManifest.xml")
				|| name.equals("kotlin-tooling-metadata.json")
				|| name.endsWith(".version");
	}

	/**
	 * Membaca konten resource.
	 */
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

	/**
	 * Membuat ringkasan build stack.
	 */
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

	/**
	 * Mendapatkan nama resource dari ResourceFile.
	 */
	private static String getResourceName(ResourceFile resource) {
		String name = resource.getOriginalName();
		int zipSeparator = name.indexOf(':');
		if (zipSeparator != -1 && zipSeparator + 1 < name.length()) {
			return name.substring(zipSeparator + 1);
		}
		return name;
	}

	/**
	 * Mengecek apakah library penting (forward ke FrameworkRules).
	 */
	public static boolean isImportantLibrary(String name) {
		return FrameworkRules.isImportantLibrary(name);
	}
}
