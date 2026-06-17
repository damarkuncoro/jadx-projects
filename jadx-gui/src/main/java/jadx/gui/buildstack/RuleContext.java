package jadx.gui.buildstack;

import java.util.Map;
import java.util.Set;

/**
 * Context yang menyediakan data untuk aturan deteksi framework.
 */
public class RuleContext {
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
