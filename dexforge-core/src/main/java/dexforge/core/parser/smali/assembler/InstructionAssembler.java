package dexforge.core.parser.smali.assembler;

import dexforge.core.parser.dex.io.DexByteWriter;
import dexforge.core.parser.dex.builder.DexPoolManager;
import dexforge.core.parser.smali.assembler.formats.*;
import java.util.Map;
import java.util.HashMap;

/**
 * REUSEABLE and SOLID instruction assembler.
 * Uses Strategy pattern with Encoders to handle different Dalvik instruction formats.
 */
public final class InstructionAssembler {
	private final Map<String, Integer> opcodeMap = new HashMap<>();
	private final Map<Integer, InstructionEncoder> encoders = new HashMap<>();

	public InstructionAssembler() {
		initMaps();
	}

	private void initMaps() {
		// --- Opcode Mapping ---
		opcodeMap.put("nop", 0x00);
		opcodeMap.put("return", 0x0F);
		opcodeMap.put("return-wide", 0x10);
		opcodeMap.put("return-object", 0x11);
		opcodeMap.put("const/16", 0x13);
		opcodeMap.put("const", 0x14);
		opcodeMap.put("const/high16", 0x15);
		opcodeMap.put("const-wide/16", 0x16);
		opcodeMap.put("const-wide/32", 0x17);
		opcodeMap.put("const-wide", 0x18);
		opcodeMap.put("const-wide/high16", 0x19);
		opcodeMap.put("const-string/jumbo", 0x1B);
		opcodeMap.put("const-class", 0x1C);
		opcodeMap.put("check-cast", 0x1F);
		opcodeMap.put("instance-of", 0x20);
		opcodeMap.put("new-instance", 0x22);
		opcodeMap.put("new-array", 0x23);
		opcodeMap.put("goto", 0x28);
		opcodeMap.put("goto/16", 0x29);
		opcodeMap.put("goto/32", 0x2A);
		opcodeMap.put("if-eq", 0x32);
		opcodeMap.put("if-ne", 0x33);
		opcodeMap.put("if-lt", 0x34);
		opcodeMap.put("if-ge", 0x35);
		opcodeMap.put("if-gt", 0x36);
		opcodeMap.put("if-le", 0x37);
		opcodeMap.put("if-eqz", 0x38);
		opcodeMap.put("if-nez", 0x39);
		opcodeMap.put("iget", 0x44);
		opcodeMap.put("iput", 0x4B);
		opcodeMap.put("sget", 0x60);
		opcodeMap.put("sput", 0x67);
		opcodeMap.put("invoke-virtual", 0x6E);
		opcodeMap.put("invoke-super", 0x6F);
		opcodeMap.put("invoke-direct", 0x70);
		opcodeMap.put("invoke-static", 0x71);
		opcodeMap.put("invoke-interface", 0x72);
		opcodeMap.put("invoke-virtual/range", 0x74);
		opcodeMap.put("invoke-super/range", 0x75);
		opcodeMap.put("invoke-direct/range", 0x76);
		opcodeMap.put("invoke-static/range", 0x77);
		opcodeMap.put("invoke-interface/range", 0x78);

		// --- Strategy Registration (SOLID: Open-Closed) ---

		// Format 10x (op 00)
		InstructionEncoder format10x = (op, ops, w, p) -> {
			w.writeByte(op);
			w.writeByte(0);
		};

		// Format 11n (B|A|op)
		InstructionEncoder format11n = new Format11nEncoder();

		// Format 11x (AA|op)
		InstructionEncoder format11x = (op, ops, w, p) -> {
			int reg = Integer.parseInt(ops[0].substring(1));
			w.writeByte(op);
			w.writeByte(reg);
		};

		// Format 21c (AA|op BBBB)
		InstructionEncoder format21c = new Format21cEncoder();

		// Format 22c (B|A|op CCCC)
		InstructionEncoder format22c = new Format22cEncoder();

		// Format 10t (AA|op)
		InstructionEncoder format10t = new Format10tEncoder();

		// Format 35c (A|G|op BBBB F|E|D|C)
		InstructionEncoder format35c = new Format35cEncoder();

		// Format 3rc (AA|op BBBB CCCC)
		InstructionEncoder format3rc = new Format3rcEncoder();

		// Map opcodes to encoders
		encoders.put(0x00, format10x);
		encoders.put(0x0E, format10x);

		encoders.put(0x0F, format11x);
		encoders.put(0x10, format11x);
		encoders.put(0x11, format11x);

		encoders.put(0x12, format11n);

		encoders.put(0x13, format21c);
		encoders.put(0x14, format21c);
		encoders.put(0x15, format21c);
		encoders.put(0x1A, format21c);
		encoders.put(0x1B, format21c);
		encoders.put(0x1C, format21c);
		encoders.put(0x22, format21c);
		encoders.put(0x60, format21c);
		encoders.put(0x67, format21c);

		encoders.put(0x23, format22c);
		encoders.put(0x44, format22c);
		encoders.put(0x4B, format22c);

		encoders.put(0x28, format10t);

		encoders.put(0x6E, format35c);
		encoders.put(0x6F, format35c);
		encoders.put(0x70, format35c);
		encoders.put(0x71, format35c);
		encoders.put(0x72, format35c);

		encoders.put(0x74, format3rc);
		encoders.put(0x75, format3rc);
		encoders.put(0x76, format3rc);
		encoders.put(0x77, format3rc);
		encoders.put(0x78, format3rc);
	}

	public void assemble(String line, DexByteWriter writer, DexPoolManager poolManager) {
		String[] parts = line.split("[\\s,]+");
		if (parts.length == 0 || parts[0].isEmpty()) {
			return;
		}

		String mnemonic = parts[0];
		Integer opcode = opcodeMap.get(mnemonic);
		if (opcode == null) {
			// Check for variants like iget-object -> iget
			if (mnemonic.startsWith("iget-")) {
				opcode = opcodeMap.get("iget");
			} else if (mnemonic.startsWith("iput-")) {
				opcode = opcodeMap.get("iput");
			} else if (mnemonic.startsWith("sget-")) {
				opcode = opcodeMap.get("sget");
			} else if (mnemonic.startsWith("sput-")) {
				opcode = opcodeMap.get("sput");
			}
		}

		if (opcode == null) {
			writer.writeShort(0x0000); // NOP as fallback
			return;
		}

		InstructionEncoder encoder = encoders.get(opcode);
		if (encoder != null) {
			String[] operands = new String[parts.length - 1];
			System.arraycopy(parts, 1, operands, 0, parts.length - 1);
			encoder.encode(opcode, operands, writer, poolManager);
		} else {
			writer.writeShort(opcode & 0xFF);
		}
	}
}
