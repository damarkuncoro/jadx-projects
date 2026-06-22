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
	private final DexForgeProjectSessionFactory projectSessionFactory;

	private DexForgeEngine(DecompilerEngine decompilerEngine) {
		this(decompilerEngine, DexForgePostLoadActionFactory.NO_OP, null);
	}

	private DexForgeEngine(DecompilerEngine decompilerEngine, DexForgePostLoadActionFactory postLoadActionFactory) {
		this(decompilerEngine, postLoadActionFactory, null);
	}

	private DexForgeEngine(
			DecompilerEngine decompilerEngine,
			DexForgePostLoadActionFactory postLoadActionFactory,
			DexForgeProjectSessionFactory projectSessionFactory) {
		this.decompileService = new DecompileApplicationService(
				Objects.requireNonNull(decompilerEngine, "DecompilerEngine cannot be null"));
		this.postLoadActionFactory = Objects.requireNonNull(postLoadActionFactory, "Post-load action factory cannot be null");
		this.projectSessionFactory = projectSessionFactory;
	}

	public static DexForgeEngine using(DecompilerEngine decompilerEngine) {
		return new DexForgeEngine(decompilerEngine);
	}

	public static DexForgeEngine using(DecompilerEngine decompilerEngine, DexForgePostLoadActionFactory postLoadActionFactory) {
		return new DexForgeEngine(decompilerEngine, postLoadActionFactory);
	}

	public static DexForgeEngine using(
			DecompilerEngine decompilerEngine,
			DexForgePostLoadActionFactory postLoadActionFactory,
			DexForgeProjectSessionFactory projectSessionFactory) {
		return new DexForgeEngine(decompilerEngine, postLoadActionFactory, projectSessionFactory);
	}

	public DexForgeSession openSession() {
		return new DexForgeSession(decompileService, postLoadActionFactory);
	}

	public DexForgeProjectSession openProject(DexForgeOpenProjectRequest request) {
		if (projectSessionFactory == null) {
			throw new UnsupportedOperationException("Project sessions are not available for this engine backend");
		}
		return projectSessionFactory.open(Objects.requireNonNull(request, "Open project request cannot be null"));
	}
}
