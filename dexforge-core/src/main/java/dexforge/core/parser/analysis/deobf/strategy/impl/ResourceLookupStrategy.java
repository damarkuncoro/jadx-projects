package dexforge.core.parser.analysis.deobf.strategy.impl;

import dexforge.core.parser.analysis.deobf.strategy.IDeobfuscationStrategy;
import dexforge.core.parser.resolver.ResourceResolver;
import java.util.Map;

/**
 * REUSEABLE: Strategy to resolve strings hidden behind resource IDs.
 * Common in apps that use R.string.xxx dynamically.
 */
public final class ResourceLookupStrategy implements IDeobfuscationStrategy {
    private final ResourceResolver resolver;

    public ResourceLookupStrategy(ResourceResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public String getStrategyId() { return "resource-lookup"; }

    @Override
    public boolean matches(String methodSignature) {
        return methodSignature.contains("getString(I)") || methodSignature.contains("getText(I)");
    }

    @Override
    public Object resolve(Object argument, Map<String, Object> context) {
        if (!(argument instanceof Integer)) return null;

        int resId = (Integer) argument;
        String resolved = resolver.resolve(resId);
        return resolved != null ? resolved : String.format("R.id.0x%08x", resId);
    }
}
