package dexforge.core.parser.analysis.cfg;

import java.util.ArrayList;
import java.util.List;
import dexforge.core.parser.analysis.cfg.model.CfgBlock;

/**
 * Represents the complete Control Flow Graph of a method.
 */
public final class ControlFlowGraph {
    private final List<CfgBlock> blocks = new ArrayList<>();
    private CfgBlock entryBlock;

    public void addBlock(CfgBlock block) {
        blocks.add(block);
        if (entryBlock == null) entryBlock = block;
    }

    public List<CfgBlock> getBlocks() { return blocks; }
    public CfgBlock getEntryBlock() { return entryBlock; }

    public CfgBlock findBlockByOffset(int offset) {
        for (CfgBlock block : blocks) {
            if (offset >= block.getStartOffset() && offset <= block.getEndOffset()) {
                return block;
            }
        }
        return null;
    }
}
