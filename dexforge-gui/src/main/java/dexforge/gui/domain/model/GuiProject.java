package dexforge.gui.domain.model;

import java.io.File;
import java.util.Objects;

/**
 * Domain entity representing a project in the GUI.
 */
public final class GuiProject {
	private final File inputFile;
	private final String name;

	public GuiProject(File inputFile) {
		this.inputFile = Objects.requireNonNull(inputFile);
		this.name = inputFile.getName();
	}

	public File getInputFile() {
		return inputFile;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "GuiProject{" + "name='" + name + '\'' + '}';
	}
}
