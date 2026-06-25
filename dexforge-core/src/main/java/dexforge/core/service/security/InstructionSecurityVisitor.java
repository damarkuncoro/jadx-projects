package dexforge.core.service.security;

import dexforge.core.parser.dex.analysis.visitor.DexInstructionVisitor;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.service.security.model.VulnerabilityIssue;
import java.util.ArrayList;
import java.util.List;

/**
 * REUSEABLE: Instruction-level security visitor.
 * Can be used by both the Scanner and the Decompiler for inline alerts.
 */
public final class InstructionSecurityVisitor implements DexInstructionVisitor {
    private final DexFastIndexer indexer;
    private final List<VulnerabilityIssue> issues = new ArrayList<>();

    public InstructionSecurityVisitor(DexFastIndexer indexer) {
        this.indexer = indexer;
    }

    public List<VulnerabilityIssue> getIssues() { return issues; }

    @Override
    public void visitInvoke(DexInstruction insn) {
        String sig = indexer.getMethodPool().getMethodSignature(insn.getIndex());
        if (sig.contains("openFileOutput") && sig.contains(",I)")) {
            issues.add(new VulnerabilityIssue("File Security", "Potential insecure file creation",
                       VulnerabilityIssue.Severity.MEDIUM, "Offset: " + insn.getOffset()));
        } else if (sig.contains("Cipher;->getInstance")) {
            issues.add(new VulnerabilityIssue("Cryptography", "Custom cipher instance",
                       VulnerabilityIssue.Severity.INFO, "Offset: " + insn.getOffset()));
        }
    }

    @Override public void visitConst(DexInstruction insn) {}
    @Override public void visitString(DexInstruction insn) {}
    @Override public void visitClass(DexInstruction insn) {}
    @Override public void visitMove(DexInstruction insn) {}
    @Override public void visitReturn(DexInstruction insn) {}
    @Override public void visitJump(DexInstruction insn) {}
    @Override public void visitFieldAccess(DexInstruction insn) {}
    @Override public void visitArithmetic(DexInstruction insn) {}
    @Override public void visitUnknown(DexInstruction insn) {}
}
