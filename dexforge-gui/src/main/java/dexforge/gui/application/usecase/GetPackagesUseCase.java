package dexforge.gui.application.usecase;

import java.util.List;
import java.util.Objects;

import dexforge.api.core.DexForgeProject;
import dexforge.api.model.DexForgePackage;
import dexforge.gui.application.port.DecompilerPort;

public final class GetPackagesUseCase {
	private final DecompilerPort decompilerPort;

	public GetPackagesUseCase(DecompilerPort decompilerPort) {
		this.decompilerPort = Objects.requireNonNull(decompilerPort);
	}

	public List<DexForgePackage> execute() {
		// We'll need to expose DexForgeProject or a method to get root packages in Port
		return decompilerPort.getRootPackages();
	}
}
