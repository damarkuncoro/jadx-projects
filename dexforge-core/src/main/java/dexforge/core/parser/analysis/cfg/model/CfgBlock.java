package dexforge.core.parser.analysis.cfg.model;

import java.util.ArrayList;
import java.util.List;
import dexforge.core.parser.dex.model.DexInstruction;

/**
 * Represents a Basic Block in the Control Flow Graph.
 * A block has one entry point and one exit point (the last instruction).
 */
public final class CfgBlock {
    private final int id;
    private final int startOffset;
    private int endOffset;
    private final List<DexInstruction> instructions = new ArrayList<>();
    private final List<CfgBlock> predecessors = new ArrayList<>();
    private final List<CfgBlock> successors = new ArrayList<>();
    private final List<ExceptionHandler> exceptionHandlers = new ArrayList<>();

    public CfgBlock(int id, int startOffset) {
        this.id = id;
        this.startOffset = startOffset;
    }

    public int getId() { return id; }
    public int getStartOffset() { return startOffset; }
    public int getEndOffset() { return endOffset; }
    public void setEndOffset(int endOffset) { this.endOffset = endOffset; }

    public List<DexInstruction> getInstructions() { return instructions; }
    public List<CfgBlock> getPredecessors() { return predecessors; }
    public List<CfgBlock> getSuccessors() { return successors; }
    public List<ExceptionHandler> getExceptionHandlers() { return exceptionHandlers; }

    public void addInstruction(DexInstruction insn) {
        instructions.add(insn);
    }

    public void addSuccessor(CfgBlock successor) {
        if (!successors.contains(successor)) {
            successors.add(successor);
            successor.predecessors.add(this);
        }
    }

    public void addExceptionHandler(String type, CfgBlock handlerBlock) {
        exceptionHandlers.add(new ExceptionHandler(type, handlerBlock));
    }

    public static final class ExceptionHandler {
        private final String type;
        private final CfgBlock handlerBlock;

        public ExceptionHandler(String type, CfgBlock handlerBlock) {
            this.type = type;
            this.handlerBlock = handlerBlock;
        }

        public String getType() { return type; }
        public CfgBlock getHandlerBlock() { return handlerBlock; }
    }
}
