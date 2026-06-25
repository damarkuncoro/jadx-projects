package dexforge.api.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Value Object representing the integrity of the project's input files.
 */
public final class DexForgeProjectFingerprint {
	private final Map<String, String> fileHashes; // Map of FileName -> SHA-256 Hash

	public DexForgeProjectFingerprint(Map<String, String> fileHashes) {
		this.fileHashes = Collections.unmodifiableMap(Objects.requireNonNull(fileHashes));
	}

	public Map<String, String> getFileHashes() {
		return fileHashes;
	}

	public boolean matches(DexForgeProjectFingerprint other) {
		if (other == null) return false;
		return this.fileHashes.equals(other.fileHashes);
	}

	@Override
	public String toString() {
		return "DexForgeProjectFingerprint{filesCount=" + fileHashes.size() + "}";
	}
}
