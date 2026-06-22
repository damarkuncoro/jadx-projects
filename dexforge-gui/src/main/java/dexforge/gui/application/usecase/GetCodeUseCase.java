package dexforge.gui.application.usecase;

import java.util.Objects;

import dexforge.gui.application.port.DecompilerPort;

/**
 * Use case for retrieving decompiled code for a specific class.
 */
public final class GetCodeUseCase {
	private final DecompilerPort decompilerPort;

	public GetCodeUseCase(DecompilerPort decompilerPort) {
		this.decompilerPort = Objects.requireNonNull(decompilerPort);
	}

	public String execute(String className) {
		return decompilerPort.getCode(className);
	}
}
