package dexforge.api.persistence;

import java.io.File;
import java.io.IOException;

/**
 * Interface for saving and loading project state.
 */
public interface DexForgeProjectStore {

	/**
	 * Saves the project state to the specified file.
	 */
	void save(DexForgeProjectState state, File file) throws IOException;

	/**
	 * Loads the project state from the specified file.
	 */
	DexForgeProjectState load(File file) throws IOException;
}
