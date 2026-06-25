package dexforge.core.parser.smali.assembler.formats;

import dexforge.core.parser.dex.io.DexByteWriter;
import dexforge.core.parser.dex.builder.DexPoolManager;

/**
 * Encodes Format 10t (AA|op): goto.
 */
public final class Format10tEncoder implements InstructionEncoder {
	@Override
	public void encode(int opcode, String[] operands, DexByteWriter writer, DexPoolManager poolManager) {
		// operands: [:label or offset]
		// Simplified: using 0 for relative offset as labels need second pass
		writer.writeByte(opcode);
		writer.writeByte(0);
	}
}
