package dexforge.gui.application.usecase;

import java.util.List;
import java.util.Objects;

import dexforge.api.analysis.DexForgeFinding;
import dexforge.gui.application.port.DecompilerPort;

public final class RunAnalysisUseCase {
	private final DecompilerPort decompilerPort;

	public RunAnalysisUseCase(DecompilerPort decompilerPort) {
		this.decompilerPort = Objects.requireNonNull(decompilerPort);
	}

	public List<DexForgeFinding> execute() {
		return decompilerPort.runAnalysis();
	}
}
