package dexforge.api.rename;

import java.util.List;
import dexforge.api.model.DexForgeNode;

/**
 * Manages semantic renaming across the project with history support.
 * Superior to raw alias setting in JADX.
 */
public interface DexForgeRenameManager {
	/**
	 * Rename a node and record the action.
	 */
	void rename(DexForgeNode node, String newName);

	/**
	 * Undo the last rename action.
	 */
	boolean undo();

	/**
	 * Redo the last undone action.
	 */
	boolean redo();

	/**
	 * Get the full history of rename actions.
	 */
	List<DexForgeRenameAction> getHistory();

	/**
	 * Load a rename history into the manager.
	 */
	void loadHistory(List<DexForgeRenameAction> history);

	/**
	 * Clear all history and persist current aliases as "original".
	 */
	void resetHistory();
}
