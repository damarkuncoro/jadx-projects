package dexforge.core.parser.smali.assembler.formats;

import dexforge.core.parser.dex.io.DexByteWriter;
import dexforge.core.parser.dex.builder.DexPoolManager;

/**
 * Encodes Format 22c (B|A|op CCCC): iget, iput, instance-of, new-array.
 */
public final class Format22cEncoder implements InstructionEncoder {
	@Override
	public void encode(int opcode, String[] operands, DexByteWriter writer, DexPoolManager poolManager) {
		// operands: [vA, vB, reference]
		int regA = parseRegister(operands[0]);
		int regB = parseRegister(operands[1]);
		String ref = operands[2];

		int index = 0;
		if (ref.startsWith("L") || ref.startsWith("[")) {
			index = poolManager.getTypeIndex(ref);
		} else {
			// Field resolution logic would go here
		}

		// Byte 1: B (4 bits) | A (4 bits)
		writer.writeByte(((regB & 0x0F) << 4) | (regA & 0x0F));
		writer.writeByte(opcode);
		writer.writeShort(index & 0xFFFF);
	}

	private int parseRegister(String reg) {
		return Integer.parseInt(reg.replaceAll("[^0-9]", ""));
	}
}
