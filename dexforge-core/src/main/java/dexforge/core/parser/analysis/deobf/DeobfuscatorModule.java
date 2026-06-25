package dexforge.core.parser.analysis.deobf;

import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.analysis.dataflow.SimpleDataFlowAnalyzer;

/**
 * Interface for deobfuscation modules.
 * Allows adding new deobfuscation strategies without modifying the core engine.
 */
public interface DeobfuscatorModule {
	/**
	 * Called for each instruction during analysis.
	 * @param clazz Current class being scanned
	 * @param method Current method being scanned
	 * @param insn The current instruction
	 * @param analyzer Current state of register constants
	 */
	void processInstruction(DexClass clazz, DexEncodedMethod method, DexInstruction insn, SimpleDataFlowAnalyzer analyzer);
}
