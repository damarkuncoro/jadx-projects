package dexforge.api.analysis;

import java.util.List;
import dexforge.api.core.DexForgeProject;

/**
 * Interface for automated analysis components.
 */
public interface DexForgeAnalyzer {
	String getName();
	String getDescription();
	List<DexForgeFinding> analyze(DexForgeProject project);
}
