package dexforge.api.model.insn;

import java.util.List;
import java.util.Optional;

import dexforge.api.model.DexForgeNode;

/**
 * High-level abstraction of a bytecode instruction.
 */
public interface DexForgeInstruction {
	/**
	 * Categorized opcode.
	 */
	DexForgeOpcode getOpcode();

	/**
	 * The raw offset in the bytecode (if available).
	 */
	int getOffset();

	/**
	 * String representation of the instruction (e.g. "invoke-static Ljava/lang/System;->currentTimeMillis()J")
	 */
	String getMnemonic();

	/**
	 * Get the node this instruction refers to (e.g. Method being called, or Field being accessed).
	 */
	Optional<DexForgeNode> getReferencedNode();

	/**
	 * List of arguments/registers used by this instruction.
	 */
	List<String> getOperands();
}
