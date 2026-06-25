package dexforge.core.parser.analysis.deobf;

import dexforge.core.parser.analysis.deobf.specific.BcaBulkDeobfuscator;
import dexforge.core.parser.analysis.deobf.specific.model.DecryptedString;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.smali.service.SmaliWriter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * High-level service that connects Deobfuscation findings with SmaliWriter patching.
 */
public final class DeobfuscationPatcher {
	private final DexFastIndexer indexer;
	private final DeobfuscationEngine engine;
	private final BcaBulkDeobfuscator bcaModule;
	private final SmaliWriter smaliWriter;

	public DeobfuscationPatcher(DexFastIndexer indexer, List<DexFastIndexer> allIndexers, File apkFile) {
		this.indexer = indexer;
		this.engine = new DeobfuscationEngine(indexer);
		this.bcaModule = new BcaBulkDeobfuscator(allIndexers, apkFile);
		this.smaliWriter = new SmaliWriter(indexer);

		this.engine.registerModule(bcaModule);
	}

	public Map<String, String> patchAll() {
		// 1. Run deobfuscation
		engine.run();

		// 2. Prepare patches
		for (DecryptedString finding : bcaModule.getFindings()) {
			String patchLocation = finding.getLocation().split("->")[0] + "->" + finding.getMethodSig() + "@" + finding.getOffset();
			String patchInsn = "const-string v0, \"" + finding.getValue() + "\"";
			smaliWriter.addPatch(patchLocation, patchInsn);
		}

		// 3. Generate Smali for classes that have findings
		Map<String, String> patchedSmali = new HashMap<>();
		for (DecryptedString finding : bcaModule.getFindings()) {
			String className = finding.getLocation().split("->")[0];
			if (!patchedSmali.containsKey(className)) {
				DexClass clazz = findClass(className);
				if (clazz != null) {
					patchedSmali.put(className, smaliWriter.writeClass(clazz));
				}
			}
		}

		return patchedSmali;
	}

	private DexClass findClass(String name) {
		for (DexClass clazz : indexer.getClasses()) {
			if (clazz.getName().equals(name)) {
				return clazz;
			}
		}
		return null;
	}
}
