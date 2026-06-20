package dexforge.core.infrastructure.jadx;

import dexforge.core.ports.decompile.DecompilerEngine;
import dexforge.core.ports.decompile.DecompilerSession;

import jadx.api.JadxArgs;

public final class JadxDecompilerEngine implements DecompilerEngine {
	private final JadxArgs args;

	public JadxDecompilerEngine(JadxArgs args) {
		this.args = args;
	}

	@Override
	public DecompilerSession open() {
		return new JadxDecompilerSession(args);
	}
}
