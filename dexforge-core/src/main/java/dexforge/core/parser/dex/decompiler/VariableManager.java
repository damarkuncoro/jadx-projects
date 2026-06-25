package dexforge.core.parser.dex.decompiler;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Manages variable names and scopes for the decompiler.
 * Translates register indices (v0, p1) into meaningful names.
 */
public final class VariableManager {
	private final Map<Integer, String> regToName = new HashMap<>();
	private final Map<String, Integer> nameCounts = new HashMap<>();

	/**
	 * Initializes parameter names based on method signature.
	 * p0 = this (if not static), p1... = parameters.
	 */
	public void initParameters(boolean isStatic, List<String> paramTypes, int totalRegisters) {
		int reg = totalRegisters - countRegisters(paramTypes) - (isStatic ? 0 : 1);

		if (!isStatic) {
			regToName.put(reg++, "this");
		}

		for (int i = 0; i < paramTypes.size(); i++) {
			String name = "p" + i;
			regToName.put(reg, name);
			// Long and Double take 2 registers
			reg += (paramTypes.get(i).equals("J") || paramTypes.get(i).equals("D")) ? 2 : 1;
		}
	}

	private int countRegisters(List<String> types) {
		int count = 0;
		for (String t : types) {
			count += (t.equals("J") || t.equals("D")) ? 2 : 1;
		}
		return count;
	}

	public String getVariableName(int reg, String type) {
		if (regToName.containsKey(reg)) {
			return regToName.get(reg);
		}

		String baseName = "v";
		if (type != null) {
			// Simplified deobfuscation for common types
			if (type.equals("int")) {
				baseName = "i";
			} else if (type.contains("String")) {
				baseName = "s";
			} else if (type.startsWith("L")) {
				int lastSlash = type.lastIndexOf('/');
				baseName = type.substring(lastSlash + 1, type.length() - 1).toLowerCase();
				if (baseName.equals("this$0")) {
					baseName = "outer";
				}
				// Heuristic: if name is still 'a', 'b', use 'obj'
				if (baseName.length() <= 2) {
					baseName = "obj";
				}
			}
		}

		int count = nameCounts.getOrDefault(baseName, 0) + 1;
		nameCounts.put(baseName, count);

		String finalName = baseName + count;
		regToName.put(reg, finalName);
		return finalName;
	}
}
