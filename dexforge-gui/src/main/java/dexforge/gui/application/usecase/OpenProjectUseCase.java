package dexforge.gui.application.usecase;

import java.io.File;
import java.util.Objects;

import dexforge.gui.application.port.DecompilerPort;
import dexforge.gui.domain.model.GuiProject;

/**
 * Use case for opening a new project.
 * SRP: Only handles the orchestration of opening a project.
 */
public final class OpenProjectUseCase {
	private final DecompilerPort decompilerPort;

	public OpenProjectUseCase(DecompilerPort decompilerPort) {
		this.decompilerPort = Objects.requireNonNull(decompilerPort);
	}

	public GuiProject execute(File inputFile, String engineId) {
		GuiProject project = new GuiProject(inputFile);
		decompilerPort.open(project, engineId);
		return project;
	}
}
