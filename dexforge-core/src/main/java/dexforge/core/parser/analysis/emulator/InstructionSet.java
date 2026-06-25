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
	private final MethodInvokeHandler invokeHandler;

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

		executors.put(0x1A, (insn, regs) -> {
			int[] r = insn.getRegisters();
			if (r != null && r.length > 0 && indexer != null) {
				String val = null;
				if (insn.getIndex() >= 0 && insn.getIndex() < indexer.getStringPool().getSize()) {
					val = indexer.getStringPool().getString(insn.getIndex());
				}
				// Cross-DEX string lookup fallback
				if (val == null && state != null) {
					// Logic to search in other indexers can be added here
				}
				regs.put(r[0], val);
			}
		});

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
		executors.put(0x94, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a % b));
		executors.put(0x95, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a & b));
		executors.put(0x96, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a | b));
		executors.put(0x97, (insn, regs) -> applyArithmetic(insn, regs, (a, b) -> a ^ b));

		// /2addr variants
		executors.put(0xB0, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a + b));
		executors.put(0xB1, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a - b));
		executors.put(0xB2, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a * b));
		executors.put(0xB5, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a & b));
		executors.put(0xB6, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a | b));
		executors.put(0xB7, (insn, regs) -> applyArithmetic2addr(insn, regs, (a, b) -> a ^ b));

		// lit8 variants
		executors.put(0xD8, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> a + b));
		executors.put(0xD9, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> b - a)); // rsub
		executors.put(0xDA, (insn, regs) -> applyArithmeticLit8(insn, regs, (a, b) -> a * b));
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
		executors.put(0x81, (insn, regs) -> handleIntCast(insn, regs, n -> n.intValue() & 0xFF)); // int-to-byte (approx)
		executors.put(0x8D, (insn, regs) -> handleIntCast(insn, regs, n -> (int) n.byteValue()));
		executors.put(0x8E, (insn, regs) -> handleIntCast(insn, regs, n -> (int) n.shortValue()));
		executors.put(0x8F, (insn, regs) -> {
			short[] units = insn.getUnits();
			int regA = (units[0] >> 8) & 0x0F;
			int regB = (units[0] >> 12) & 0x0F;
			Object val = regs.get(regB);
			if (val instanceof Number) regs.put(regA, (int) ((Number) val).shortValue()); // charValue not in Number, use short
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
		executors.put(0x2D, (insn, regs) -> { // cmp-long
			int[] r = getRegs23x(insn);
			long v1 = safeLong(regs, r[1]);
			long v2 = safeLong(regs, r[2]);
			regs.put(r[0], Long.compare(v1, v2));
		});
		InstructionExecutor cmpFloat = (insn, regs) -> { // cmpl/cmpg-float
			int[] r = getRegs23x(insn);
			float v1 = safeFloat(regs, r[1]);
			float v2 = safeFloat(regs, r[2]);
			boolean isG = (insn.getOpcode() & 0xFF) == 0x2F;
			if (Float.isNaN(v1) || Float.isNaN(v2)) {
				regs.put(r[0], isG ? 1 : -1);
			} else {
				regs.put(r[0], Float.compare(v1, v2));
			}
		};
		executors.put(0x2E, cmpFloat);
		executors.put(0x2F, cmpFloat);
		InstructionExecutor cmpDouble = (insn, regs) -> { // cmpl/cmpg-double
			int[] r = getRegs23x(insn);
			double v1 = safeDouble(regs, r[1]);
			double v2 = safeDouble(regs, r[2]);
			boolean isG = (insn.getOpcode() & 0xFF) == 0x31;
			if (Double.isNaN(v1) || Double.isNaN(v2)) {
				regs.put(r[0], isG ? 1 : -1);
			} else {
				regs.put(r[0], Double.compare(v1, v2));
			}
		};
		executors.put(0x30, cmpDouble);
		executors.put(0x31, cmpDouble);
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
}
