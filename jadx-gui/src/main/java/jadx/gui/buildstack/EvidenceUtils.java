package jadx.gui.buildstack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class untuk bekerja dengan evidence.
 */
public class EvidenceUtils {

	private EvidenceUtils() {
	}

	/**
	 * Mengecek apakah resource dengan nama tertentu ada di dalam daftar.
	 */
	public static boolean containsResource(Set<String> resourceNames, String fileName) {
		return resourceNames.stream().anyMatch(name -> name.equals(fileName) || name.endsWith('/' + fileName));
	}

	/**
	 * Mencari resource yang sesuai dengan predicate.
	 */
	public static List<String> matchingResources(Set<String> resourceNames, Predicate<String> predicate) {
		return resourceNames.stream()
				.filter(predicate)
				.sorted()
				.collect(Collectors.toList());
	}

	/**
	 * Mencari evidence yang sesuai dari resource dan class.
	 */
	public static List<String> matchingEvidence(
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

	/**
	 * Mengecek apakah library dengan prefix tertentu ada.
	 */
	public static boolean hasLibrary(Map<String, String> libraryVersions, String prefix) {
		return libraryVersions.keySet().stream().anyMatch(name -> name.startsWith(prefix));
	}

	/**
	 * Mencari evidence dari library dan class.
	 */
	public static List<String> matchingLibraryAndClassEvidence(
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

	/**
	 * Mencari evidence untuk Firebase.
	 */
	public static List<String> matchingFirebaseEvidence(
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

	/**
	 * Mencari evidence untuk Dagger.
	 */
	public static List<String> matchingDaggerEvidence(Map<String, String> libraryVersions, Set<String> classNames) {
		List<String> evidence =
				new ArrayList<>(matchingLibraryAndClassEvidence(libraryVersions, classNames, "com.google.dagger", "dagger"));
		if (classNames.stream().anyMatch(name -> name.startsWith("javax/inject/"))) {
			evidence.add("package:javax/inject");
		}
		return evidence.stream().distinct().sorted().collect(Collectors.toList());
	}

	/**
	 * Mencari evidence untuk WebView.
	 */
	public static List<String> matchingWebViewEvidence(Set<String> resourceNames, Set<String> classNames) {
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

	/**
	 * Mencari evidence untuk R8/ProGuard.
	 */
	public static List<String> matchingR8Evidence(Set<String> resourceNames, Set<String> classNames) {
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

	/**
	 * Mengkonversi evidence ke format untuk proyek yang diekspor.
	 */
	public static String toExportedEvidence(String item, String resourcePrefix) {
		if (item.startsWith("class:")) {
			return "sources/" + item.substring("class:".length()) + ".java";
		}
		if (item.startsWith("package:")) {
			return "sources/" + item.substring("package:".length());
		}
		return resourcePrefix + item;
	}
}
