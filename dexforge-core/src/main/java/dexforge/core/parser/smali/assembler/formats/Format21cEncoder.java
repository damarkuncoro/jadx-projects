package dexforge.core.parser.smali.assembler.formats;

import dexforge.core.parser.dex.io.DexByteWriter;
import dexforge.core.parser.dex.builder.DexPoolManager;

/**
 * Encodes Format 21c (AA|op BBBB): const-string, check-cast, new-instance, sget/sput.
 */
public final class Format21cEncoder implements InstructionEncoder {
	@Override
	public void encode(int opcode, String[] operands, DexByteWriter writer, DexPoolManager poolManager) {
		// operands: [vAA, reference]
		int regA = Integer.parseInt(operands[0].replaceAll("[^0-9]", ""));
		String ref = operands[1];

		int index = 0;
		// Resolve reference based on context (Simplified)
		if (ref.startsWith("\"")) {
			index = poolManager.getStringIndex(ref.substring(1, ref.length() - 1));
		} else if (ref.startsWith("L")) {
			index = poolManager.getTypeIndex(ref);
		} else {
			// Field reference logic
		}

		writer.writeByte(opcode);
		writer.writeByte(regA & 0xFF);
		writer.writeShort(index & 0xFFFF);
	}
}
