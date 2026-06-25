package dexforge.core.parser.analysis.dataflow;

import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.sections.DexOpcode;
import java.util.HashMap;
import java.util.Map;

/**
 * A lightweight analyzer for tracking register values (Constants).
 * Designed for scalability and reuse across different deobfuscators.
 */
public final class SimpleDataFlowAnalyzer {
	private final Map<Integer, Long> registerConstants = new HashMap<>();

	/**
	 * Processes a single instruction to update the internal state of registers.
	 */
	public void analyze(DexInstruction insn) {
		int op = insn.getOpcode() & 0xFF;

		if (isConst(op)) {
			handleConst(insn);
			return;
		}

		if (isMove(op)) {
			handleMove(insn);
			return;
		}

		// Any other instruction that writes to a register clobbers its constant value
		invalidateClobbered(insn);
	}

	private boolean isConst(int op) {
		return op >= 0x12 && op <= 0x15;
	}

	private boolean isMove(int op) {
		return op >= 0x01 && op <= 0x09;
	}

	private void handleConst(DexInstruction insn) {
		int[] regs = insn.getRegisters();
		if (regs != null && regs.length > 0) {
			registerConstants.put(regs[0], insn.getLiteral());
		}
	}

	private void handleMove(DexInstruction insn) {
		int[] regs = insn.getRegisters();
		if (regs != null && regs.length >= 2) {
			int dest = regs[0];
			int src = regs[1];
			Long value = registerConstants.get(src);
			if (value != null) {
				registerConstants.put(dest, value);
			} else {
				registerConstants.remove(dest);
			}
		}
	}

	private void invalidateClobbered(DexInstruction insn) {
		int destReg = DexOpcode.getDestinationRegister(insn.getOpcode());
		if (destReg != -1) {
			int op = insn.getOpcode() & 0xFF;
			// If it's not handled by handleConst or handleMove, it's clobbered
			if (!isConst(op) && !isMove(op)) {
				registerConstants.remove(destReg);
			}
		}
	}

	public Long getConstant(int register) {
		return registerConstants.get(register);
	}

	public Map<Integer, Long> getAllConstants() {
		return new HashMap<>(registerConstants);
	}

	public void clear() {
		registerConstants.clear();
	}
}
