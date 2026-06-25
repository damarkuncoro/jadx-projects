package dexforge.core.parser.dex.decompiler.analysis;

import dexforge.core.parser.analysis.cfg.ControlFlowGraph;
import dexforge.core.parser.analysis.cfg.model.CfgBlock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builds a Dominator Tree for the CFG to help identify high-level control structures accurately.
 */
public final class DominatorTree {
    private final ControlFlowGraph cfg;
    private final Map<CfgBlock, Set<CfgBlock>> dominators = new HashMap<>();

    public DominatorTree(ControlFlowGraph cfg) {
        this.cfg = cfg;
    }

    public void compute() {
        if (cfg.getEntryBlock() == null) return;

        Set<CfgBlock> allBlocks = new HashSet<>(cfg.getBlocks());
        for (CfgBlock block : allBlocks) {
            dominators.put(block, new HashSet<>(allBlocks));
        }

        // The entry block is dominated only by itself
        Set<CfgBlock> entryDom = new HashSet<>();
        entryDom.add(cfg.getEntryBlock());
        dominators.put(cfg.getEntryBlock(), entryDom);

        boolean changed = true;
        while (changed) {
            changed = false;
            for (CfgBlock block : cfg.getBlocks()) {
                if (block == cfg.getEntryBlock()) continue;

                Set<CfgBlock> newDoms = new HashSet<>(allBlocks);
                for (CfgBlock pred : block.getPredecessors()) {
                    newDoms.retainAll(dominators.get(pred));
                }
                newDoms.add(block);

                if (!newDoms.equals(dominators.get(block))) {
                    dominators.put(block, newDoms);
                    changed = true;
                }
            }
        }
    }

    public boolean dominates(CfgBlock a, CfgBlock b) {
        return dominators.get(b) != null && dominators.get(b).contains(a);
    }
}
