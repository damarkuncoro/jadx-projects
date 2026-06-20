package dexforge.engine;

import java.util.Objects;

import dexforge.core.application.decompile.DecompileApplicationService;
import dexforge.core.ports.decompile.DecompilerEngine;

/**
 * Public DexForge engine boundary for clients that should not depend directly on JADX internals.
 */
public final class DexForgeEngine {
	private final DecompileApplicationService decompileService;
	private final DexForgePostLoadActionFactory postLoadActionFactory;

	private DexForgeEngine(DecompilerEngine decompilerEngine) {
		this(decompilerEngine, DexForgePostLoadActionFactory.NO_OP);
	}

	private DexForgeEngine(DecompilerEngine decompilerEngine, DexForgePostLoadActionFactory postLoadActionFactory) {
		this.decompileService = new DecompileApplicationService(
				Objects.requireNonNull(decompilerEngine, "DecompilerEngine cannot be null"));
		this.postLoadActionFactory = Objects.requireNonNull(postLoadActionFactory, "Post-load action factory cannot be null");
	}

	public static DexForgeEngine using(DecompilerEngine decompilerEngine) {
		return new DexForgeEngine(decompilerEngine);
	}

	public static DexForgeEngine using(DecompilerEngine decompilerEngine, DexForgePostLoadActionFactory postLoadActionFactory) {
		return new DexForgeEngine(decompilerEngine, postLoadActionFactory);
	}

	public DexForgeSession openSession() {
		return new DexForgeSession(decompileService, postLoadActionFactory);
	}
}
