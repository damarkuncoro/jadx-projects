package dexforge.gui.application.usecase;

import java.util.List;
import java.util.Objects;

import dexforge.api.resource.DexForgeResourceFile;
import dexforge.gui.application.port.DecompilerPort;

public final class GetResourcesUseCase {
	private final DecompilerPort decompilerPort;

	public GetResourcesUseCase(DecompilerPort decompilerPort) {
		this.decompilerPort = Objects.requireNonNull(decompilerPort);
	}

	public List<DexForgeResourceFile> execute() {
		return decompilerPort.getResources();
	}
}
