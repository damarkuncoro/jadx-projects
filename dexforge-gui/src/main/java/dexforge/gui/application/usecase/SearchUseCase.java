package dexforge.gui.application.usecase;

import java.util.List;
import java.util.Objects;

import dexforge.api.model.DexForgeNode;
import dexforge.gui.application.port.DecompilerPort;

/**
 * Use case for searching text or patterns across the entire project.
 */
public final class SearchUseCase {
	private final DecompilerPort decompilerPort;

	public SearchUseCase(DecompilerPort decompilerPort) {
		this.decompilerPort = Objects.requireNonNull(decompilerPort);
	}

	public List<DexForgeNode> execute(String query) {
		return decompilerPort.search(query);
	}
}
