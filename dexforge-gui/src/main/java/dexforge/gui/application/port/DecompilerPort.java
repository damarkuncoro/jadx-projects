package dexforge.gui.application.port;

import java.io.File;
import java.util.List;

import dexforge.gui.domain.model.GuiProject;

/**
 * Port for interacting with the decompiler engine.
 * Defined in Application layer, implemented in Infrastructure.
 */
public interface DecompilerPort {
	void open(GuiProject project);

	List<String> getClasses();

	String getCode(String className);

	void close();
}
