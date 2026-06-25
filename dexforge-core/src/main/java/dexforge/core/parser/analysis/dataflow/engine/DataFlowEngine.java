package dexforge.core.parser.analysis.dataflow.engine;

import dexforge.core.parser.analysis.cfg.ControlFlowGraph;
import dexforge.core.parser.analysis.cfg.model.CfgBlock;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * SOLID & REUSEABLE: Generic Data Flow Analysis Engine.
 * Implements the Worklist algorithm for any Control Flow Graph and DataFlowFact.
 */
public final class DataFlowEngine<F extends DataFlowFact<F>> {
    private final ControlFlowGraph cfg;
    private final Map<CfgBlock, F> blockEntryFacts = new HashMap<>();

    public DataFlowEngine(ControlFlowGraph cfg) {
        this.cfg = cfg;
    }

    /**
     * Executes the analysis.
     * @param initialFact Supplier for the starting state.
     * @param transferFunction Function to apply a block's instructions to a fact.
     */
    public void run(Supplier<F> initialFact, BiConsumer<CfgBlock, F> transferFunction) {
        CfgBlock entry = cfg.getEntryBlock();
        if (entry == null) return;

        Queue<CfgBlock> worklist = new LinkedList<>();
        worklist.add(entry);
        blockEntryFacts.put(entry, initialFact.get());

        while (!worklist.isEmpty()) {
            CfgBlock block = worklist.poll();
            F fact = blockEntryFacts.get(block).copy();

            // Apply Transfer Function (Single block analysis)
            transferFunction.accept(block, fact);

            // Propagate and Merge into successors
            for (CfgBlock successor : block.getSuccessors()) {
                F existing = blockEntryFacts.get(successor);
                if (existing == null) {
                    blockEntryFacts.put(successor, fact.copy());
                    worklist.add(successor);
                } else if (existing.merge(fact)) {
                    worklist.add(successor);
                }
            }
        }
    }

    public F getEntryFact(CfgBlock block) {
        return blockEntryFacts.get(block);
    }
}
