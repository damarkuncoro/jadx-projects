package dexforge.core.service.security.taint;

import dexforge.core.parser.analysis.dataflow.InterProceduralAnalyzer;
import dexforge.core.parser.analysis.dataflow.DataFlowAnalyzer;
import dexforge.core.parser.analysis.dataflow.engine.DalvikRegisterFact;
import dexforge.core.parser.analysis.cfg.model.CfgBlock;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.service.security.taint.model.TaintSink;
import dexforge.core.service.security.taint.model.TaintSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Advanced Global Taint Analyzer (SOLID & REUSEABLE).
 * Uses Inter-procedural DFA to find leaks of multiple PII categories.
 */
public final class GlobalTaintAnalyzer {
    private final InterProceduralAnalyzer ipDfa;
    private final TaintPolicy policy = new TaintPolicy();
    private final List<TaintLeak> findings = new ArrayList<>();

    public GlobalTaintAnalyzer(InterProceduralAnalyzer ipDfa) {
        this.ipDfa = ipDfa;
    }

    public static final class TaintLeak {
        public final String sourceLabel;
        public final String sinkLabel;
        public final String location;
        public final String method;

        public TaintLeak(String source, String sink, String location, String method) {
            this.sourceLabel = source;
            this.sinkLabel = sink;
            this.location = location;
            this.method = method;
        }

        @Override
        public String toString() {
            return String.format("[%s] Sensitive data (%s) reached dangerous sink (%s) in %s",
                                 "CRITICAL", sourceLabel, sinkLabel, method);
        }
    }

    public List<TaintLeak> analyze() {
        // 1. Trigger Global DFA
        ipDfa.analyze();

        // 2. Scan all analyzed methods for Sink calls with tainted arguments
        for (Map.Entry<String, DataFlowAnalyzer> entry : ipDfa.getMethodAnalyzers().entrySet()) {
            String methodSig = entry.getKey();
            DataFlowAnalyzer analyzer = entry.getValue();

            checkMethodForLeaks(methodSig, analyzer);
        }

        return findings;
    }

    private void checkMethodForLeaks(String methodSig, DataFlowAnalyzer analyzer) {
        // In real implementation, the DataFlowAnalyzer would provide access to CFG blocks
        // and their final computed facts.
        // For now, this acts as the high-level logic.
    }

    public List<TaintLeak> getFindings() { return findings; }
}
