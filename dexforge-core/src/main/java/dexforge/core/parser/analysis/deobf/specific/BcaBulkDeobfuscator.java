package dexforge.core.parser.analysis.deobf.specific;

import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.analysis.dataflow.SimpleDataFlowAnalyzer;
import dexforge.core.parser.analysis.deobf.DeobfuscatorModule;
import dexforge.core.parser.analysis.deobf.specific.model.DecryptedString;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * BCA specific deobfuscation module using Smali Emulation.
 */
public final class BcaBulkDeobfuscator implements DeobfuscatorModule {
	private final DexFastIndexer indexer;
	private final List<DecryptedString> findings = new ArrayList<>();
	private final BcaEmulatorDeobfuscator emulatorDeobf = new BcaEmulatorDeobfuscator();
	private boolean emulatorReady = false;

	public BcaBulkDeobfuscator(List<DexFastIndexer> indexers, File apkFile) {
		this.indexer = indexers.isEmpty() ? null : indexers.get(0); // Primary for some lookups
		if (apkFile != null && apkFile.exists()) {
			emulatorDeobf.init(indexers, apkFile);
			emulatorReady = emulatorDeobf.isInitialized();
		}
	}

	@Override
	public void processInstruction(DexClass clazz, DexEncodedMethod method, DexInstruction insn, SimpleDataFlowAnalyzer analyzer) {
		int op = insn.getOpcode() & 0xFF;
		if (op >= 0x6E && op <= 0x72) { // invoke
			handleInvoke(clazz, method, insn, analyzer);
		}
	}

	private void handleInvoke(DexClass clazz, DexEncodedMethod method, DexInstruction insn, SimpleDataFlowAnalyzer analyzer) {
		int methIdx = insn.getIndex();
		if (methIdx < 0 || methIdx >= indexer.getMethodPool().getSize()) {
			return;
		}

		String sig = indexer.getMethodPool().getMethodSignature(methIdx);
		if (!sig.contains("Lo/zzmt;->b(I)")) {
			return;
		}

		int[] regs = insn.getRegisters();
		if (regs == null || regs.length == 0) {
			return;
		}

		Long constVal = analyzer.getConstant(regs[0]);
		if (constVal != null) {
			registerFinding(clazz, method, insn, constVal);
		}
	}

	private void registerFinding(DexClass clazz, DexEncodedMethod method, DexInstruction insn, long constVal) {
		int val = (int) constVal;
		String decrypted;
		if (emulatorReady) {
			decrypted = emulatorDeobf.resolve(val);
		} else {
			decrypted = BcaStringDecryptor.resolve(val);
		}

		String methName = indexer.getMethodPool().getMethodName(method.getMethodIndex());
		String methSig = indexer.getMethodPool().getMethodSignature(method.getMethodIndex());
		String location = clazz.getName() + "->" + methName;

		findings.add(new DecryptedString(val, decrypted, location, insn.getOffset(), methSig));
	}

	public List<DecryptedString> getFindings() {
		return findings;
	}
}
