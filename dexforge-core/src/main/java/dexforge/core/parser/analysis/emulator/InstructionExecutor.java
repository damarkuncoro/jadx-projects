package dexforge.core.parser.analysis.emulator;

import dexforge.core.parser.dex.model.DexInstruction;
import java.util.Map;

/**
 * Interface for executing a specific Dalvik instruction opcode.
 */
@FunctionalInterface
public interface InstructionExecutor {
    void execute(DexInstruction insn, Map<Integer, Object> registers);
}
