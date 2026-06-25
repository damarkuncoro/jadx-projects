package dexforge.core.parser.smali.assembler.formats;

import dexforge.core.parser.dex.io.DexByteWriter;
import dexforge.core.parser.dex.builder.DexPoolManager;

/**
 * Encodes Format 3rc (AA|op BBBB CCCC): invoke-kind/range.
 */
public final class Format3rcEncoder implements InstructionEncoder {
	@Override
	public void encode(int opcode, String[] operands, DexByteWriter writer, DexPoolManager poolManager) {
		// operands: [{vCCCC..vNNNN}, method_ref]
		int count = 0;
		int startReg = 0;

		for (String op : operands) {
			if (op.startsWith("{")) {
				String range = op.substring(1, op.length() - 1);
				if (range.contains("..")) {
					String[] parts = range.split("\\.\\.");
					startReg = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
					int endReg = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
					count = endReg - startReg + 1;
				} else {
					startReg = Integer.parseInt(range.replaceAll("[^0-9]", ""));
					count = 1;
				}
			}
		}

		writer.writeByte(opcode);
		writer.writeByte(count & 0xFF);
		writer.writeShort(0); // BBBB: method index placeholder
		writer.writeShort(startReg & 0xFFFF);
	}
}
