package dexforge.core.parser.analysis.deobf.strategy.impl;

import dexforge.core.parser.analysis.deobf.strategy.IDeobfuscationStrategy;
import java.util.Map;

/**
 * REUSEABLE: Strategy to handle common XOR-based string obfuscation.
 * Logic: original = obscured ^ key
 */
public final class GenericXorStrategy implements IDeobfuscationStrategy {
    private final String pattern;
    private final int hardcodedKey;

    public GenericXorStrategy(String methodPattern, int key) {
        this.pattern = methodPattern;
        this.hardcodedKey = key;
    }

    @Override
    public String getStrategyId() { return "generic-xor"; }

    @Override
    public boolean matches(String methodSignature) {
        return methodSignature.contains(pattern);
    }

    @Override
    public Object resolve(Object argument, Map<String, Object> context) {
        if (!(argument instanceof Integer)) return null;

        int obscured = (Integer) argument;
        int decrypted = obscured ^ hardcodedKey;

        // Convert to readable string (heuristic)
        return "XOR_RESOLVED_" + decrypted;
    }
}
