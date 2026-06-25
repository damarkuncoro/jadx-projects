package dexforge.core.diagnostic.owasp.mas_crackmes.level4;

import static org.assertj.core.api.Assertions.assertThat;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.service.DexFastIndexer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

class UnCrackableLevel4Test {

    @Test
    void testSolveUnCrackableLevel4a() throws Exception {
        String apkPath = "/Users/damarkuncoro/antigravity/Experiments/JADX-Projects/Samples/mastg/Crackmes/Android/Level_04/r2pay-v0.9.apk";
        File apkFile = new File(apkPath);
        if (apkFile.exists()) {
            ApkLoader loader = new ApkLoader();
            loader.load(apkFile);
            for (DexFastIndexer indexer : loader.getIndexers()) {
                for (dexforge.core.parser.dex.model.DexClass clazz : indexer.getClasses()) {
                    if (clazz.getName().contains("re/pwnme")) {
                        System.out.println("=== CLASS: " + clazz.getName() + " ===");
                        indexer.fillClassData(clazz);
                        var data = clazz.getClassData();
                        if (data != null) {
                            for (var method : data.directMethods) {
                                dumpMethodDecoded(indexer, method);
                            }
                            for (var method : data.virtualMethods) {
                                dumpMethodDecoded(indexer, method);
                            }
                        }
                    }
                }
            }
        }
        assertThat(apkFile).exists();
    }

    private void dumpMethodDecoded(DexFastIndexer indexer, dexforge.core.parser.dex.model.DexEncodedMethod method) {
        String sig = indexer.getMethodPool().getMethodSignature(method.getMethodIndex());
        System.out.println("  Method: " + sig);
        if (method.getCodeOff() != 0) {
            dexforge.core.parser.dex.model.DexCode code = new dexforge.core.parser.dex.sections.DexCodeParser(indexer.getReader()).parse(method.getCodeOff());
            List<DexInstruction> insns = dexforge.core.parser.dex.sections.DexInstructionDecoder.decode(code);
            for (DexInstruction insn : insns) {
                int op = insn.getOpcode() & 0xFF;
                int idx = insn.getIndex();
                String decoded = "";
                if ((op == 0x1A || op == 0x1B) && idx != -1) { // const-string
                    decoded = " -> \"" + indexer.getStringPool().getString(idx) + "\"";
                } else if (op >= 0x6E && op <= 0x72 && idx != -1) { // invoke-*
                    decoded = " -> " + indexer.getMethodPool().getMethodSignature(idx);
                } else if (op >= 0x52 && op <= 0x5F && idx != -1) { // sget/sput/iget/iput
                    decoded = " -> " + indexer.getFieldPool().getDeclaringClass(idx) + "->" + indexer.getFieldPool().getFieldName(idx);
                }
                System.out.printf("    0x%04X: 0x%02X [idx: %d]%s\n", insn.getOffset(), op, idx, decoded);
            }
        }
    }
}
