package dexforge.core.parser.analysis.deobf;

import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexCode;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.dex.sections.DexInstructionDecoder;
import dexforge.core.parser.analysis.dataflow.SimpleDataFlowAnalyzer;
import java.util.ArrayList;
import java.util.List;

/**
 * Scalable engine that orchestrates multiple deobfuscation modules.
 */
public final class DeobfuscationEngine {
	private final DexFastIndexer indexer;
	private final List<DeobfuscatorModule> modules = new ArrayList<>();
	private final SimpleDataFlowAnalyzer analyzer = new SimpleDataFlowAnalyzer();

	public DeobfuscationEngine(DexFastIndexer indexer) {
		this.indexer = indexer;
	}

	public void registerModule(DeobfuscatorModule module) {
		modules.add(module);
	}

	public void run() {
		// Process classes in parallel if needed, but for now linear is safer for debugging
		for (DexClass clazz : indexer.getClasses()) {
			indexer.fillClassData(clazz);
			if (clazz.getClassData() == null) {
				continue;
			}
			processMethods(clazz, clazz.getClassData().directMethods);
			processMethods(clazz, clazz.getClassData().virtualMethods);
		}
	}

	private void processMethods(DexClass clazz, List<DexEncodedMethod> methods) {
		for (DexEncodedMethod method : methods) {
			if (method.getCodeOff() == 0) {
				continue;
			}
			DexCode code = indexer.getCodeParser().parse(method.getCodeOff());
			List<DexInstruction> insns = DexInstructionDecoder.decode(code);

			analyzer.clear();
			for (DexInstruction insn : insns) {
				// Update data flow state
				analyzer.analyze(insn);

				// Notify all registered modules
				for (DeobfuscatorModule module : modules) {
					module.processInstruction(clazz, method, insn, analyzer);
				}
			}
		}
	}
}
