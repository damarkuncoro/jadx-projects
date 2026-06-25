package dexforge.gui.infrastructure.adapter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import dexforge.api.analysis.DexForgeFinding;
import dexforge.api.core.DexForgeDecompiler;
import dexforge.api.core.DexForgeProject;
import dexforge.api.model.DexForgeApkMetadata;
import dexforge.api.model.DexForgeClass;
import dexforge.api.model.DexForgeNode;
import dexforge.api.model.DexForgePackage;
import dexforge.api.resource.DexForgeResourceFile;
import dexforge.api.intelligence.IProjectIntelligence;
import dexforge.api.ui.IUiEditor;
import dexforge.gui.application.port.DecompilerPort;
import dexforge.gui.domain.model.GuiProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter implementing DecompilerPort using DexForge API.
 * This is the ONLY place that knows about dexforge.api.
 * It must NOT import jadx.*.
 */
public final class DexForgeApiAdapter implements DecompilerPort {
	private static final Logger LOG = LoggerFactory.getLogger(DexForgeApiAdapter.class);
	private DexForgeProject project;

	@Override
	public void open(GuiProject guiProject, String engineId) {
		LOG.info("Opening project: {} with engine: {}", guiProject.getInputFile().getAbsolutePath(), engineId);
		if (project != null) {
			project.close();
		}
		try {
			project = DexForgeDecompiler.builder()
					.inputFile(guiProject.getInputFile())
					.engine(engineId)
					.build();
			project.load();
			LOG.info("Project opened successfully. Classes: {}, Resources: {}",
					project.getClasses().size(), project.getResources().size());
		} catch (Exception e) {
			LOG.error("Failed to open project: {}", e.getMessage(), e);
			throw e;
		}
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
	public List<DexForgePackage> getRootPackages() {
		if (project == null) {
			return Collections.emptyList();
		}
		return project.getPackages();
	}

	@Override
	public List<DexForgeResourceFile> getResources() {
		if (project == null) {
			return Collections.emptyList();
		}
		return project.getResources();
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
	public String getSmali(String className) {
		if (project == null) {
			return "";
		}
		DexForgeClass cls = project.searchClassByAlias(className);
		return cls != null ? cls.getSmali() : "";
	}

	@Override
	public List<DexForgeFinding> runAnalysis() {
		if (project == null) {
			return Collections.emptyList();
		}
		return project.runAnalysis();
	}

	@Override
	public List<DexForgeNode> search(String query) {
		if (project == null) {
			return Collections.emptyList();
		}
		return project.search().global().containing(query).findAll();
	}

	@Override
	public DexForgeApkMetadata getApkMetadata() {
		if (project == null) {
			return null;
		}
		return project.getApkMetadata();
	}

	@Override
	public IProjectIntelligence getIntelligence() {
		return project != null ? project.getIntelligence() : null;
	}

	@Override
	public IUiEditor getUiEditor(Object layoutRoot) {
		return project != null ? project.getUiEditor(layoutRoot) : null;
	}

	@Override
	public void close() {
		if (project != null) {
			project.close();
			project = null;
		}
	}
}
