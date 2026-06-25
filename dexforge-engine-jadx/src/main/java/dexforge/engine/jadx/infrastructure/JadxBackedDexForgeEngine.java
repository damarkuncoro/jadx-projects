package dexforge.engine.jadx.infrastructure;

import dexforge.engine.DexForgeEngine;
import dexforge.engine.DexForgePostLoadActionFactory;
import dexforge.engine.jadx.session.JadxProjectSessionFactory;
import jadx.api.JadxArgs;

/**
 * Factory and bridge for JADX-backed DexForgeEngine.
 */
public final class JadxBackedDexForgeEngine {

	public static DexForgeEngine create(JadxArgs args) {
		return DexForgeEngine.using(
				new JadxDecompilerEngine(args),
				DexForgePostLoadActionFactory.NO_OP,
				new JadxProjectSessionFactory(args));
	}

	public static DexForgeEngine create() {
		return create(new JadxArgs());
	}

	private JadxBackedDexForgeEngine() {
	}
}
