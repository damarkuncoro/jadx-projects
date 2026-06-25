package dexforge.gui.application.usecase;

import dexforge.gui.application.port.DecompilerPort;
import java.util.Objects;

public final class GetSmaliUseCase {
	private final DecompilerPort decompilerPort;

	public GetSmaliUseCase(DecompilerPort decompilerPort) {
		this.decompilerPort = Objects.requireNonNull(decompilerPort);
	}

	public String execute(String className) {
		return decompilerPort.getSmali(className);
	}
}
