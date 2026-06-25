package dexforge.core.parser.analysis.deobf;

import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.service.DexFastIndexer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Recovers original class names using heuristics like source file names.
 * Especially useful for ProGuard/R8 obfuscated code.
 */
public final class HeuristicDeobfuscator {
	private final DexFastIndexer indexer;
	private final Map<String, String> classNameMap = new HashMap<>();

	public HeuristicDeobfuscator(DexFastIndexer indexer) {
		this.indexer = indexer;
	}

	public Map<String, String> deobfuscate() {
		List<DexClass> classes = indexer.getClasses();
		for (DexClass clazz : classes) {
			String currentName = clazz.getName();
			String sourceFile = clazz.getSourceFile();

			// Heuristic 1: If class name is short (a, b, c) and source file is descriptive
			if (isObfuscated(currentName) && sourceFile != null && !sourceFile.isEmpty()) {
				String potentialName = guessClassNameFromSource(currentName, sourceFile);
				if (potentialName != null && !potentialName.equals(currentName)) {
					classNameMap.put(currentName, potentialName);
				}
			}
		}
		return classNameMap;
	}

	private boolean isObfuscated(String name) {
		// Simple check: Lcom/example/a; -> last part is "a"
		int lastSlash = name.lastIndexOf('/');
		String simpleName = name.substring(lastSlash + 1, name.length() - 1);
		return simpleName.length() <= 2;
	}

	private String guessClassNameFromSource(String currentName, String sourceFile) {
		// sourceFile: "MainActivity.java"
		// currentName: "Lcom/example/a;"
		// Result: "Lcom/example/MainActivity;"
		String nameWithoutExt = sourceFile.contains(".") ?
				sourceFile.substring(0, sourceFile.lastIndexOf('.')) : sourceFile;

		int lastSlash = currentName.lastIndexOf('/');
		if (lastSlash == -1) {
			return "L" + nameWithoutExt + ";";
		}

		return currentName.substring(0, lastSlash + 1) + nameWithoutExt + ";";
	}
}
