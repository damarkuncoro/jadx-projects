package dexforge.core.parser.dex.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dexforge.api.model.DexForgeApkMetadata;
import dexforge.core.parser.dex.model.DexClass;

/**
 * Aggregates multiple DEX files (Multi-DEX) into a single logical project.
 */
public final class DexProject {
	private final List<DexFastIndexer> indexers = new ArrayList<>();
	private DexForgeApkMetadata metadata;

	public void addDex(byte[] data) {
		indexers.add(new DexFastIndexer(data));
	}

	public void setMetadata(DexForgeApkMetadata metadata) {
		this.metadata = metadata;
	}

	public DexForgeApkMetadata getMetadata() {
		return metadata;
	}

	/**
	 * Search for strings across all DEX files in parallel.
	 */
	public Map<String, List<String>> searchGlobalStrings(String query) {
		Map<String, List<String>> combinedResults = new HashMap<>();
		indexers.parallelStream().forEach(indexer -> {
			Map<String, List<String>> results = indexer.searchGlobalStringUsages(query);
			synchronized (combinedResults) {
				results.forEach((str, methods) ->
						combinedResults.computeIfAbsent(str, k -> new ArrayList<>()).addAll(methods)
				);
			}
		});
		return combinedResults;
	}

	/**
	 * Search for method calls across all DEX files.
	 */
	public Map<String, List<String>> searchGlobalMethods(String query) {
		Map<String, List<String>> combinedResults = new HashMap<>();
		for (DexFastIndexer indexer : indexers) {
			Map<String, List<String>> results = indexer.searchGlobalMethodCalls(query);
			results.forEach((sig, callers) ->
					combinedResults.computeIfAbsent(sig, k -> new ArrayList<>()).addAll(callers)
			);
		}
		return combinedResults;
	}

	/**
	 * Get all classes from all DEX files.
	 */
	public List<DexClass> getAllClasses() {
		List<DexClass> allClasses = new ArrayList<>();
		for (DexFastIndexer indexer : indexers) {
			allClasses.addAll(indexer.getClasses());
		}
		return allClasses;
	}

	/**
	 * Finds a class by its name across all indexers.
	 */
	public DexClass findClass(String className) {
		for (DexFastIndexer indexer : indexers) {
			for (DexClass clazz : indexer.getClasses()) {
				if (clazz.getName().equals(className)) {
					return clazz;
				}
			}
		}
		return null;
	}

	public List<DexFastIndexer> getIndexers() {
		return indexers;
	}

	/**
	 * Detects Android components in the project.
	 */
	public AndroidComponentDetector getComponentDetector() {
		return new AndroidComponentDetector(this);
	}

	/**
	 * Builds a tree-like structure of packages and classes.
	 * Useful for ProjectTreePanel in GUI.
	 */
	public Map<String, List<String>> getPackageTree() {
		Map<String, List<String>> tree = new HashMap<>();
		for (DexClass clazz : getAllClasses()) {
			String fullName = clazz.getName(); // e.g., "Lcom/app/MainActivity;"
			if (fullName.startsWith("L") && fullName.endsWith(";")) {
				fullName = fullName.substring(1, fullName.length() - 1);
			}
			int lastSlash = fullName.lastIndexOf('/');
			String packageName = (lastSlash == -1) ? "" : fullName.substring(0, lastSlash).replace('/', '.');
			String className = (lastSlash == -1) ? fullName : fullName.substring(lastSlash + 1);

			tree.computeIfAbsent(packageName, k -> new ArrayList<>()).add(className);
		}
		return tree;
	}
}
