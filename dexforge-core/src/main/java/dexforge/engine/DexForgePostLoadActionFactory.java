package dexforge.engine;

import dexforge.core.application.decompile.DecompilePostLoadAction;

@FunctionalInterface
public interface DexForgePostLoadActionFactory {
	DexForgePostLoadActionFactory NO_OP = request -> DecompilePostLoadAction.NO_OP;

	DecompilePostLoadAction create(DexForgeDecompileRequest request);
}
