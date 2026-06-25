package dexforge.core.parser.analysis.emulator;

import dexforge.core.parser.dex.model.DexInstruction;
import java.util.Map;

/**
 * Handles control flow logic such as jumps (GOTO) and conditional branches (IF-*).
 */
public final class ControlFlowHandler {
    private final Map<Integer, Object> registers;

    public ControlFlowHandler(Map<Integer, Object> registers) {
        this.registers = registers;
    }

    public int getGotoTarget(DexInstruction insn) {
        int op = insn.getOpcode() & 0xFF;
        short[] units = insn.getUnits();
        if (op == 0x28) {
            int offsetVal = (insn.getOpcode() >> 8) & 0xFF;
            if (offsetVal >= 128) offsetVal -= 256;
            return insn.getOffset() + offsetVal;
        } else if (op == 0x29) {
            if (units != null && units.length >= 2) return insn.getOffset() + units[1];
        } else if (op == 0x2A) {
            if (units != null && units.length >= 3) {
                int relative = (units[1] & 0xFFFF) | (units[2] << 16);
                return insn.getOffset() + relative;
            }
        }
        return insn.getOffset() + insn.getLength();
    }

    public int getBranchTarget(DexInstruction insn) {
        short[] units = insn.getUnits();
        if (units != null && units.length >= 2) return insn.getOffset() + units[1];
        return insn.getOffset() + insn.getLength();
    }

    public boolean shouldBranch(DexInstruction insn) {
        int op = insn.getOpcode() & 0xFF;
        short[] units = insn.getUnits();
        if (units == null || units.length < 1) return false;

        if (op >= 0x32 && op <= 0x37) {
            int regA = (units[0] >> 8) & 0x0F;
            int regB = (units[0] >> 12) & 0x0F;
            return compareValues(registers.get(regA), registers.get(regB), op);
        } else if (op >= 0x38 && op <= 0x3D) {
            int regA = (units[0] >> 8) & 0xFF;
            Object valA = registers.get(regA);
            long a = getValAsLong(valA);
            switch (op) {
                case 0x38: return valA == null || a == 0;
                case 0x39: return valA != null && a != 0;
                case 0x3A: return a < 0;
                case 0x3B: return a >= 0;
                case 0x3C: return a > 0;
                case 0x3D: return a <= 0;
                default: return false;
            }
        }
        return false;
    }

    private boolean compareValues(Object valA, Object valB, int op) {
        if (isObjectRef(valA) || isObjectRef(valB)) {
            switch (op) {
                case 0x32: return valA == valB;
                case 0x33: return valA != valB;
                default: return false;
            }
        }
        long a = getValAsLong(valA);
        long b = getValAsLong(valB);
        switch (op) {
            case 0x32: return a == b;
            case 0x33: return a != b;
            case 0x34: return a < b;
            case 0x35: return a >= b;
            case 0x36: return a > b;
            case 0x37: return a <= b;
            default: return false;
        }
    }

    private long getValAsLong(Object val) {
        if (val == null) return 0;
        if (val instanceof Boolean) return ((Boolean) val) ? 1 : 0;
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof Character) return (long) ((Character) val).charValue();
        return 1;
    }

    private boolean isObjectRef(Object val) {
        return val != null && !(val instanceof Number || val instanceof Boolean || val instanceof Character);
    }
}
