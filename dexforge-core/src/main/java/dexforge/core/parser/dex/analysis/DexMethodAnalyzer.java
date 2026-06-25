package dexforge.core.parser.dex.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dexforge.core.parser.dex.model.DexCode;
import dexforge.core.parser.dex.service.DexFastIndexer;

/**
 * Performs simple data flow analysis on a method to track constant values.
 */
public final class DexMethodAnalyzer {
    private final DexFastIndexer indexer;

    public DexMethodAnalyzer(DexFastIndexer indexer) {
        this.indexer = indexer;
    }

    public static final class CallContext {
        public final String methodSignature;
        public final List<RegisterState> arguments;
        public final int address;

        public CallContext(String methodSignature, List<RegisterState> arguments, int address) {
            this.methodSignature = methodSignature;
            this.arguments = arguments;
            this.address = address;
        }
    }

    public List<CallContext> analyzeCalls(DexCode code) {
        List<CallContext> calls = new ArrayList<>();
        if (code == null) return calls;

        short[] insns = code.getInstructions();
        RegisterState[] registers = new RegisterState[code.getRegistersSize()];
        for (int i = 0; i < registers.length; i++) registers[i] = RegisterState.UNKNOWN;

        int i = 0;
        while (i < insns.length) {
            int opcode = insns[i] & 0xFF;

            // Track constants
            if (opcode == 0x1A || opcode == 0x1B) { // CONST-STRING
                int reg = (insns[i] >> 8) & 0xFF;
                int strIdx = (opcode == 0x1A) ? (insns[i+1] & 0xFFFF) :
                            ((insns[i+1] & 0xFFFF) | ((insns[i+2] & 0xFFFF) << 16));
                registers[reg] = new RegisterState(RegisterState.Type.STRING, indexer.getStringPool().getString(strIdx));
            }
            else if (opcode >= 0x12 && opcode <= 0x14) { // CONST (4, 16, 32 bit)
                int reg = (insns[i] >> 8) & 0xFF;
                // Simplified: just store as Integer for now
                registers[reg] = new RegisterState(RegisterState.Type.INTEGER, "const-val");
            }

            // Analyze calls
            else if (opcode >= 0x6E && opcode <= 0x72) { // INVOKE-KIND
                int methIdx = insns[i+1] & 0xFFFF;
                String sig = indexer.getMethodPool().getMethodSignature(methIdx);

                // Extract arguments from registers (simplified for the first 5 regs)
                List<RegisterState> args = new ArrayList<>();
                int argRegs = (insns[i] >> 12) & 0x0F;
                // vC, vD, vE, vF, vG are stored in subsequent nibbles of insns[i+2] and insns[i]
                // For simplicity, we'll just record the call with current register states
                calls.add(new CallContext(sig, args, i));
            }

            // Move to next instruction
            int length = dexforge.core.parser.dex.sections.DexOpcode.getFormatLength(opcode);
            if (length <= 0) break;
            i += length;
        }

        return calls;
    }
}
