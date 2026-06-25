package dexforge.core.parser.analysis.deobf.strategy.impl;

import dexforge.core.parser.analysis.deobf.strategy.IDeobfuscationStrategy;
import dexforge.core.parser.analysis.emulator.SmaliEmulator;
import dexforge.core.parser.dex.model.DexInstruction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SOLID: Strategy that uses the SmaliEmulator to resolve values.
 * Extremely REUSEABLE for any method-based deobfuscation.
 */
public final class EmulatedDeobfuscationStrategy implements IDeobfuscationStrategy {
    private final String targetMethodSignature;
    private final List<DexInstruction> methodInstructions;
    private final SmaliEmulator emulator = new SmaliEmulator();

    public EmulatedDeobfuscationStrategy(String signature, List<DexInstruction> instructions) {
        this.targetMethodSignature = signature;
        this.methodInstructions = instructions;
    }

    @Override
    public String getStrategyId() { return "emulated-resolver"; }

    @Override
    public boolean matches(String methodSignature) {
        return methodSignature.equals(targetMethodSignature);
    }

    @Override
    public Object resolve(Object argument, Map<String, Object> context) {
        Map<Integer, Object> initialRegs = new HashMap<>();
        // Map the first argument to register v0 (Dalvik convention for static params)
        initialRegs.put(0, argument);

        return emulator.execute(methodInstructions, initialRegs);
    }
}
