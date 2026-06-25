package dexforge.core.parser.smali.assembler.formats;

import dexforge.core.parser.dex.io.DexByteWriter;
import dexforge.core.parser.dex.builder.DexPoolManager;

/**
 * REUSEABLE interface for encoding different Dalvik instruction formats.
 * Promotes SOLID (Open-Closed Principle).
 */
public interface InstructionEncoder {
	/**
	 * Encodes a specific Dalvik instruction format into bytecode.
	 * @param opcode The Dalvik opcode.
	 * @param operands The instruction operands (registers, constants, references).
	 * @param writer Binary writer to output the bytecode.
	 * @param poolManager Pool manager to resolve reference indices.
	 */
	void encode(int opcode, String[] operands, DexByteWriter writer, DexPoolManager poolManager);
}
