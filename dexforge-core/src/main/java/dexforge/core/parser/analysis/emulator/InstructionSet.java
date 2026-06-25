package dexforge.core.parser.analysis.emulator;

import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.sections.DexOpcode;
import dexforge.core.parser.dex.service.DexFastIndexer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * REUSEABLE: Centralized collection of instruction executors.
 * Follows Strategy pattern and promotes DRY.
 */
public final class InstructionSet {
	private final Map<Integer, InstructionExecutor> executors = new HashMap<>();
	private final EmulatorState state;
	private final DexFastIndexer indexer;
	private final ControlFlowHandler cfHandler;
	private MethodInvokeHandler invokeHandler;

	public InstructionSet(EmulatorState state, DexFastIndexer indexer) {
		this(state, indexer, null);
	}

	public InstructionSet(EmulatorState state, DexFastIndexer indexer, MethodInvokeHandler invokeHandler) {
		this.state = state;
		this.indexer = indexer;
		this.cfHandler = new ControlFlowHandler(state.getRegisters());
		this.invokeHandler = invokeHandler;
		initExecutors();
	}

	public InstructionExecutor getExecutor(int opcode) {
		return executors.get(opcode);
	}

	public void setInvokeHandler(MethodInvokeHandler invokeHandler) {
		this.invokeHandler = invokeHandler;
	}

	private void setWideRegister(Map<Integer, Object> regs, int reg, long val) {
		regs.put(reg, val);
		regs.put(reg + 1, (int) (val >> 32));
	}

	private void setDoubleRegister(Map<Integer, Object> regs, int reg, double val) {
		regs.put(reg, val);
		long bits = Double.doubleToRawLongBits(val);
		regs.put(reg + 1, (int) (bits >> 32));
	}

