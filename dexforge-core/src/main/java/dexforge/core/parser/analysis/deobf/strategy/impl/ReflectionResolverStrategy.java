package dexforge.core.parser.analysis.deobf.strategy.impl;

import dexforge.core.parser.analysis.deobf.strategy.IDeobfuscationStrategy;
import java.util.Map;

/**
 * REUSEABLE: Strategy to resolve class names hidden by Class.forName().
 */
public final class ReflectionResolverStrategy implements IDeobfuscationStrategy {

    @Override
    public String getStrategyId() { return "reflection-resolver"; }

    @Override
    public boolean matches(String methodSignature) {
        return methodSignature.contains("Ljava/lang/Class;->forName(Ljava/lang/String;)");
    }

    @Override
    public Object resolve(Object argument, Map<String, Object> context) {
        if (!(argument instanceof String)) return null;

        String className = (String) argument;
        // In real app, we would cross-check with deobfuscationMap
        return "RESOLVED_CLASS[" + className + "]";
    }
}
