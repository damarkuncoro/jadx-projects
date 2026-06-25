package dexforge.core.diagnostic.owasp.mas_crackmes.level3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.analysis.emulator.SmaliEmulator;
import dexforge.core.parser.analysis.emulator.library.VirtualMethodHandler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class UnCrackableLevel3Test {

    @Test
    void testSolveUnCrackableLevel3() throws Exception {
        String apkPath = "/Users/damarkuncoro/antigravity/Experiments/JADX-Projects/Samples/mastg/Crackmes/Android/Level_03/UnCrackable-Level3.apk";
        File apkFile = new File(apkPath);

        assumeTrue(apkFile.exists(), "Sample UnCrackable-Level3.apk not found, skipping integration test.");

        ApkLoader loader = new ApkLoader();
        loader.load(apkFile);

        String targetMethodSig = "Lsg/vantagepoint/uncrackable3/CodeCheck;->check_code(Ljava/lang/String;)Z";
        List<DexInstruction> instructions = null;
        DexFastIndexer targetIndexer = null;
        for (DexFastIndexer indexer : loader.getIndexers()) {
            instructions = indexer.getMethodInstructions(targetMethodSig);
            if (!instructions.isEmpty()) {
                targetIndexer = indexer;
                break;
            }
        }

        assertThat(instructions).isNotNull().isNotEmpty();

        // 1. Test with the correct password
        {
            SmaliEmulator emulator = new SmaliEmulator();
            emulator.setIndexer(targetIndexer);

            // Register dynamic JNI override for bar([B)Z in Level 3
            emulator.registerHandler(new VirtualMethodHandler() {
                @Override
                public boolean canHandle(String sig) {
                    return sig.contains("CodeCheck;->bar");
                }
                @Override
                public Object execute(String sig, List<Object> args) {
                    if (args.size() >= 2 && args.get(1) instanceof byte[]) {
                        byte[] input = (byte[]) args.get(1);
                        byte[] secret = "making owasp great again".getBytes(StandardCharsets.UTF_8);
                        return Arrays.equals(input, secret);
                    }
                    return false;
                }
            });

            Map<Integer, Object> initialRegs = new HashMap<>();
            initialRegs.put(0, new Object()); // this (CodeCheck instance)
            initialRegs.put(1, "making owasp great again"); // input String

            Object result = emulator.execute(instructions, initialRegs);
            assertThat(result).isEqualTo(true);
        }

        // 2. Test with a wrong password
        {
            SmaliEmulator emulator = new SmaliEmulator();
            emulator.setIndexer(targetIndexer);

            // Register dynamic JNI override for bar([B)Z in Level 3
            emulator.registerHandler(new VirtualMethodHandler() {
                @Override
                public boolean canHandle(String sig) {
                    return sig.contains("CodeCheck;->bar");
                }
                @Override
                public Object execute(String sig, List<Object> args) {
                    if (args.size() >= 2 && args.get(1) instanceof byte[]) {
                        byte[] input = (byte[]) args.get(1);
                        byte[] secret = "making owasp great again".getBytes(StandardCharsets.UTF_8);
                        return Arrays.equals(input, secret);
                    }
                    return false;
                }
            });

            Map<Integer, Object> initialRegs = new HashMap<>();
            initialRegs.put(0, new Object()); // this (CodeCheck instance)
            initialRegs.put(1, "wrong password"); // input String

            Object result = emulator.execute(instructions, initialRegs);
            assertThat(result).isEqualTo(false);
        }
    }
}
