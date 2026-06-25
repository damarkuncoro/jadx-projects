package dexforge.api.core;

import java.util.List;

/**
 * Result of project integrity verification.
 */
public final class DexForgeIntegrityStatus {
	private final boolean valid;
	private final List<String> modifiedFiles;

	public DexForgeIntegrityStatus(boolean valid, List<String> modifiedFiles) {
		this.valid = valid;
		this.modifiedFiles = java.util.Collections.unmodifiableList(modifiedFiles);
	}

	public boolean isValid() {
		return valid;
	}

	public List<String> getModifiedFiles() {
		return modifiedFiles;
	}

	@Override
	public String toString() {
		return "DexForgeIntegrityStatus{valid=" + valid + ", modifiedFiles=" + modifiedFiles + "}";
	}
}
