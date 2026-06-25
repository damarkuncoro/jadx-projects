package dexforge.core.parser.analysis.patterns;

import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.sections.DexInstructionDecoder;
import dexforge.core.parser.dex.service.DexFastIndexer;
import java.util.ArrayList;
import java.util.List;

public final class SecurityTrapDetector {
    private final DexFastIndexer indexer;

    public static final class TrapWarning {
        private final String methodSignature;
        private final int offset;
        private final String description;
        private final String suggestion;

        public TrapWarning(String methodSignature, int offset, String description, String suggestion) {
            this.methodSignature = methodSignature;
            this.offset = offset;
            this.description = description;
            this.suggestion = suggestion;
        }

        public String getMethodSignature() { return methodSignature; }
        public int getOffset() { return offset; }
        public String getDescription() { return description; }
        public String getSuggestion() { return suggestion; }
    }

    public SecurityTrapDetector(DexFastIndexer indexer) {
        this.indexer = indexer;
    }

    public List<TrapWarning> scanForTraps() {
        List<TrapWarning> results = new ArrayList<>();

        for (DexClass clazz : indexer.getClasses()) {
            indexer.fillClassData(clazz);
            if (clazz.getClassData() == null) continue;

            scanMethodList(clazz.getClassData().directMethods, results);
            scanMethodList(clazz.getClassData().virtualMethods, results);
        }

        return results;
    }

    private void scanMethodList(List<DexEncodedMethod> methods, List<TrapWarning> results) {
        for (DexEncodedMethod m : methods) {
            if (m.getCodeOff() == 0) continue;

            var code = indexer.getCodeParser().parse(m.getCodeOff());
            if (code == null) continue;

            List<DexInstruction> insns = DexInstructionDecoder.decode(code);
            String signature = indexer.getMethodPool().getMethodSignature(m.getMethodIndex());

            for (int i = 0; i < insns.size(); i++) {
                DexInstruction insn = insns.get(i);
                int op = insn.getOpcode() & 0xFF;

                // 1. Literal 8 divide-by-zero (0xDB/0xDC)
                if (op == 0xDB || op == 0xDC) {
                    if (insn.getLiteral() == 0) {
                        results.add(new TrapWarning(
                            signature,
                            insn.getOffset(),
                            "Intentional divide-by-zero via literal-8 trap detected.",
                            "Suggested action: inspect parent branch of division at offset " + insn.getOffset() + " and bypass it."
                        ));
                    }
                }

                // 2. Register divide-by-zero (0x93/0x94 or 0xB3/0xB4)
                if (op == 0x93 || op == 0x94 || op == 0xB3 || op == 0xB4) {
                    int divisorReg = -1;
                    if (op == 0x93 || op == 0x94) {
                        int[] regs = insn.getRegisters();
                        if (regs != null && regs.length >= 3) {
                            divisorReg = regs[2];
                        }
                    } else { // 0xB3 or 0xB4
                        int[] regs = insn.getRegisters();
                        if (regs != null && regs.length >= 2) {
                            divisorReg = regs[1];
                        }
                    }

                    if (divisorReg != -1 && isRegisterZero(insns, i, divisorReg)) {
                        results.add(new TrapWarning(
                            signature,
                            insn.getOffset(),
                            "Intentional divide-by-zero trap via zero-register detected.",
                            "Suggested action: inspect parent branch of division at offset " + insn.getOffset() + " and bypass it."
                        ));
                    }
                }
            }
        }
    }

    private boolean isRegisterZero(List<DexInstruction> insns, int currentIndex, int reg) {
        // Trace backward to find the last write to 'reg'
        for (int i = currentIndex - 1; i >= 0; i--) {
            DexInstruction insn = insns.get(i);
            int[] regs = insn.getRegisters();
            if (regs != null && regs.length > 0 && regs[0] == reg) {
                int op = insn.getOpcode() & 0xFF;
                // If it's a const load, check if value is 0
                if (op == 0x12 || op == 0x13 || op == 0x14 || op == 0x15) {
                    return insn.getLiteral() == 0;
                }
                // Any other write to this register is treated as non-zero for safety
                return false;
            }
        }
        return false;
    }
}
