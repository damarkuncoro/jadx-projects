package dexforge.core.service.security.rules;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexCode;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.dex.sections.DexInstructionDecoder;
import dexforge.core.parser.analysis.dataflow.InterProceduralAnalyzer;
import dexforge.core.parser.analysis.dataflow.model.MethodSummary;
import dexforge.core.service.security.model.VulnerabilityIssue;
import java.util.List;
import java.util.Map;

/**
 * SOLID: Advanced rule to detect chains of Crypto-to-Comparison logic.
 * Enhanced with Inter-procedural Semantic Tracking to detect logic across method boundaries.
 */
public final class CryptoDecisionRule implements SecurityRule {
    private InterProceduralAnalyzer ipDfa;

    public void setInterProceduralAnalyzer(InterProceduralAnalyzer ipDfa) {
        this.ipDfa = ipDfa;
    }

    @Override
    public void execute(ApkLoader loader, List<VulnerabilityIssue> issues) {
        for (DexFastIndexer indexer : loader.getIndexers()) {
            for (DexClass clazz : indexer.getClasses()) {
                indexer.fillClassData(clazz);
                if (clazz.getClassData() == null) continue;

                scanMethods(indexer, clazz.getClassData().directMethods, issues);
                scanMethods(indexer, clazz.getClassData().virtualMethods, issues);
            }
        }
    }

    private void scanMethods(DexFastIndexer indexer, List<DexEncodedMethod> methods, List<VulnerabilityIssue> issues) {
        for (DexEncodedMethod method : methods) {
            if (method.getCodeOff() == 0) continue;

            String currentMethodSig = indexer.getMethodPool().getMethodSignature(method.getMethodIndex());
            DexCode code = indexer.getCodeParser().parse(method.getCodeOff());
            List<DexInstruction> insns = DexInstructionDecoder.decode(code);

            boolean hasDecryption = false;

            // Check method summary for inherited semantic events
            if (ipDfa != null) {
                MethodSummary summary = ipDfa.getMethodSummaries().get(currentMethodSig);
                if (summary != null && summary.hasSemanticEvent("CRYPTO_DECRYPT")) {
                    hasDecryption = true;
                }
            }

            for (DexInstruction insn : insns) {
                int op = insn.getOpcode() & 0xFF;
                if (op >= 0x6E && op <= 0x72) { // INVOKE
                    int mIdx = insn.getIndex();
                    if (mIdx < 0 || mIdx >= indexer.getMethodPool().getSize()) continue;

                    String calleeSig = indexer.getMethodPool().getMethodSignature(mIdx);

                    // Direct or inherited decryption detection
                    if (calleeSig.contains("Cipher;->doFinal")) {
                        hasDecryption = true;
                    } else if (ipDfa != null) {
                        MethodSummary calleeSummary = ipDfa.getMethodSummaries().get(calleeSig);
                        if (calleeSummary != null && calleeSummary.hasSemanticEvent("CRYPTO_DECRYPT")) {
                            hasDecryption = true;
                        }
                    }

                    // Decision point detection
                    if (hasDecryption && (calleeSig.contains("String;->equals") || calleeSig.contains("Arrays;->equals"))) {
                        issues.add(new VulnerabilityIssue("Logic Security",
                            "CRITICAL: Decryption-to-Comparison chain detected across method boundaries. This is a likely verification point.",
                            VulnerabilityIssue.Severity.CRITICAL,
                            currentMethodSig));
                        break;
                    }
                }
            }
        }
    }
}
