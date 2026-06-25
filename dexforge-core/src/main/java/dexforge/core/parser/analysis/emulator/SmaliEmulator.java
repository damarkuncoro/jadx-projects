package dexforge.core.parser.analysis.emulator;

import dexforge.core.parser.dex.model.DexInstruction;
import dexforge.core.parser.analysis.emulator.library.VirtualMethodHandler;
import dexforge.core.parser.dex.sections.DexOpcode;
import dexforge.core.parser.dex.service.DexFastIndexer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REUSEABLE: Orchestrates the execution of Dalvik bytecode by delegating to specialized handlers.
 * Follows SOLID principles: Open for extension (new opcodes/handlers), closed for modification.
 */
public final class SmaliEmulator {
    private final EmulatorState state = new EmulatorState();
    private final MethodInvokeHandler invokeHandler;
    private final ControlFlowHandler cfHandler;
    private InstructionSet instructionSet;
    private DexFastIndexer indexer;
    private final List<DexFastIndexer> allIndexers = new ArrayList<>();

    public SmaliEmulator() {
        this.invokeHandler = new MethodInvokeHandler(state.getRegisters());
        this.invokeHandler.setEmulator(this);
        this.cfHandler = new ControlFlowHandler(state.getRegisters());
        this.instructionSet = new InstructionSet(state, null);
    }

    public void setIndexer(DexFastIndexer indexer) {
        this.indexer = indexer;
        this.instructionSet = new InstructionSet(state, indexer);
        if (!allIndexers.contains(indexer)) {
            allIndexers.add(indexer);
        }
    }

    public void setAllIndexers(List<DexFastIndexer> indexers) {
        this.allIndexers.clear();
        this.allIndexers.addAll(indexers);
    }

    public void registerHandler(VirtualMethodHandler handler) {
        invokeHandler.registerHandler(handler);
    }

    public Object executeMethod(String signature, Map<Integer, Object> initialRegisters) {
        // Search for method in all indexers
        List<DexInstruction> instructions = null;
        DexFastIndexer targetIndexer = null;

        for (DexFastIndexer idx : allIndexers) {
            instructions = idx.getMethodInstructions(signature);
            if (instructions != null && !instructions.isEmpty()) {
                targetIndexer = idx;
                break;
            }
        }

        if (instructions == null || targetIndexer == null) return null;

        // Save current indexer and swap to target
        DexFastIndexer oldIndexer = this.indexer;
        this.indexer = targetIndexer;
        this.instructionSet = new InstructionSet(state, targetIndexer);

        try {
            return execute(instructions, initialRegisters);
        } finally {
            // Restore indexer
            this.indexer = oldIndexer;
            this.instructionSet = new InstructionSet(state, oldIndexer);
        }
    }

    public Object execute(List<DexInstruction> instructions, Map<Integer, Object> initialRegisters) {
        state.clear();
        state.getRegisters().putAll(initialRegisters);

        Map<Integer, Integer> offsetToInsnIndex = new HashMap<>();
        for (int i = 0; i < instructions.size(); i++) {
            offsetToInsnIndex.put(instructions.get(i).getOffset(), i);
        }

        int index = 0;
        int instructionsExecuted = 0;
        while (index < instructions.size()) {
            if (instructionsExecuted++ > 10000) {
                System.err.println("  Emulator: Execution limit reached (infinite loop?)");
                break;
            }

            DexInstruction insn = instructions.get(index);
            int op = insn.getOpcode() & 0xFF;
            InstructionExecutor executor = (instructionSet != null) ? instructionSet.getExecutor(op) : null;

            boolean jumped = false;

            if (executor != null) {
                executor.execute(insn, state.getRegisters());
            } else if (DexOpcode.isInvoke(op)) {
                if (indexer != null && insn.getIndex() >= 0) {
                    String signature = indexer.getMethodPool().getMethodSignature(insn.getIndex());
                    List<Object> args = invokeHandler.getInvokeArgs(insn);
                    Object result = invokeHandler.handleInvoke(signature, args);
                    state.recordResult(signature, insn, result);
                }
            } else if (op >= 0x0E && op <= 0x11) { // RETURN
                int reg = (insn.getOpcode() >> 8) & 0xFF;
                return state.getRegisters().get(reg);
            } else if (op == 0x28 || op == 0x29 || op == 0x2A) { // GOTO
                Integer targetIdx = offsetToInsnIndex.get(cfHandler.getGotoTarget(insn));
                if (targetIdx != null) {
                    index = targetIdx;
                    jumped = true;
                }
            } else if (op >= 0x32 && op <= 0x3D) { // IF-*
                if (cfHandler.shouldBranch(insn)) {
                    Integer targetIdx = offsetToInsnIndex.get(cfHandler.getBranchTarget(insn));
                    if (targetIdx != null) {
                        index = targetIdx;
                        jumped = true;
                    }
                }
            }

            if (!jumped) index++;
        }
        return state.getRegisters().get(0);
    }
}
