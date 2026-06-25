package dexforge.core.parser.analysis.dataflow;

import dexforge.core.parser.analysis.cfg.ControlFlowGraph;
import dexforge.core.parser.analysis.cfg.model.CfgBlock;
import dexforge.core.parser.analysis.dataflow.engine.DalvikRegisterFact;
import dexforge.core.parser.dex.model.DexInstruction;
import java.util.ArrayList;
import java.util.List;

/**
 * Specialized analyzer to detect flows of sensitive data to dangerous sinks.
 */
public final class TaintAnalyzer {
    private final ControlFlowGraph cfg;
    private final DataFlowAnalyzer dfa;
    private final List<TaintFlow> findings = new ArrayList<>();

    public TaintAnalyzer(ControlFlowGraph cfg) {
        this.cfg = cfg;
        this.dfa = new DataFlowAnalyzer(cfg);
    }

    public static final class TaintFlow {
        public final String sink;
        public final int address;

        public TaintFlow(String sink, int address) {
            this.sink = sink;
            this.address = address;
        }
    }

    public List<TaintFlow> analyze() {
        dfa.analyze();

        for (CfgBlock block : cfg.getBlocks()) {
            DalvikRegisterFact state = dfa.getBlockEntryState(block);
            if (state == null) continue;

            for (DexInstruction insn : block.getInstructions()) {
                // Check if any register used as argument to a Sink is tainted
                if (isSink(insn)) {
                    int argReg = 0; // Simplified argument extraction
                    if (state.isTainted(argReg)) {
                        findings.add(new TaintFlow(insn.toString(), insn.getOffset()));
                    }
                }
            }
        }
        return findings;
    }

    private boolean isSink(DexInstruction insn) {
        // Future: Load sinks from Security Rules
        return insn.toString().contains("Log;->d") || insn.toString().contains("Http");
    }
}
