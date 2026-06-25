package dexforge.core.parser.analysis.deobf.specific;

import dexforge.core.parser.analysis.emulator.SmaliEmulator;
import dexforge.core.parser.analysis.emulator.library.BcaEnvironmentalHandler;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.apk.ApkLoader;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced deobfuscator that uses Smali Emulator to resolve BCA strings.
 */
public final class BcaEmulatorDeobfuscator {
	private final SmaliEmulator emulator = new SmaliEmulator();
	private final String DECRYPTOR_CLASS = "Lo/zzmt;";
	private boolean initialized = false;

	public void init(List<DexFastIndexer> indexers, File apkFile) {
		if (indexers.isEmpty()) return;

		emulator.setAllIndexers(indexers);
		emulator.setIndexer(indexers.get(0));
		emulator.registerHandler(new BcaEnvironmentalHandler(apkFile));

		// Run <clinit> to initialize decryption keys and data blobs
		try {
			System.out.println("  Initializing BCA Emulator for " + DECRYPTOR_CLASS + "...");
			emulator.executeMethod(DECRYPTOR_CLASS + "-><clinit>()V", new HashMap<>());
			initialized = true;
			System.out.println("  BCA Emulator Initialized successfully.");
		} catch (Exception e) {
			System.err.println("Warning: Failed to initialize BCA Emulator: " + e.getMessage());
		}
	}

	public String resolve(int index) {
		if (!initialized) {
			return BcaStringDecryptor.resolve(index); // Fallback
		}

		try {
			Map<Integer, Object> regs = new HashMap<>();
			regs.put(0, index);
			Object result = emulator.executeMethod(DECRYPTOR_CLASS + "->b(I)Ljava/lang/Object;", regs);
			if (result instanceof String) {
				return (String) result;
			}
			return result != null ? result.toString() : null;
		} catch (Exception e) {
			return "EMU_ERR_" + index;
		}
	}

	public boolean isInitialized() {
		return initialized;
	}
}
