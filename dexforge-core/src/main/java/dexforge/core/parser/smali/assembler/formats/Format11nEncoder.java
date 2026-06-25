package dexforge.core.parser.smali.assembler.formats;

import dexforge.core.parser.dex.io.DexByteWriter;
import dexforge.core.parser.dex.builder.DexPoolManager;

/**
 * Encodes Format 11n (B|A|op): const/4.
 */
public final class Format11nEncoder implements InstructionEncoder {
	@Override
	public void encode(int opcode, String[] operands, DexByteWriter writer, DexPoolManager poolManager) {
		// operands: [vA, #+B]
		int regA = parseRegister(operands[0]);
		int valB = Integer.parseInt(operands[1].replace("#", ""));

		// Byte 1: B (4 bits) | A (4 bits)
		// Byte 2: Opcode
		writer.writeByte(((valB & 0x0F) << 4) | (regA & 0x0F));
		writer.writeByte(opcode);
	}

	private int parseRegister(String reg) {
		return Integer.parseInt(reg.replaceAll("[^0-9]", ""));
	}
}
