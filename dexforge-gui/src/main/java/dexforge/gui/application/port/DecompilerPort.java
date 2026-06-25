package dexforge.gui.application.port;

import dexforge.api.analysis.DexForgeFinding;
import dexforge.api.model.DexForgeApkMetadata;
import dexforge.api.model.DexForgeNode;
import dexforge.api.model.DexForgePackage;
import dexforge.api.resource.DexForgeResourceFile;
import dexforge.api.intelligence.IProjectIntelligence;
import dexforge.api.ui.IUiEditor;
import dexforge.gui.domain.model.GuiProject;

import java.util.List;

/**
 * Port for interacting with the decompiler engine.
 * Defined in Application layer, implemented in Infrastructure.
 */
public interface DecompilerPort {
	void open(GuiProject project, String engineId);

	List<String> getClasses();

	List<DexForgePackage> getRootPackages();

	List<DexForgeResourceFile> getResources();

	String getCode(String className);

	String getSmali(String className);

	List<DexForgeFinding> runAnalysis();

	List<DexForgeNode> search(String query);

	DexForgeApkMetadata getApkMetadata();

    IProjectIntelligence getIntelligence();

    IUiEditor getUiEditor(Object layoutRoot);

	void close();
}
