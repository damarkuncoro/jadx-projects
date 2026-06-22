package dexforge.api.diagnostic;

import java.util.List;
import dexforge.api.model.DexForgeNode;

/**
 * Manages project-wide diagnostics and analysis results.
 */
public interface DexForgeDiagnosticManager {
	/**
	 * Get all diagnostics for the entire project.
	 */
	List<DexForgeDiagnostic> getAll();

	/**
	 * Get diagnostics related to a specific node.
	 */
	List<DexForgeDiagnostic> getForNode(DexForgeNode node);

	/**
	 * Filter diagnostics by severity.
	 */
	List<DexForgeDiagnostic> getBySeverity(DexForgeDiagnosticSeverity severity);

	/**
	 * Total count of errors in the project.
	 */
	int getErrorCount();

	/**
	 * Total count of warnings in the project.
	 */
	int getWarningCount();
}
