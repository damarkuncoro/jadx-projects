package dexforge.core.service.annotation;

import dexforge.core.parser.dex.analysis.visitor.InstructionDispatcher;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.service.security.InstructionSecurityVisitor;
import dexforge.core.service.security.model.VulnerabilityIssue;
import dexforge.core.parser.analysis.deobf.specific.BcaStringDecryptor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REUSEABLE service that decorates code with insights from multiple analyzers.
 * Connects Security, Resources, JNI, and Decompiler.
 */
public final class CodeInsightAnnotator {
    private final DexFastIndexer indexer;
    private Map<String, String> jniBridges;

    public CodeInsightAnnotator(DexFastIndexer indexer) {
        this.indexer = indexer;
    }

    public void setJniBridges(Map<String, String> jniBridges) {
        this.jniBridges = jniBridges;
    }

    /**
     * Generates an inline comment for a specific instruction based on all known insights.
     */
    public String getInsightComment(DexInstruction insn) {
        StringBuilder sb = new StringBuilder();

        // 1. Security Insights
        InstructionSecurityVisitor securityVisitor = new InstructionSecurityVisitor(indexer);
        InstructionDispatcher.dispatch(insn, securityVisitor);

        List<VulnerabilityIssue> issues = securityVisitor.getIssues();
        if (!issues.isEmpty()) {
            sb.append(" // SECURITY: ").append(issues.stream()
                .map(VulnerabilityIssue::getDescription)
                .collect(Collectors.joining(", ")));
        }

        // 2. JNI Bridge Insights
        if (jniBridges != null) {
            int op = insn.getOpcode() & 0xFF;
            if (op >= 0x6E && op <= 0x72) { // INVOKE
                String sig = indexer.getMethodPool().getMethodSignature(insn.getIndex());
                String javaName = mapSigToJava(sig);
                if (jniBridges.containsKey(javaName)) {
                    if (sb.length() > 0) sb.append(" | ");
                    else sb.append(" // ");
                    sb.append("JNI -> ").append(jniBridges.get(javaName));
                }
            }

            // 3. BCA String Decryption Insights
            if (op >= 0x6E && op <= 0x72) {
                String sig = indexer.getMethodPool().getMethodSignature(insn.getIndex());
                if (sig.contains("Lo/zzmt;->b(I)")) {
                    // Try to find the constant argument from previous instruction (simplified)
                    if (sb.length() > 0) sb.append(" | ");
                    else sb.append(" // ");
                    sb.append("DECRYPTED -> ").append(BcaStringDecryptor.resolve(insn.getIndex()));
                }
            }
        }

        return sb.toString();
    }

    private String mapSigToJava(String sig) {
        // sig: Lcom/pkg/Cls;->mth(I)V
        // result: com.pkg.Cls.mth
        if (sig.startsWith("L")) sig = sig.substring(1);
        int arrow = sig.indexOf("->");
        if (arrow == -1) return sig;

        String cls = sig.substring(0, arrow).replace('/', '.');
        int paren = sig.indexOf('(', arrow);
        String mth = sig.substring(arrow + 2, paren);
        return cls + "." + mth;
    }
}
