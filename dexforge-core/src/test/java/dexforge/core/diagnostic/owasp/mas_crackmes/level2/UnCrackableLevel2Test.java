package dexforge.core.diagnostic.owasp.mas_crackmes.level2;

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

class UnCrackableLevel2Test {

    @Test
    void testSolveUnCrackableLevel2() throws Exception {
        String apkPath = "/Users/damarkuncoro/antigravity/Experiments/JADX-Projects/Samples/mastg/Crackmes/Android/Level_02/UnCrackable-Level2.apk";
        File apkFile = new File(apkPath);

        assumeTrue(apkFile.exists(), "Sample UnCrackable-Level2.apk not found, skipping integration test.");

        ApkLoader loader = new ApkLoader();
        loader.load(apkFile);

        String targetMethodSig = "Lsg/vantagepoint/uncrackable2/CodeCheck;->a(Ljava/lang/String;)Z";
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

        System.out.println("--- DUMPING INSTRUCTIONS FOR Lsg/vantagepoint/uncrackable2/CodeCheck;->a(Ljava/lang/String;)Z ---");
        for (DexInstruction insn : instructions) {
            System.out.printf("Offset: %d, Opcode: 0x%02X, Length: %d, Index: %d%n",
                insn.getOffset(),
                insn.getOpcode() & 0xFF,
                insn.getLength(),
                insn.getIndex());
            if (insn.getRegisters() != null) {
                System.out.print("  Registers: ");
                for (int r : insn.getRegisters()) System.out.print("v" + r + " ");
                System.out.println();
            }
        }

        // 1. Test with the correct password
        {
            SmaliEmulator emulator = new SmaliEmulator();
            emulator.setIndexer(targetIndexer);

            // Register dynamic JNI override for bar([B)Z
            emulator.registerHandler(new VirtualMethodHandler() {
                @Override
                public boolean canHandle(String sig) {
                    return sig.contains("CodeCheck;->bar");
                }
                @Override
                public Object execute(String sig, List<Object> args) {
                    if (args.size() >= 2 && args.get(1) instanceof byte[]) {
                        byte[] input = (byte[]) args.get(1);
                        byte[] secret = "Thanks for all the fish".getBytes(StandardCharsets.UTF_8);
                        return Arrays.equals(input, secret);
                    }
                    return false;
                }
            });

            Map<Integer, Object> initialRegs = new HashMap<>();
            initialRegs.put(0, new Object()); // this (CodeCheck instance)
            initialRegs.put(1, "Thanks for all the fish"); // input String

            Object result = emulator.execute(instructions, initialRegs);
            assertThat(result).isEqualTo(true);
        }

        // 2. Test with a wrong password
        {
            SmaliEmulator emulator = new SmaliEmulator();
            emulator.setIndexer(targetIndexer);

            // Register dynamic JNI override for bar([B)Z
            emulator.registerHandler(new VirtualMethodHandler() {
                @Override
                public boolean canHandle(String sig) {
                    return sig.contains("CodeCheck;->bar");
                }
                @Override
                public Object execute(String sig, List<Object> args) {
                    if (args.size() >= 2 && args.get(1) instanceof byte[]) {
                        byte[] input = (byte[]) args.get(1);
                        byte[] secret = "Thanks for all the fish".getBytes(StandardCharsets.UTF_8);
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
