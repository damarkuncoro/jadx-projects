package dexforge.api.engine;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for available decompiler engines.
 */
public final class EngineRegistry {
	private static final Map<String, DexForgeEngine> ENGINES = new ConcurrentHashMap<>();

	static {
		loadEngines();
	}

	public static void loadEngines() {
		ServiceLoader<DexForgeEngine> loader = ServiceLoader.load(DexForgeEngine.class);
		for (DexForgeEngine engine : loader) {
			register(engine);
		}
	}

	public static void register(DexForgeEngine engine) {
		ENGINES.put(engine.getEngineId(), engine);
	}

	public static Optional<DexForgeEngine> get(String id) {
		if (ENGINES.isEmpty()) {
			loadEngines();
		}
		return Optional.ofNullable(ENGINES.get(id));
	}

	public static DexForgeEngine getDefault() {
		if (ENGINES.isEmpty()) {
			loadEngines();
		}
		return ENGINES.values().stream().findFirst()
				.orElseThrow(() -> new IllegalStateException("No decompiler engine registered!"));
	}
}
