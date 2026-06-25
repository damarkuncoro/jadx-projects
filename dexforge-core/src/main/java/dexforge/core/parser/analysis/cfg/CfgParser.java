package dexforge.core.parser.analysis.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dexforge.core.parser.analysis.cfg.model.CfgBlock;
import dexforge.core.parser.dex.model.DexInstruction;

/**
 * Parser to build a Control Flow Graph from a list of instructions.
 */
public final class CfgParser {

    public ControlFlowGraph parse(List<DexInstruction> instructions) {
        if (instructions.isEmpty()) return new ControlFlowGraph();

        // 1. Identify leaders (start of basic blocks)
        TreeMap<Integer, Boolean> leaders = new TreeMap<>();
        leaders.put(0, true); // First instruction is always a leader

        for (int i = 0; i < instructions.size(); i++) {
            DexInstruction insn = instructions.get(i);
            int op = insn.getOpcode() & 0xFF;

            if (isJump(op)) {
                // Target of a jump is a leader
                int target = insn.getOffset() + insn.getIndex(); // Simplified: target calculation depends on format
                leaders.put(target, true);

                // Instruction following a jump/branch is a leader
                if (i + 1 < instructions.size()) {
                    leaders.put(instructions.get(i + 1).getOffset(), true);
                }
            } else if (isReturnOrThrow(op)) {
                // Instruction following a return is a leader (though usually dead code)
                if (i + 1 < instructions.size()) {
                    leaders.put(instructions.get(i + 1).getOffset(), true);
                }
            }
        }

        // 2. Create blocks
        ControlFlowGraph cfg = new ControlFlowGraph();
        Map<Integer, CfgBlock> offsetToBlock = new HashMap<>();
        int blockId = 0;

        CfgBlock currentBlock = null;
        for (DexInstruction insn : instructions) {
            if (leaders.containsKey(insn.getOffset())) {
                if (currentBlock != null) {
                    currentBlock.setEndOffset(insn.getOffset() - 1);
                }
                currentBlock = new CfgBlock(blockId++, insn.getOffset());
                cfg.addBlock(currentBlock);
                offsetToBlock.put(insn.getOffset(), currentBlock);
            }
            currentBlock.addInstruction(insn);
            currentBlock.setEndOffset(insn.getOffset()); // Temporarily update
        }

        // 3. Connect blocks (Edges)
        for (CfgBlock block : cfg.getBlocks()) {
            DexInstruction lastInsn = block.getInstructions().get(block.getInstructions().size() - 1);
            int op = lastInsn.getOpcode() & 0xFF;

            if (isReturnOrThrow(op)) {
                // No successors
            } else if (isUnconditionalJump(op)) {
                int target = lastInsn.getOffset() + lastInsn.getIndex();
                CfgBlock targetBlock = offsetToBlock.get(target);
                if (targetBlock != null) block.addSuccessor(targetBlock);
            } else if (isConditionalJump(op)) {
                // Successor 1: Jump target
                int target = lastInsn.getOffset() + lastInsn.getIndex();
                CfgBlock targetBlock = offsetToBlock.get(target);
                if (targetBlock != null) block.addSuccessor(targetBlock);

                // Successor 2: Next block
                CfgBlock nextBlock = findBlockStartingAt(cfg, lastInsn.getOffset() + lastInsn.getLength());
                if (nextBlock != null) block.addSuccessor(nextBlock);
            } else {
                // Fall-through
                CfgBlock nextBlock = findBlockStartingAt(cfg, lastInsn.getOffset() + lastInsn.getLength());
                if (nextBlock != null) block.addSuccessor(nextBlock);
            }
        }

        return cfg;
    }

    private CfgBlock findBlockStartingAt(ControlFlowGraph cfg, int offset) {
        for (CfgBlock b : cfg.getBlocks()) {
            if (b.getStartOffset() == offset) return b;
        }
        return null;
    }

    private boolean isJump(int op) {
        return (op >= 0x28 && op <= 0x3D); // GOTO, IF-*
    }

    private boolean isUnconditionalJump(int op) {
        return (op >= 0x28 && op <= 0x2A); // GOTO
    }

    private boolean isConditionalJump(int op) {
        return (op >= 0x32 && op <= 0x3D); // IF-*
    }

    private boolean isReturnOrThrow(int op) {
        return (op >= 0x0E && op <= 0x11) || (op == 0x27); // RETURN, THROW
    }
}
