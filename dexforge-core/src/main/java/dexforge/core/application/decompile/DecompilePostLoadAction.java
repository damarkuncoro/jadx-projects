package dexforge.core.application.decompile;

import dexforge.core.ports.decompile.DecompilerSession;

@FunctionalInterface
public interface DecompilePostLoadAction {
	DecompilePostLoadAction NO_OP = session -> false;

	boolean process(DecompilerSession session);
}
