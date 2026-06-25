package dexforge.infrastructure.adapter;

import dexforge.api.engine.DexForgeEngine;

/**
 * Application Context: Dependency Injection container.
 * Now engine-agnostic.
 */
public class ApplicationContext {
	private final DexForgeEngine engine;

	public ApplicationContext(DexForgeEngine engine) {
		this.engine = engine;
	}

	public DexForgeEngine getEngine() {
		return engine;
	}
}
