package jadx.gui.buildstack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Collector untuk mengumpulkan versi library dari file .version.
 */
public class LibraryVersionCollector {

	private LibraryVersionCollector() {
	}

	/**
	 * Mengumpulkan versi library dari resource.
	 */
	public static Map<String, String> collect(Function<String, String> resourceReader, Set<String> resourceNames) {
		Map<String, String> versions = new LinkedHashMap<>();
		resourceNames.stream()
				.filter(name -> name.startsWith("META-INF/"))
				.filter(name -> name.endsWith(".version"))
				.sorted()
				.forEach(name -> {
					String library = name.substring("META-INF/".length(), name.length() - ".version".length());
					String version = resourceReader.apply(name);
					if (version != null) {
						versions.put(library, version.trim());
					}
				});
		return versions;
	}
}
