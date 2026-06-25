package dexforge.core.parser.dex.analysis.visitor;

import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.sections.DexOpcode;
import java.util.List;

/**
 * Dispatches instructions to a visitor using centralized opcode categorization.
 * Follows DRY and SOLID principles.
 */
public final class InstructionDispatcher {

    public static void dispatch(DexInstruction insn, DexInstructionVisitor visitor) {
        int op = insn.getOpcode();

        if (DexOpcode.isString(op)) {
            visitor.visitString(insn);
        } else if (DexOpcode.isConst(op)) {
            visitor.visitConst(insn);
        } else if (op == 0x1C) {
            visitor.visitClass(insn);
        } else if (op >= 0x01 && op <= 0x0D) {
            visitor.visitMove(insn);
        } else if (op >= 0x0E && op <= 0x11) {
            visitor.visitReturn(insn);
        } else if (op >= 0x28 && op <= 0x3D) {
            visitor.visitJump(insn);
        } else if (DexOpcode.isInvoke(op)) {
            visitor.visitInvoke(insn);
        } else if (DexOpcode.isFieldAccess(op)) {
            visitor.visitFieldAccess(insn);
        } else if (DexOpcode.isArithmetic(op)) {
            visitor.visitArithmetic(insn);
        } else {
            visitor.visitUnknown(insn);
        }
    }

    public static void dispatchAll(List<DexInstruction> instructions, DexInstructionVisitor visitor) {
        for (DexInstruction insn : instructions) {
            dispatch(insn, visitor);
        }
    }
}
