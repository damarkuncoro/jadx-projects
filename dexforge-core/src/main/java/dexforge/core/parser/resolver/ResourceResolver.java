package dexforge.core.parser.resolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Unified service to resolve Android Resource IDs across the project.
 * Aggregates information from resources.arsc and DEX R classes.
 */
public final class ResourceResolver {
	private final Map<Integer, String> idToNameMap = new HashMap<>();

	public ResourceResolver() {
		// Initialize with core system attributes for better AXML decompilation
		idToNameMap.putAll(SystemResourceMap.getAll());
	}

	public void addMapping(int id, String name) {
		idToNameMap.put(id, name);
	}

	public void addMappings(Map<Integer, String> mappings) {
		if (mappings != null) {
			idToNameMap.putAll(mappings);
		}
	}

	public String resolve(int id) {
		return idToNameMap.get(id);
	}

	public String resolveOrDefault(int id) {
		String name = idToNameMap.get(id);
		if (name != null) {
			return name;
		}
		return String.format("0x%08x", id);
	}

	public boolean isResourceId(int val) {
		// Standard Android resource patterns: 0x7f... (app) or 0x01... (system)
		return (val >= 0x7f010000 && val <= 0x7fffffff) || (val >= 0x01010000 && val <= 0x01ffffff);
	}

	public Map<Integer, String> getAllMappings() {
		return new HashMap<>(idToNameMap);
	}
}
