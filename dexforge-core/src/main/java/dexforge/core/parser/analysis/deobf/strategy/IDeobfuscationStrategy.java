package dexforge.core.parser.analysis.deobf.strategy;

import java.util.Map;

/**
 * REUSEABLE: Interface for different deobfuscation techniques.
 */
public interface IDeobfuscationStrategy {
    /**
     * Unique ID of the strategy (e.g., "generic-xor", "array-lookup").
     */
    String getStrategyId();

    /**
     * Determines if a method call matches this deobfuscation pattern.
     */
    boolean matches(String methodSignature);

    /**
     * Performs the actual transformation logic.
     * @param argument The constant argument found in code.
     * @param context Global context (static fields, etc.)
     * @return The deobfuscated value (usually a String).
     */
    Object resolve(Object argument, Map<String, Object> context);
}
