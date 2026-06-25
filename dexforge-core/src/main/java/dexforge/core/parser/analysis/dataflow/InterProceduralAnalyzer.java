package dexforge.core.parser.analysis.dataflow;

import dexforge.core.parser.analysis.callgraph.model.CallGraphNode;
import dexforge.core.parser.analysis.cfg.ControlFlowGraph;
import dexforge.core.parser.analysis.cfg.CfgParser;
import dexforge.core.parser.analysis.dataflow.engine.DalvikRegisterFact;
import dexforge.core.parser.analysis.dataflow.model.MethodSummary;
import dexforge.core.parser.dex.model.DexClass;
import dexforge.core.parser.dex.model.DexEncodedMethod;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.dex.service.DexFastIndexer;
import dexforge.core.parser.dex.sections.DexInstructionDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;

/**
 * Enhanced Inter-procedural Data Flow Analyzer (IP-DFA).
 * Now supports Semantic Tracking across method boundaries.
 */
public final class InterProceduralAnalyzer {
    private final DexFastIndexer indexer;
    private final Map<String, CallGraphNode> callGraph;
    private final Map<String, DataFlowAnalyzer> methodAnalyzers = new HashMap<>();
    private final Map<String, MethodSummary> methodSummaries = new HashMap<>();

    public InterProceduralAnalyzer(DexFastIndexer indexer, Map<String, CallGraphNode> callGraph) {
        this.indexer = indexer;
        this.callGraph = callGraph;
    }

    public void analyze() {
        Queue<CallGraphNode> worklist = new LinkedList<>(callGraph.values());

        while (!worklist.isEmpty()) {
            CallGraphNode node = worklist.poll();
            String sig = node.getSignature();

            DataFlowAnalyzer analyzer = getOrCreateAnalyzer(sig);
            if (analyzer == null) continue;

            analyzer.analyze();

            // 1. Generate Semantic Method Summary
            MethodSummary newSummary = createSummary(sig, analyzer);

            // 2. Propagation Logic
            MethodSummary oldSummary = methodSummaries.get(sig);
            if (oldSummary == null || !summariesEqual(oldSummary, newSummary)) {
                methodSummaries.put(sig, newSummary);
                // Trigger re-analysis of callers to propagate semantic events
                for (CallGraphNode caller : node.getCallers()) {
                    worklist.add(caller);
                }
            }
        }
    }

    private MethodSummary createSummary(String signature, DataFlowAnalyzer analyzer) {
        MethodSummary summary = new MethodSummary(signature, new DalvikRegisterFact());

        ControlFlowGraph cfg = analyzer.getCfg();
        for (var block : cfg.getBlocks()) {
            for (DexInstruction insn : block.getInstructions()) {
                if ((insn.getOpcode() & 0xFF) >= 0x6E && insn.getIndex() != -1) { // Any valid invoke
                    int mIdx = insn.getIndex();
                    if (mIdx < 0 || mIdx >= indexer.getMethodPool().getSize()) continue;

                    String callee = indexer.getMethodPool().getMethodSignature(mIdx);

                    MethodSummary calleeSummary = methodSummaries.get(callee);
                    if (calleeSummary != null) {
                        summary.getSemanticEvents().addAll(calleeSummary.getSemanticEvents());
                    }

                    if (callee.contains("Cipher;->doFinal")) {
                        summary.addSemanticEvent("CRYPTO_DECRYPT");
                    }
                }
            }
        }
        return summary;
    }

    private boolean summariesEqual(MethodSummary s1, MethodSummary s2) {
        return s1.getSemanticEvents().equals(s2.getSemanticEvents());
    }

    private DataFlowAnalyzer getOrCreateAnalyzer(String signature) {
        if (methodAnalyzers.containsKey(signature)) return methodAnalyzers.get(signature);

        for (DexClass clazz : indexer.getClasses()) {
            indexer.fillClassData(clazz);
            if (clazz.getClassData() == null) continue;

            DexEncodedMethod method = findMethodBySignature(clazz, signature);
            if (method != null && method.getCodeOff() != 0) {
                var code = indexer.getCodeParser().parse(method.getCodeOff());
                var insns = DexInstructionDecoder.decode(code);
                ControlFlowGraph cfg = new CfgParser().parse(insns);

                DataFlowAnalyzer analyzer = new DataFlowAnalyzer(cfg);
                methodAnalyzers.put(signature, analyzer);
                return analyzer;
            }
        }
        return null;
    }

    private DexEncodedMethod findMethodBySignature(DexClass clazz, String sig) {
        for (var m : clazz.getClassData().directMethods) {
            if (indexer.getMethodPool().getMethodSignature(m.getMethodIndex()).equals(sig)) return m;
        }
        for (var m : clazz.getClassData().virtualMethods) {
            if (indexer.getMethodPool().getMethodSignature(m.getMethodIndex()).equals(sig)) return m;
        }
        return null;
    }

    public Map<String, DataFlowAnalyzer> getMethodAnalyzers() { return methodAnalyzers; }
    public Map<String, MethodSummary> getMethodSummaries() { return methodSummaries; }
}
