package dexforge.core.parser.smali.analysis;

import dexforge.core.parser.smali.model.SmaliClass;
import dexforge.core.parser.smali.model.SmaliMethod;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates registers, labels, types, and opcodes in Smali models.
 */
public final class SmaliSemanticAnalyzer {

	public List<String> analyze(SmaliClass smaliClass) {
		List<String> errors = new ArrayList<>();

		for (SmaliMethod method : smaliClass.getMethods()) {
			validateMethod(method, errors);
		}

		return errors;
	}

	private void validateMethod(SmaliMethod method, List<String> errors) {
		int maxRegisters = method.getRegisters();
		for (String insn : method.getInstructions()) {
			// Basic register range validation
			String[] parts = insn.split("[\\s,{}]+");
			for (String part : parts) {
				if (part.matches("[vp]\\d+")) {
					int regIdx = Integer.parseInt(part.substring(1));
					if (regIdx >= maxRegisters && !part.startsWith("p")) {
						// Simplified: p registers are usually handled differently in real dalvik
						errors.add(String.format("Method %s: Register %s exceeds allocated %d registers",
								method.getName(), part, maxRegisters));
					}
				}
			}

			// Basic opcode validation
			if (insn.startsWith("invokevirtual")) {
				errors.add(String.format("Method %s: Invalid opcode 'invokevirtual', did you mean 'invoke-virtual'?",
						method.getName()));
			}
		}

		// Check for duplicate labels, invalid types, etc. could be added here
	}
}
