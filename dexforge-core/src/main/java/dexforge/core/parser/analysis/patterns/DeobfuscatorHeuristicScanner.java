package dexforge.core.parser.analysis.patterns;

import dexforge.core.parser.analysis.patterns.model.MethodFingerprint;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.sections.DexInstructionDecoder;
import dexforge.core.parser.dex.sections.DexOpcode;
import dexforge.core.parser.dex.service.DexFastIndexer;
import java.util.ArrayList;
import java.util.List;

/**
 * SOLID: Heuristic scanner to automatically identify string/constant deobfuscator methods.
 * Searches for methods with high density of bitwise and arithmetic instructions.
 */
public final class DeobfuscatorHeuristicScanner {
    private final DexFastIndexer indexer;

    public DeobfuscatorHeuristicScanner(DexFastIndexer indexer) {
        this.indexer = indexer;
    }

    /**
     * Scans the entire DEX for suspicious deobfuscation helper methods.
     */
    public List<String> findDeobfuscatorMethods() {
        List<String> suspiciousMethods = new ArrayList<>();

        for (DexClass clazz : indexer.getClasses()) {
            indexer.fillClassData(clazz);
            if (clazz.getClassData() == null) continue;

            scanMethodList(clazz.getClassData().directMethods, suspiciousMethods);
            scanMethodList(clazz.getClassData().virtualMethods, suspiciousMethods);
        }

        return suspiciousMethods;
    }

    private void scanMethodList(List<DexEncodedMethod> methods, List<String> results) {
        for (DexEncodedMethod m : methods) {
            if (m.getCodeOff() == 0) continue;

            MethodFingerprint fingerprint = analyzeMethod(m);

            // Heuristic Rule:
            // 1. High Bitwise/Arithmetic density (> 40%)
            // 2. Few external calls (typically a self-contained math loop)
            // 3. Small-to-medium size
            if (fingerprint.getBitwiseDensity() > 0.4 && fingerprint.getInvokeCount() <= 2) {
                results.add(indexer.getMethodPool().getMethodSignature(m.getMethodIndex()));
            }
        }
    }

    private MethodFingerprint analyzeMethod(DexEncodedMethod m) {
        MethodFingerprint fingerprint = new MethodFingerprint();
        var code = indexer.getCodeParser().parse(m.getCodeOff());
        List<DexInstruction> insns = DexInstructionDecoder.decode(code);

        for (DexInstruction insn : insns) {
            int op = insn.getOpcode() & 0xFF;
            if (DexOpcode.isBitwise(op)) {
                fingerprint.addBitwise();
            } else if (DexOpcode.isArithmetic(op)) {
                fingerprint.addArithmetic();
            } else if (DexOpcode.isInvoke(op)) {
                fingerprint.addInvoke();
            } else if (op >= 0x44 && op <= 0x51) { // AGET/APUT
                fingerprint.addArrayOp();
            } else {
                fingerprint.addOther();
            }
        }
        return fingerprint;
    }
}
