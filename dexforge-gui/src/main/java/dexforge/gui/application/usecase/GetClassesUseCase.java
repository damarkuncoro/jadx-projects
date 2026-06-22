package dexforge.gui.application.usecase;

import java.util.List;
import java.util.Objects;

import dexforge.gui.application.port.DecompilerPort;

/**
 * Use case for retrieving the list of classes from the current project.
 */
public final class GetClassesUseCase {
	private final DecompilerPort decompilerPort;

	public GetClassesUseCase(DecompilerPort decompilerPort) {
		this.decompilerPort = Objects.requireNonNull(decompilerPort);
	}

	public List<String> execute() {
		return decompilerPort.getClasses();
	}
}
