package dexforge.core.infrastructure.jadx;

import dexforge.core.application.decompile.SingleClassDecompileOptions;
import dexforge.engine.DexForgeEngine;

import jadx.api.JadxArgs;

/**
 * Factory for DexForge engine instances backed by the current JADX implementation.
 */
public final class JadxBackedDexForgeEngine {
	private JadxBackedDexForgeEngine() {
	}

	public static DexForgeEngine create() {
		return create(new JadxArgs());
	}

	public static DexForgeEngine create(JadxArgs args) {
		return DexForgeEngine.using(new JadxDecompilerEngine(args), request -> new JadxSingleClassDecompileAction(
				new SingleClassDecompileOptions(request.getSingleClassName(), request.getSingleClassOutputPath())),
				request -> JadxProjectSession.open(request, args));
	}
}
