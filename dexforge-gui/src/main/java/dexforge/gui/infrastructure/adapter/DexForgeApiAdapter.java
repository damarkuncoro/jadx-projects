package dexforge.gui.infrastructure.adapter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import dexforge.api.core.DexForgeDecompiler;
import dexforge.api.core.DexForgeProject;
import dexforge.api.model.DexForgeClass;
import dexforge.gui.application.port.DecompilerPort;
import dexforge.gui.domain.model.GuiProject;

/**
 * Adapter implementing DecompilerPort using DexForge API.
 * This is the ONLY place that knows about dexforge.api.
 * It must NOT import jadx.*.
 */
public final class DexForgeApiAdapter implements DecompilerPort {
	private DexForgeProject project;

	@Override
	public void open(GuiProject guiProject) {
		if (project != null) {
			project.close();
		}
		project = DexForgeDecompiler.open(guiProject.getInputFile());
	}

	@Override
	public List<String> getClasses() {
		if (project == null) {
			return Collections.emptyList();
		}
		return project.getClasses().stream()
				.map(DexForgeClass::getFullName)
				.collect(Collectors.toList());
	}

	@Override
	public String getCode(String className) {
		if (project == null) {
			return "";
		}
		DexForgeClass cls = project.searchClassByAlias(className);
		return cls != null ? cls.getCode() : "";
	}

	@Override
	public void close() {
		if (project != null) {
			project.close();
			project = null;
		}
	}
}
