package dexforge.core.parser.analysis.dataflow;

import dexforge.core.parser.analysis.cfg.ControlFlowGraph;
import dexforge.core.parser.analysis.cfg.model.CfgBlock;
import dexforge.core.parser.analysis.dataflow.engine.DalvikRegisterFact;
import dexforge.core.parser.analysis.dataflow.engine.DataFlowEngine;
import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.service.security.taint.TaintPolicy;
import java.util.HashSet;

/**
 * Enhanced Data Flow Analyzer powered by a generic DFA Engine.
 * Provides access to its CFG for semantic analysis.
 */
public final class DataFlowAnalyzer {
    private final ControlFlowGraph cfg;
    private final DataFlowEngine<DalvikRegisterFact> engine;
    private final TaintPolicy taintPolicy = new TaintPolicy();

    public DataFlowAnalyzer(ControlFlowGraph cfg) {
        this.cfg = cfg;
        this.engine = new DataFlowEngine<>(cfg);
    }

    public void analyze() {
        engine.run(DalvikRegisterFact::new, this::transferBlock);
    }

    public ControlFlowGraph getCfg() {
        return cfg;
    }

    private void transferBlock(CfgBlock block, DalvikRegisterFact fact) {
        for (DexInstruction insn : block.getInstructions()) {
            updateInsnState(insn, fact);
        }
    }

    private void updateInsnState(DexInstruction insn, DalvikRegisterFact fact) {
        int op = insn.getOpcode() & 0xFF;
        if (op >= 0x12 && op <= 0x15) {
            fact.set(extractRegA(insn), insn.getIndex(), "int", new HashSet<>());
        } else if (op == 0x1A || op == 0x1B) {
            fact.set(extractRegA(insn), "STRING", "Ljava/lang/String;", new HashSet<>());
        }
    }

    private int extractRegA(DexInstruction insn) {
        return (insn.getOpcode() >> 8) & 0x0F;
    }

    public DalvikRegisterFact getBlockEntryState(CfgBlock block) {
        return engine.getEntryFact(block);
    }
}
