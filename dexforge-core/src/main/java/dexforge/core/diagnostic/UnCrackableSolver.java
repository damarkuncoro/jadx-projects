package dexforge.core.diagnostic;

import dexforge.core.parser.apk.ApkLoader;
import dexforge.core.parser.dex.model.*;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.analysis.emulator.SmaliEmulator;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Specialized diagnostic to verify SmaliEmulator + CryptoHandler on UnCrackable Level 1.
 */
public class UnCrackableSolver {
	public static void main(String[] args) throws Exception {
		String apkPath = "/Users/damarkuncoro/antigravity/Experiments/JADX-Projects/Samples/mastg/Crackmes/Android/Level_01/UnCrackable-Level1.apk";
		System.out.println("--- DexForge Crackme Solver (Level 1) ---");

		ApkLoader loader = new ApkLoader();
		loader.load(new File(apkPath));

		// 1. Real cryptographic parameters extracted from sg.vantagepoint.uncrackable1.a
		byte[] key = hexToBytes("8d127684cbc37c17616d806cf50473cc");
		byte[] encryptedSecret = java.util.Base64.getDecoder().decode("5UJiFctbmgbDoLXmpL12mkno8HT4Lv8dlat8FxR2GOc=");

		// 2. Find the decryption method instructions
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

		if (instructions == null || instructions.isEmpty()) {
			System.err.println("Error: Could not find decryption method: " + decryptMethodSig);
			return;
		}

		// 3. Emulate Decryption
		System.out.println("Emulating: " + decryptMethodSig);
		SmaliEmulator emulator = new SmaliEmulator();
		emulator.setIndexer(targetIndexer);

		Map<Integer, Object> initialRegs = new HashMap<>();
		// In a static method with 4 registers (.registers 4) and 2 parameters,
		// Dalvik maps parameters to the last registers: v2 (key) and v3 (ciphertext)
		initialRegs.put(2, key);
		initialRegs.put(3, encryptedSecret);

		try {
			Object result = emulator.execute(instructions, initialRegs);
			if (result instanceof byte[]) {
				String password = new String((byte[]) result).trim();
				System.out.println("\n[SUCCESS] Decrypted Password Found: " + password);
			} else {
				System.out.println("Emulation finished but result is not a byte array.");
			}
		} catch (Exception e) {
			System.err.println("Emulation failed: " + e.getMessage());
		}
	}

	private static byte[] hexToBytes(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
		}
		return bytes;
	}
}
