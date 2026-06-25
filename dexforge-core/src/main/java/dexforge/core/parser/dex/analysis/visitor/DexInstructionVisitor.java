package dexforge.core.parser.dex.analysis.visitor;

import dexforge.core.parser.dex.model.DexInstruction;

/**
 * Interface for the Visitor pattern on Dalvik instructions.
 * Promotes SOLID principles by decoupling instruction walking from specific analysis.
 */
public interface DexInstructionVisitor {
    void visitConst(DexInstruction insn);
    void visitString(DexInstruction insn);
    void visitClass(DexInstruction insn);
    void visitMove(DexInstruction insn);
    void visitReturn(DexInstruction insn);
    void visitJump(DexInstruction insn);
    void visitInvoke(DexInstruction insn);
    void visitFieldAccess(DexInstruction insn);
    void visitArithmetic(DexInstruction insn);
    void visitUnknown(DexInstruction insn);
}
