package dexforge.core.parser.dex.decompiler;

import dexforge.core.parser.analysis.cfg.ControlFlowGraph;
import dexforge.core.parser.analysis.cfg.model.CfgBlock;
import dexforge.core.parser.analysis.dataflow.DataFlowAnalyzer;
import dexforge.core.parser.analysis.dataflow.engine.DalvikRegisterFact;
import dexforge.core.parser.dex.decompiler.analysis.DominatorTree;
import dexforge.core.parser.dex.decompiler.model.statements.*;
import dexforge.core.parser.dex.decompiler.model.expressions.JavaExpression;
import dexforge.core.parser.dex.model.DexInstruction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Restructures the flat CFG into high-level Java control structures (if, while, switch, try-catch).
 */
public final class ControlFlowRestructurer {

    public List<JavaStatement> restructure(ControlFlowGraph cfg, BiFunction<DexInstruction, DalvikRegisterFact, JavaExpression> exprProvider, DataFlowAnalyzer dfa) {
        DominatorTree domTree = new DominatorTree(cfg);
        domTree.compute();

        List<JavaStatement> result = new ArrayList<>();
        List<CfgBlock> blocks = cfg.getBlocks();
        Set<CfgBlock> processed = new HashSet<>();

        for (int i = 0; i < blocks.size(); i++) {
            CfgBlock block = blocks.get(i);
            if (processed.contains(block)) continue;

            DalvikRegisterFact state = dfa.getBlockEntryState(block);

            // 1. Detect Try-Catch
            if (!block.getExceptionHandlers().isEmpty()) {
                TryCatchStatement tryCatch = new TryCatchStatement();
                block.getInstructions().forEach(insn -> tryCatch.getTryBody().add(new BasicStatement(insn.toString())));

                for (CfgBlock.ExceptionHandler handler : block.getExceptionHandlers()) {
                    TryCatchStatement.CatchBlock catchBlock = new TryCatchStatement.CatchBlock(handler.getType(), "e");
                    handler.getHandlerBlock().getInstructions().forEach(insn ->
                        catchBlock.getBody().add(new BasicStatement(insn.toString())));
                    tryCatch.getCatchBlocks().add(catchBlock);
                    processed.add(handler.getHandlerBlock());
                }

                result.add(tryCatch);
                processed.add(block);
                continue;
            }

            // 2. Detect Switch
            if (isSwitchBlock(block)) {
                SwitchStatement switchStmt = new SwitchStatement("var");
                for (int j = 0; j < block.getSuccessors().size(); j++) {
                    CfgBlock target = block.getSuccessors().get(j);
                    List<JavaStatement> caseBody = new ArrayList<>();
                    target.getInstructions().forEach(insn -> caseBody.add(new BasicStatement(insn.toString())));
                    switchStmt.addCase(j, caseBody);
                    processed.add(target);
                }
                result.add(switchStmt);
                processed.add(block);
                continue;
            }

            // 3. Detect Loop (While)
            if (isLoopHeader(block)) {
                DexInstruction last = block.getInstructions().get(block.getInstructions().size() - 1);
                WhileStatement whileStmt = new WhileStatement(exprProvider.apply(last, state));
                if (block.getSuccessors().size() >= 2) {
                    CfgBlock bodyBlock = block.getSuccessors().get(1);
                    for (DexInstruction insn : bodyBlock.getInstructions()) {
                        whileStmt.getBody().add(new BasicStatement(insn.toString()));
                    }
                    processed.add(bodyBlock);
                }
                result.add(whileStmt);
                processed.add(block);
                continue;
            }

            // 4. Detect IF
            DexInstruction lastInsn = block.getInstructions().isEmpty() ? null : block.getInstructions().get(block.getInstructions().size() - 1);
            int op = lastInsn != null ? lastInsn.getOpcode() & 0xFF : -1;

            if (isConditionalJump(op) && block.getSuccessors().size() == 2) {
                IfStatement ifStmt = new IfStatement(exprProvider.apply(lastInsn, state));
                CfgBlock thenBlock = block.getSuccessors().get(1);

                if (!processed.contains(thenBlock)) {
                    for (DexInstruction insn : thenBlock.getInstructions()) {
                        ifStmt.getThenBranch().add(new BasicStatement(insn.toString()));
                    }
                    processed.add(thenBlock);
                }

                result.add(ifStmt);
                processed.add(block);
            } else {
                for (DexInstruction insn : block.getInstructions()) {
                    result.add(new BasicStatement(insn.toString()));
                }
                processed.add(block);
            }
        }

        return result;
    }

    private boolean isSwitchBlock(CfgBlock block) {
        if (block.getInstructions().isEmpty()) return false;
        DexInstruction last = block.getInstructions().get(block.getInstructions().size() - 1);
        int op = last.getOpcode() & 0xFF;
        return op == 0x2B || op == 0x2C;
    }

    private boolean isLoopHeader(CfgBlock block) {
        for (CfgBlock pred : block.getPredecessors()) {
            if (pred.getId() > block.getId()) return true;
        }
        return false;
    }

    private boolean isConditionalJump(int op) {
        return (op >= 0x32 && op <= 0x3D);
    }
}
