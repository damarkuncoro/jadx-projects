package dexforge.engine.jadx.infrastructure;

import dexforge.core.ports.decompile.DecompilerEngine;
import dexforge.core.ports.decompile.DecompilerSession;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;

public class JadxDecompilerEngine implements DecompilerEngine {
	private final JadxArgs args;

	public JadxDecompilerEngine(JadxArgs args) {
		this.args = args;
	}

	@Override
	public DecompilerSession open() {
		return new JadxDecompilerSession(new JadxDecompiler(args));
	}
}