	private void initExecutors() {
		// --- MOVE FAMILY ---
		InstructionExecutor moveExec = (insn, regs) -> {
			int[] r = insn.getRegisters();
			if (r != null && r.length >= 2) regs.put(r[0], regs.get(r[1]));
		};
		for (int op = 0x01; op <= 0x0D; op++) executors.put(op, moveExec);

		// --- MOVE RESULT FAMILY ---
		InstructionExecutor moveResultExec = (insn, regs) -> {
			int[] r = insn.getRegisters();
			if (r != null && r.length >= 1) regs.put(r[0], state.getLastResult());
		};
		executors.put(0x0A, moveResultExec);
		executors.put(0x0B, moveResultExec);
		executors.put(0x0C, moveResultExec);

		// --- CONST FAMILY ---
		executors.put(0x12, (insn, regs) -> {
			int[] r = insn.getRegisters();
			if (r != null) regs.put(r[0], (int) insn.getLiteral());
		});
		executors.put(0x13, (insn, regs) -> {
			int[] r = insn.getRegisters();
			if (r != null) regs.put(r[0], (int) insn.getLiteral());
		});
		executors.put(0x14, (insn, regs) -> {
			int[] r = insn.getRegisters();
			if (r != null) regs.put(r[0], (int) insn.getLiteral());
		});
		executors.put(0x15, (insn, regs) -> {
			int[] r = insn.getRegisters();
			if (r != null) regs.put(r[0], (int) insn.getLiteral());
		});

		InstructionExecutor constStringExec = (insn, regs) -> {
			int[] r = insn.getRegisters();
			if (r != null && r.length > 0 && indexer != null) {
				String val = null;
				if (insn.getIndex() >= 0 && insn.getIndex() < indexer.getStringPool().getSize()) {
					val = indexer.getStringPool().getString(insn.getIndex());
				}
				regs.put(r[0], val);
			}
		};
		executors.put(0x1A, constStringExec);
		executors.put(0x1B, constStringExec);

		executors.put(0x1C, (insn, regs) -> {
			int[] r = insn.getRegisters();
			if (r != null && r.length > 0 && indexer != null) {
				String type = null;
				if (insn.getIndex() >= 0 && insn.getIndex() < indexer.getTypePool().getSize()) {
					type = indexer.getTypePool().getTypeName(insn.getIndex());
				}
				regs.put(r[0], type);
			}
		});

		executors.put(0x22, (insn, regs) -> {
			int[] r = insn.getRegisters();
			if (r != null) regs.put(r[0], createNewInstance(insn.getIndex()));
		});

		// --- ARITHMETIC ---
		executors.put(0x90, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a + b));
		executors.put(0x91, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a - b));
		executors.put(0x92, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a * b));
		executors.put(0x93, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> b == 0 ? 0 : a / b));
		executors.put(0x94, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> b == 0 ? 0 : a % b));
		executors.put(0x95, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a & b));
		executors.put(0x96, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a | b));
		executors.put(0x97, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a ^ b));

		// /2addr variants
		executors.put(0xB0, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a + b));
		executors.put(0xB1, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a - b));
		executors.put(0xB2, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a * b));
		executors.put(0xB3, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> b == 0 ? 0 : a / b));
		executors.put(0xB4, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> b == 0 ? 0 : a % b));
		executors.put(0xB5, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a & b));
		executors.put(0xB6, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a | b));
		executors.put(0xB7, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a ^ b));
		executors.put(0xB8, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a << b));
		executors.put(0xB9, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a >> b));
		executors.put(0xBA, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a >>> b));

		// lit8 variants
		executors.put(0xD8, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> a + b));
		executors.put(0xD9, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> b - a)); // rsub
		executors.put(0xDA, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> a * b));
		executors.put(0xDB, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> b == 0 ? 0 : a / b));
		executors.put(0xDC, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> b == 0 ? 0 : a % b));
		executors.put(0xDD, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> a & b));
		executors.put(0xDE, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> a | b));
		executors.put(0xDF, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> a ^ b));
		executors.put(0xE0, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> a << b));
		executors.put(0xE1, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> a >> b));

		// --- ARRAY INSTRUCTIONS ---
		executors.put(0x21, (insn, regs) -> { // array-length
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			Object array = regs.get(regB);
			regs.put(regA, (array != null && array.getClass().isArray()) ? java.lang.reflect.Array.getLength(array) : 0);
		});

		executors.put(0x23, (insn, regs) -> { // new-array
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			int size = safeInt(regs, regB);
			regs.put(regA, createNewArray(insn.getIndex(), size));
		});

		InstructionExecutor agetExec = (insn, regs) -> {
			int[] r = getRegs23x(insn);
			Object array = regs.get(r[1]);
			int index = safeInt(regs, r[2]);
			regs.put(r[0], (array != null && array.getClass().isArray()) ? java.lang.reflect.Array.get(array, index) : null);
		};
		for (int op = 0x44; op <= 0x4A; op++) executors.put(op, agetExec);

		InstructionExecutor aputExec = (insn, regs) -> {
			int[] r = getRegs23x(insn);
			Object array = regs.get(r[1]);
			int index = safeInt(regs, r[2]);
			if (array != null && array.getClass().isArray()) {
				Object val = regs.get(r[0]);
				Class<?> componentType = array.getClass().getComponentType();
				if (componentType.isPrimitive() && val instanceof Number) {
					Number num = (Number) val;
					if (componentType == byte.class) val = num.byteValue();
					else if (componentType == short.class) val = num.shortValue();
					else if (componentType == int.class) val = num.intValue();
					else if (componentType == long.class) val = num.longValue();
					else if (componentType == float.class) val = num.floatValue();
					else if (componentType == double.class) val = num.doubleValue();
					else if (componentType == char.class) val = (char) num.intValue();
					else if (componentType == boolean.class) val = num.intValue() != 0;
				}
				java.lang.reflect.Array.set(array, index, val);
			}
		};
		for (int op = 0x4B; op <= 0x51; op++) executors.put(op, aputExec);

		// --- FIELD ACCESS ---
		for (int op = 0x52; op <= 0x58; op++) executors.put(op, (insn, regs) -> handleIget(insn, regs));
		for (int op = 0x59; op <= 0x5F; op++) executors.put(op, (insn, regs) -> handleIput(insn, regs));
		for (int op = 0x60; op <= 0x66; op++) executors.put(op, (insn, regs) -> handleSget(insn, regs));
		for (int op = 0x67; op <= 0x6D; op++) executors.put(op, (insn, regs) -> handleSput(insn, regs));

		// --- CASTS ---
		executors.put(0x81, (insn, regs) -> { // int-to-long
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			setWideRegister(regs, regA, (long) safeInt(regs, regB));
		});
		executors.put(0x82, (insn, regs) -> { // int-to-float
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			regs.put(regA, (float) safeInt(regs, regB));
		});
		executors.put(0x83, (insn, regs) -> { // int-to-double
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			setDoubleRegister(regs, regA, (double) safeInt(regs, regB));
		});
		executors.put(0x84, (insn, regs) -> { // long-to-int
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			regs.put(regA, (int) safeLong(regs, regB));
		});
		executors.put(0x85, (insn, regs) -> { // long-to-float
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			regs.put(regA, (float) safeLong(regs, regB));
		});
		executors.put(0x86, (insn, regs) -> { // long-to-double
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			setDoubleRegister(regs, regA, (double) safeLong(regs, regB));
		});
		executors.put(0x87, (insn, regs) -> { // float-to-int
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			regs.put(regA, (int) safeFloat(regs, regB));
		});
		executors.put(0x88, (insn, regs) -> { // float-to-long
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			setWideRegister(regs, regA, (long) safeFloat(regs, regB));
		});
		executors.put(0x89, (insn, regs) -> { // float-to-double
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			setDoubleRegister(regs, regA, (double) safeFloat(regs, regB));
		});
		executors.put(0x8A, (insn, regs) -> { // double-to-int
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			regs.put(regA, (int) safeDouble(regs, regB));
		});
		executors.put(0x8B, (insn, regs) -> { // double-to-long
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			setWideRegister(regs, regA, (long) safeDouble(regs, regB));
		});
		executors.put(0x8C, (insn, regs) -> { // double-to-float
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			regs.put(regA, (float) safeDouble(regs, regB));
		});
		executors.put(0x8D, (insn, regs) -> { // int-to-byte
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			regs.put(regA, (int) (byte) safeInt(regs, regB));
		});
		executors.put(0x8E, (insn, regs) -> { // int-to-char
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			regs.put(regA, (int) (char) safeInt(regs, regB));
		});
		executors.put(0x8F, (insn, regs) -> { // int-to-short
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			regs.put(regA, (int) (short) safeInt(regs, regB));
		});

		// --- GOTO FAMILY ---
		InstructionExecutor gotoExec = (insn, regs) -> {
			state.setNextOffset(cfHandler.getGotoTarget(insn));
		};
		executors.put(0x28, gotoExec);
		executors.put(0x29, gotoExec);
		executors.put(0x2A, gotoExec);

		// --- IF FAMILY ---
		InstructionExecutor ifExec = (insn, regs) -> {
			if (cfHandler.shouldBranch(insn)) {
				state.setNextOffset(cfHandler.getBranchTarget(insn));
			}
		};
		for (int op = 0x32; op <= 0x3D; op++) executors.put(op, ifExec);

		// --- INVOKE FAMILY ---
		InstructionExecutor invokeExec = (insn, regs) -> {
			if (indexer != null && insn.getIndex() >= 0 && invokeHandler != null) {
				String signature = indexer.getMethodPool().getMethodSignature(insn.getIndex());
				java.util.List<Object> args = invokeHandler.getInvokeArgs(insn);
				Object result = invokeHandler.handleInvoke(signature, args);
				state.recordResult(signature, insn, result);
			}
		};
		for (int op = 0x6E; op <= 0x78; op++) executors.put(op, invokeExec);

		// --- COMPARISONS ---
		executors.put(0x31, (insn, regs) -> { // cmp-long
			int[] r = getRegs23x(insn);
			long v1 = safeLong(regs, r[1]);
			long v2 = safeLong(regs, r[2]);
			regs.put(r[0], Long.compare(v1, v2));
		});
		executors.put(0x2D, (insn, regs) -> { // cmpl-float
			int[] r = getRegs23x(insn);
			float v1 = safeFloat(regs, r[1]);
			float v2 = safeFloat(regs, r[2]);
			if (Float.isNaN(v1) || Float.isNaN(v2)) {
				regs.put(r[0], -1);
			} else {
				regs.put(r[0], Float.compare(v1, v2));
			}
		});
		executors.put(0x2E, (insn, regs) -> { // cmpg-float
			int[] r = getRegs23x(insn);
			float v1 = safeFloat(regs, r[1]);
			float v2 = safeFloat(regs, r[2]);
			if (Float.isNaN(v1) || Float.isNaN(v2)) {
				regs.put(r[0], 1);
			} else {
				regs.put(r[0], Float.compare(v1, v2));
			}
		});
		executors.put(0x2F, (insn, regs) -> { // cmpl-double
			int[] r = getRegs23x(insn);
			double v1 = safeDouble(regs, r[1]);
			double v2 = safeDouble(regs, r[2]);
			if (Double.isNaN(v1) || Double.isNaN(v2)) {
				regs.put(r[0], -1);
			} else {
				regs.put(r[0], Double.compare(v1, v2));
			}
		});
		executors.put(0x30, (insn, regs) -> { // cmpg-double
			int[] r = getRegs23x(insn);
			double v1 = safeDouble(regs, r[1]);
			double v2 = safeDouble(regs, r[2]);
			if (Double.isNaN(v1) || Double.isNaN(v2)) {
				regs.put(r[0], 1);
			} else {
				regs.put(r[0], Double.compare(v1, v2));
			}
		});

		// --- RETURN FAMILY ---
		executors.put(0x0E, (insn, regs) -> state.setLastResult(null));
		InstructionExecutor returnValExec = (insn, regs) -> {
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0xFF;
			state.setLastResult(regs.get(regA));
		};
		executors.put(0x0F, returnValExec);
		executors.put(0x10, returnValExec);
		executors.put(0x11, returnValExec);

		// --- MONITOR / THROW / SWITCH / CHECK-CAST / FILL-ARRAY-DATA ---
		InstructionExecutor noOpExec = (insn, regs) -> {};
		executors.put(0x1D, noOpExec); // monitor-enter
		executors.put(0x1E, noOpExec); // monitor-exit
		executors.put(0x1F, noOpExec); // check-cast
		executors.put(0x2B, noOpExec); // packed-switch
		executors.put(0x2C, noOpExec); // sparse-switch

		executors.put(0x26, (insn, regs) -> { // fill-array-data
			int[] r = insn.getRegisters();
			if (r == null || r.length == 0) return;
			int regA = r[0];
			Object array = regs.get(regA);
			if (array == null || !array.getClass().isArray()) return;

			int payloadOffset = insn.getOffset() + (int) insn.getLiteral();
			short[] raw = state.getRawCodeUnits();
			if (raw == null || payloadOffset < 0 || payloadOffset + 4 > raw.length) return;

			int magic = raw[payloadOffset] & 0xFFFF;
			if (magic != 0x0300) return; // FILL_ARRAY_DATA_PAYLOAD magic

			int elementWidth = raw[payloadOffset + 1] & 0xFFFF;
			long size = (raw[payloadOffset + 2] & 0xFFFFL) | ((raw[payloadOffset + 3] & 0xFFFFL) << 16);

			if (array instanceof byte[]) {
				byte[] arr = (byte[]) array;
				for (int i = 0; i < arr.length && i < size; i++) {
					int wordIdx = payloadOffset + 4 + (i / 2);
					if (wordIdx >= raw.length) break;
					int byteShift = (i % 2) * 8;
					arr[i] = (byte) ((raw[wordIdx] >> byteShift) & 0xFF);
				}
			} else if (array instanceof boolean[]) {
				boolean[] arr = (boolean[]) array;
				for (int i = 0; i < arr.length && i < size; i++) {
					int wordIdx = payloadOffset + 4 + (i / 2);
					if (wordIdx >= raw.length) break;
					int byteShift = (i % 2) * 8;
					arr[i] = (((raw[wordIdx] >> byteShift) & 0xFF) != 0);
				}
			} else if (array instanceof short[]) {
				short[] arr = (short[]) array;
				for (int i = 0; i < arr.length && i < size; i++) {
					int wordIdx = payloadOffset + 4 + i;
					if (wordIdx >= raw.length) break;
					arr[i] = raw[wordIdx];
				}
			} else if (array instanceof char[]) {
				char[] arr = (char[]) array;
				for (int i = 0; i < arr.length && i < size; i++) {
					int wordIdx = payloadOffset + 4 + i;
					if (wordIdx >= raw.length) break;
					arr[i] = (char) raw[wordIdx];
				}
			} else if (array instanceof int[]) {
				int[] arr = (int[]) array;
				for (int i = 0; i < arr.length && i < size; i++) {
					int wordIdx = payloadOffset + 4 + i * 2;
					if (wordIdx + 1 >= raw.length) break;
					arr[i] = (raw[wordIdx] & 0xFFFF) | ((raw[wordIdx + 1] & 0xFFFF) << 16);
				}
			} else if (array instanceof float[]) {
				float[] arr = (float[]) array;
				for (int i = 0; i < arr.length && i < size; i++) {
					int wordIdx = payloadOffset + 4 + i * 2;
					if (wordIdx + 1 >= raw.length) break;
					int val = (raw[wordIdx] & 0xFFFF) | ((raw[wordIdx + 1] & 0xFFFF) << 16);
					arr[i] = Float.intBitsToFloat(val);
				}
			} else if (array instanceof long[]) {
				long[] arr = (long[]) array;
				for (int i = 0; i < arr.length && i < size; i++) {
					int wordIdx = payloadOffset + 4 + i * 4;
					if (wordIdx + 3 >= raw.length) break;
					long val = (raw[wordIdx] & 0xFFFFL)
							| ((raw[wordIdx + 1] & 0xFFFFL) << 16)
							| ((raw[wordIdx + 2] & 0xFFFFL) << 32)
							| ((raw[wordIdx + 3] & 0xFFFFL) << 48);
					arr[i] = val;
				}
			} else if (array instanceof double[]) {
				double[] arr = (double[]) array;
				for (int i = 0; i < arr.length && i < size; i++) {
					int wordIdx = payloadOffset + 4 + i * 4;
					if (wordIdx + 3 >= raw.length) break;
					long val = (raw[wordIdx] & 0xFFFFL)
							| ((raw[wordIdx + 1] & 0xFFFFL) << 16)
							| ((raw[wordIdx + 2] & 0xFFFFL) << 32)
							| ((raw[wordIdx + 3] & 0xFFFFL) << 48);
					arr[i] = Double.longBitsToDouble(val);
				}
			}
		});

		executors.put(0x27, (insn, regs) -> { // throw
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0xFF;
			state.setLastResult(regs.get(regA));
		});

		executors.put(0x20, (insn, regs) -> { // instance-of
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			regs.put(regA, 1); // Mocked to always true (1)
		});

		// --- FILLED-NEW-ARRAY ---
		executors.put(0x24, (insn, regs) -> { // filled-new-array
			short[] units = insn.getUnits();
			int regCount = (units[0] >> 12) & 0x0F;
			int g = (units[0] >> 8) & 0x0F;
			if (units.length >= 3) {
				int c = units[2] & 0x0F;
				int d = (units[2] >> 4) & 0x0F;
				int e = (units[2] >> 8) & 0x0F;
				int f = (units[2] >> 12) & 0x0F;
				int[] argsRegs = {c, d, e, f, g};
				Object[] arr = new Object[regCount];
				for (int idx = 0; idx < Math.min(regCount, 5); idx++) {
					arr[idx] = regs.get(argsRegs[idx]);
				}
				state.setLastResult(arr);
			}
		});

		executors.put(0x25, (insn, regs) -> { // filled-new-array/range
			short[] units = insn.getUnits();
			int regCount = (units[0] >> 8) & 0xFF;
			if (units.length >= 3) {
				int startReg = units[2] & 0xFFFF;
				Object[] arr = new Object[regCount];
				for (int idx = 0; idx < regCount; idx++) {
					arr[idx] = regs.get(startReg + idx);
				}
				state.setLastResult(arr);
			}
		});

		// --- CONST-WIDE ---
		InstructionExecutor constWideExec = (insn, regs) -> {
			int regA = (insn.getRegisters() != null && insn.getRegisters().length > 0)
					? insn.getRegisters()[0]
					: ((insn.getUnits()[0] >> 8) & 0xFF);
			setWideRegister(regs, regA, insn.getLiteral());
		};
		for (int op = 0x16; op <= 0x19; op++) executors.put(op, constWideExec);

		// --- UNARY OPS ---
		executors.put(0x7B, (insn, regs) -> { // neg-int
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			regs.put(regA, -safeInt(regs, regB));
		});
		executors.put(0x7C, (insn, regs) -> { // not-int
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			regs.put(regA, ~safeInt(regs, regB));
		});
		executors.put(0x7D, (insn, regs) -> { // neg-long
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			setWideRegister(regs, regA, -safeLong(regs, regB));
		});
		executors.put(0x7E, (insn, regs) -> { // not-long
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			setWideRegister(regs, regA, ~safeLong(regs, regB));
		});
		executors.put(0x7F, (insn, regs) -> { // neg-float
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			regs.put(regA, -safeFloat(regs, regB));
		});
		executors.put(0x80, (insn, regs) -> { // neg-double
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			setDoubleRegister(regs, regA, -safeDouble(regs, regB));
		});

		// --- SHL / USHR EXTENSIONS ---
		executors.put(0x98, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a << b));
		executors.put(0x99, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a >> b));
		executors.put(0x9A, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a >>> b));
		executors.put(0xE2, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> a >>> b));

		// --- LIT16 ARITHMETIC ---
		executors.put(0xD0, (insn, regs) -> applyArithmeticLit16(insn, regs, (a, b) -> a + b));
		executors.put(0xD1, (insn, regs) -> applyArithmeticLit16(insn, regs, (a, b) -> b - a)); // rsub
		executors.put(0xD2, (insn, regs) -> applyArithmeticLit16(insn, regs, (a, b) -> a * b));
		executors.put(0xD3, (insn, regs) -> applyArithmeticLit16(insn, regs, (a, b) -> b == 0 ? 0 : a / b));
		executors.put(0xD4, (insn, regs) -> applyArithmeticLit16(insn, regs, (a, b) -> b == 0 ? 0 : a % b));
		executors.put(0xD5, (insn, regs) -> applyArithmeticLit16(insn, regs, (a, b) -> a & b));
		executors.put(0xD6, (insn, regs) -> applyArithmeticLit16(insn, regs, (a, b) -> a | b));
		executors.put(0xD7, (insn, regs) -> applyArithmeticLit16(insn, regs, (a, b) -> a ^ b));

		// --- LONG ARITHMETIC ---
		executors.put(0x9B, (insn, regs) -> applyLongArith(insn, regs, (a, b) -> a + b));
		executors.put(0x9C, (insn, regs) -> applyLongArith(insn, regs, (a, b) -> a - b));
		executors.put(0x9D, (insn, regs) -> applyLongArith(insn, regs, (a, b) -> a * b));
		executors.put(0x9E, (insn, regs) -> applyLongArith(insn, regs, (a, b) -> b == 0 ? 0L : a / b));
		executors.put(0x9F, (insn, regs) -> applyLongArith(insn, regs, (a, b) -> b == 0 ? 0L : a % b));
		executors.put(0xA0, (insn, regs) -> applyLongArith(insn, regs, (a, b) -> a & b));
		executors.put(0xA1, (insn, regs) -> applyLongArith(insn, regs, (a, b) -> a | b));
		executors.put(0xA2, (insn, regs) -> applyLongArith(insn, regs, (a, b) -> a ^ b));
		executors.put(0xA3, (insn, regs) -> applyLongArith(insn, regs, (a, b) -> a << b));
		executors.put(0xA4, (insn, regs) -> applyLongArith(insn, regs, (a, b) -> a >> b));
		executors.put(0xA5, (insn, regs) -> applyLongArith(insn, regs, (a, b) -> a >>> b));

		executors.put(0xBB, (insn, regs) -> applyLongArith2addr(insn, regs, (a, b) -> a + b));
		executors.put(0xBC, (insn, regs) -> applyLongArith2addr(insn, regs, (a, b) -> a - b));
		executors.put(0xBD, (insn, regs) -> applyLongArith2addr(insn, regs, (a, b) -> a * b));
		executors.put(0xBE, (insn, regs) -> applyLongArith2addr(insn, regs, (a, b) -> b == 0 ? 0L : a / b));
		executors.put(0xBF, (insn, regs) -> applyLongArith2addr(insn, regs, (a, b) -> b == 0 ? 0L : a % b));
		executors.put(0xC0, (insn, regs) -> applyLongArith2addr(insn, regs, (a, b) -> a & b));
		executors.put(0xC1, (insn, regs) -> applyLongArith2addr(insn, regs, (a, b) -> a | b));
		executors.put(0xC2, (insn, regs) -> applyLongArith2addr(insn, regs, (a, b) -> a ^ b));
		executors.put(0xC3, (insn, regs) -> applyLongArith2addr(insn, regs, (a, b) -> a << b));
		executors.put(0xC4, (insn, regs) -> applyLongArith2addr(insn, regs, (a, b) -> a >> b));
		executors.put(0xC5, (insn, regs) -> applyLongArith2addr(insn, regs, (a, b) -> a >>> b));

		// --- FLOAT ARITHMETIC ---
		executors.put(0xA6, (insn, regs) -> applyFloatArith(insn, regs, (a, b) -> a + b));
		executors.put(0xA7, (insn, regs) -> applyFloatArith(insn, regs, (a, b) -> a - b));
		executors.put(0xA8, (insn, regs) -> applyFloatArith(insn, regs, (a, b) -> a * b));
		executors.put(0xA9, (insn, regs) -> applyFloatArith(insn, regs, (a, b) -> b == 0.0f ? Float.NaN : a / b));
		executors.put(0xAA, (insn, regs) -> applyFloatArith(insn, regs, (a, b) -> a % b));

		executors.put(0xC6, (insn, regs) -> applyFloatArith2addr(insn, regs, (a, b) -> a + b));
		executors.put(0xC7, (insn, regs) -> applyFloatArith2addr(insn, regs, (a, b) -> a - b));
		executors.put(0xC8, (insn, regs) -> applyFloatArith2addr(insn, regs, (a, b) -> a * b));
		executors.put(0xC9, (insn, regs) -> applyFloatArith2addr(insn, regs, (a, b) -> b == 0.0f ? Float.NaN : a / b));
		executors.put(0xCA, (insn, regs) -> applyFloatArith2addr(insn, regs, (a, b) -> a % b));

		// --- DOUBLE ARITHMETIC ---
		executors.put(0xAB, (insn, regs) -> applyDoubleArith(insn, regs, (a, b) -> a + b));
		executors.put(0xAC, (insn, regs) -> applyDoubleArith(insn, regs, (a, b) -> a - b));
		executors.put(0xAD, (insn, regs) -> applyDoubleArith(insn, regs, (a, b) -> a * b));
		executors.put(0xAE, (insn, regs) -> applyDoubleArith(insn, regs, (a, b) -> b == 0.0d ? Double.NaN : a / b));
		executors.put(0xAF, (insn, regs) -> applyDoubleArith(insn, regs, (a, b) -> a % b));

		executors.put(0xCB, (insn, regs) -> applyDoubleArith2addr(insn, regs, (a, b) -> a + b));
		executors.put(0xCC, (insn, regs) -> applyDoubleArith2addr(insn, regs, (a, b) -> a - b));
		executors.put(0xCD, (insn, regs) -> applyDoubleArith2addr(insn, regs, (a, b) -> a * b));
		executors.put(0xCE, (insn, regs) -> applyDoubleArith2addr(insn, regs, (a, b) -> b == 0.0d ? Double.NaN : a / b));
		executors.put(0xCF, (insn, regs) -> applyDoubleArith2addr(insn, regs, (a, b) -> a % b));
	}

	private int safeInt(Map<Integer, Object> regs, int reg) {
		Object val = regs.get(reg);
		if (val instanceof Number) return ((Number) val).intValue();
		return 0;
	}

	private long safeLong(Map<Integer, Object> regs, int reg) {
		Object val = regs.get(reg);
		if (val instanceof Number) return ((Number) val).longValue();
		if (val instanceof Boolean) return ((Boolean) val) ? 1L : 0L;
		if (val instanceof Character) return (long) ((Character) val).charValue();
		return 0L;
	}

	private float safeFloat(Map<Integer, Object> regs, int reg) {
		Object val = regs.get(reg);
		if (val instanceof Number) return ((Number) val).floatValue();
		return 0.0f;
	}

	private double safeDouble(Map<Integer, Object> regs, int reg) {
		Object val = regs.get(reg);
		if (val instanceof Number) return ((Number) val).doubleValue();
		return 0.0;
	}

	private Object createNewInstance(int typeIdx) {
		if (indexer == null || typeIdx < 0) return new Object();
		String type = indexer.getTypePool().getTypeName(typeIdx);
		try {
			if (type.equals("Ljava/lang/StringBuilder;")) return new StringBuilder();
			if (type.equals("Ljava/lang/StringBuffer;")) return new StringBuffer();
			if (type.equals("Ljava/util/HashMap;")) return new HashMap<>();
			if (type.equals("Ljava/util/ArrayList;")) return new java.util.ArrayList<>();
		} catch (Exception ignored) {
		}
		return new Object(); // Generic mock object
	}

	private Object createNewArray(int typeIdx, int size) {
		if (size < 0) size = 0;
		if (indexer == null || typeIdx < 0) return new Object[size];

		String type = null;
		if (typeIdx < indexer.getTypePool().getSize()) {
			type = indexer.getTypePool().getTypeName(typeIdx);
		}

		if (type == null) return new Object[size];

		// Handle Dalvik array types (e.g. "[B", "[I")
		if (type.startsWith("[")) {
			char element = type.charAt(1);
			switch (element) {
				case 'B': return new byte[size];
				case 'I': return new int[size];
				case 'C': return new char[size];
				case 'S': return new short[size];
				case 'J': return new long[size];
				case 'Z': return new boolean[size];
				case 'F': return new float[size];
				case 'D': return new double[size];
			}
		}

		return new Object[size];
	}

	private void applyArithmetic(DexInstruction insn, Map<Integer, Object> regs, BiFunction<Integer, Integer, Integer> op) {
		int[] r = getRegs23x(insn);
		int v1 = safeInt(regs, r[1]);
		int v2 = safeInt(regs, r[2]);
		regs.put(r[0], op.apply(v1, v2));
	}

	private void applyArithmetic2addr(DexInstruction insn, Map<Integer, Object> regs, BiFunction<Integer, Integer, Integer> op) {
		short[] units = insn.getUnits();
		int regA = (units[0] >> 8) & 0x0F;
		int regB = (units[0] >> 12) & 0x0F;
		int v1 = safeInt(regs, regA);
		int v2 = safeInt(regs, regB);
		regs.put(regA, op.apply(v1, v2));
	}

	private void applyArithmeticLit8(DexInstruction insn, Map<Integer, Object> regs, BiFunction<Integer, Integer, Integer> op) {
		int regA, regB, litC;
		int[] r = insn.getRegisters();
		if (r != null && r.length >= 2) {
			regA = r[0];
			regB = r[1];
			litC = (int) insn.getLiteral();
		} else {
			short[] units = insn.getUnits();
			regA = (units[0] >> 8) & 0x0F;
			regB = (units[0] >> 12) & 0x0F;
			litC = units[1] & 0xFF;
			if ((litC & 0x80) != 0) litC |= 0xFFFFFF00;
		}

		int v1 = safeInt(regs, regB);
		regs.put(regA, op.apply(v1, litC));
	}

	private void handleIget(DexInstruction insn, Map<Integer, Object> regs) {
		short[] units = insn.getUnits();
		int regA = (units[0] >> 8) & 0x0F;
		int regB = (units[0] >> 12) & 0x0F;
		Object instance = regs.get(regB);
		regs.put(regA, state.getInstanceField(instance, getFieldSig(insn.getIndex())));
	}

	private void handleIput(DexInstruction insn, Map<Integer, Object> regs) {
		short[] units = insn.getUnits();
		int regA = (units[0] >> 8) & 0x0F;
		int regB = (units[0] >> 12) & 0x0F;
		Object instance = regs.get(regB);
		state.setInstanceField(instance, getFieldSig(insn.getIndex()), regs.get(regA));
	}

	private void handleSget(DexInstruction insn, Map<Integer, Object> regs) {
		short[] units = insn.getUnits();
		int regA = (units[0] >> 8) & 0xFF;
		regs.put(regA, state.getStaticField(getFieldSig(insn.getIndex())));
	}

	private void handleSput(DexInstruction insn, Map<Integer, Object> regs) {
		short[] units = insn.getUnits();
		int regA = (units[0] >> 8) & 0xFF;
		state.setStaticField(getFieldSig(insn.getIndex()), regs.get(regA));
	}

	private String getFieldSig(int index) {
		if (indexer == null || index < 0) return "field_" + index;
		return indexer.getFieldPool().getFieldSignature(index);
	}

	private void handleIntCast(DexInstruction insn, Map<Integer, Object> regs, Function<Number, Integer> cast) {
		short[] units = insn.getUnits();
		int regA = (units[0] >> 8) & 0x0F;
		int regB = (units[0] >> 12) & 0x0F;
		Object val = regs.get(regB);
		if (val instanceof Number) regs.put(regA, cast.apply((Number) val));
	}

	private int[] getRegs23x(DexInstruction insn) {
		short[] units = insn.getUnits();
		int a = (units[0] >> 8) & 0xFF;
		int b = units[1] & 0xFF;
		int c = (units[1] >> 8) & 0xFF;
		return new int[]{a, b, c};
	}

	private void applyArithmeticLit16(DexInstruction insn, Map<Integer, Object> regs, BiFunction<Integer, Short, Integer> op) {
		short[] units = insn.getUnits();
		int regA = (units[0] >> 8) & 0x0F;
		int regB = (units[0] >> 12) & 0x0F;
		short litC = units[1];
		int v1 = safeInt(regs, regB);
		regs.put(regA, op.apply(v1, litC));
	}

	private void applyLongArith(DexInstruction insn, Map<Integer, Object> regs, BiFunction<Long, Long, Long> op) {
		int[] r = getRegs23x(insn);
		long v1 = safeLong(regs, r[1]);
		long v2 = safeLong(regs, r[2]);
		setWideRegister(regs, r[0], op.apply(v1, v2));
	}

	private void applyLongArith2addr(DexInstruction insn, Map<Integer, Object> regs, BiFunction<Long, Long, Long> op) {
		short[] units = insn.getUnits();
		int regA = (units[0] >> 8) & 0x0F;
		int regB = (units[0] >> 12) & 0x0F;
		long v1 = safeLong(regs, regA);
		long v2 = safeLong(regs, regB);
		setWideRegister(regs, regA, op.apply(v1, v2));
	}

	private void applyFloatArith(DexInstruction insn, Map<Integer, Object> regs, BiFunction<Float, Float, Float> op) {
		int[] r = getRegs23x(insn);
		float v1 = safeFloat(regs, r[1]);
		float v2 = safeFloat(regs, r[2]);
		regs.put(r[0], op.apply(v1, v2));
	}

	private void applyFloatArith2addr(DexInstruction insn, Map<Integer, Object> regs, BiFunction<Float, Float, Float> op) {
		short[] units = insn.getUnits();
		int regA = (units[0] >> 8) & 0x0F;
		int regB = (units[0] >> 12) & 0x0F;
		float v1 = safeFloat(regs, regA);
		float v2 = safeFloat(regs, regB);
		regs.put(regA, op.apply(v1, v2));
	}

	private void applyDoubleArith(DexInstruction insn, Map<Integer, Object> regs, BiFunction<Double, Double, Double> op) {
		int[] r = getRegs23x(insn);
		double v1 = safeDouble(regs, r[1]);
		double v2 = safeDouble(regs, r[2]);
		setDoubleRegister(regs, r[0], op.apply(v1, v2));
	}

	private void applyDoubleArith2addr(DexInstruction insn, Map<Integer, Object> regs, BiFunction<Double, Double, Double> op) {
		short[] units = insn.getUnits();
		int regA = (units[0] >> 8) & 0x0F;
		int regB = (units[0] >> 12) & 0x0F;
		double v1 = safeDouble(regs, regA);
		double v2 = safeDouble(regs, regB);
		setDoubleRegister(regs, regA, op.apply(v1, v2));
	}
}
