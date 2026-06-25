package dexforge.core.service.intelligence.registry;

import dexforge.core.parser.analysis.deobf.strategy.IDeobfuscationStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * REUSEABLE: Central registry for all deobfuscation strategies and cached results.
 */
public final class DeobfuscationRegistry {
    private final List<IDeobfuscationStrategy> strategies = new ArrayList<>();
    private final Map<String, Object> resolvedValuesCache = new HashMap<>();

    public void registerStrategy(IDeobfuscationStrategy strategy) {
        strategies.add(strategy);
    }

    public IDeobfuscationStrategy findMatch(String methodSignature) {
        for (IDeobfuscationStrategy s : strategies) {
            if (s.matches(methodSignature)) return s;
        }
        return null;
    }

    public void cacheResult(String callSite, Object value) {
        resolvedValuesCache.put(callSite, value);
    }

    public Object getCachedValue(String callSite) {
        return resolvedValuesCache.get(callSite);
    }

    public Map<String, Object> getCache() {
        return resolvedValuesCache;
    }
}
