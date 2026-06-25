package dexforge.core.parser.smali.assembler.formats;

import dexforge.core.parser.dex.io.DexByteWriter;
import dexforge.core.parser.dex.builder.DexPoolManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Encodes Format 35c (A|G|op BBBB F|E|D|C): invoke-kind.
 */
public final class Format35cEncoder implements InstructionEncoder {
	@Override
	public void encode(int opcode, String[] operands, DexByteWriter writer, DexPoolManager poolManager) {
		// operands: [{vC, vD...}, method_ref]
		// Simplified parsing for smali format: invoke-virtual {v0, v1}, Lcls;->mth()V
		List<Integer> regs = new ArrayList<>();
		String methodRef = "";

		for (String op : operands) {
			if (op.startsWith("{")) {
				String list = op.substring(1, op.length() - 1);
				if (!list.isEmpty()) {
					for (String r : list.split(",")) {
						regs.add(Integer.parseInt(r.trim().replaceAll("[^0-9]", "")));
					}
				}
			} else if (op.contains("->")) {
				methodRef = op;
			}
		}

		int count = regs.size();
		int bbbb = 0; // In real implementation, resolve from poolManager

		// Byte 1: A (4 bits, count) | G (4 bits, 5th reg)
		int g = (count == 5) ? regs.get(4) : 0;
		writer.writeByte(((count & 0x0F) << 4) | (g & 0x0F));

		// Byte 2: Opcode
		writer.writeByte(opcode);

		// Bytes 3-4: BBBB (method index)
		writer.writeShort(bbbb & 0xFFFF);

		// Byte 5: F (4 bits) | E (4 bits)
		int f = (count >= 4) ? regs.get(3) : 0;
		int e = (count >= 3) ? regs.get(2) : 0;
		writer.writeByte(((f & 0x0F) << 4) | (e & 0x0F));

		// Byte 6: D (4 bits) | C (4 bits)
		int d = (count >= 2) ? regs.get(1) : 0;
		int c = (count >= 1) ? regs.get(0) : 0;
		writer.writeByte(((d & 0x0F) << 4) | (c & 0x0F));
	}
}
