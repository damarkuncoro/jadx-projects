package dexforge.core.diagnostic.owasp.mas_crackmes.level1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.analysis.emulator.SmaliEmulator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class UnCrackableLevel1Test {

    @Test
    void testSolveUnCrackableLevel1() throws Exception {
        String apkPath = "/Users/damarkuncoro/antigravity/Experiments/JADX-Projects/Samples/mastg/Crackmes/Android/Level_01/UnCrackable-Level1.apk";
        File apkFile = new File(apkPath);

        // Skip test if sample APK is not present
        assumeTrue(apkFile.exists(), "Sample UnCrackable-Level1.apk not found, skipping integration test.");

        ApkLoader loader = new ApkLoader();
        loader.load(apkFile);

        byte[] key = hexToBytes("8d127684cbc37c17616d806cf50473cc");
        byte[] encryptedSecret = java.util.Base64.getDecoder().decode("5UJiFctbmgbDoLXmpL12mkno8HT4Lv8dlat8FxR2GOc=");

        String decryptMethodSig = "Lsg/vantagepoint/a/a;->a([B[B)[B";
        List<DexInstruction> instructions = null;
        DexFastIndexer targetIndexer = null;
        for (DexFastIndexer indexer : loader.getIndexers()) {
            instructions = indexer.getMethodInstructions(decryptMethodSig);
            if (!instructions.isEmpty()) {
                targetIndexer = indexer;
                break;
            }
        }

        assertThat(instructions).isNotNull().isNotEmpty();

        SmaliEmulator emulator = new SmaliEmulator();
        emulator.setIndexer(targetIndexer);

        Map<Integer, Object> initialRegs = new HashMap<>();
        initialRegs.put(2, key);
        initialRegs.put(3, encryptedSecret);

        Object result = emulator.execute(instructions, initialRegs);
        assertThat(result).isInstanceOf(byte[].class);

        String password = new String((byte[]) result).trim();
        assertThat(password).isEqualTo("I want to believe");
    }

    private static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}
