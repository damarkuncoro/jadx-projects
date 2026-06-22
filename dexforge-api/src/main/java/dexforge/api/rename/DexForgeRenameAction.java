package dexforge.api.rename;

import java.time.Instant;
import dexforge.api.model.DexForgeNode;

/**
 * Represents a single renaming operation that can be tracked, persisted, or undone.
 */
public final class DexForgeRenameAction {
	private final String nodeId;
	private final String oldName;
	private final String newName;
	private final Instant timestamp;

	public DexForgeRenameAction(String nodeId, String oldName, String newName) {
		this.nodeId = nodeId;
		this.oldName = oldName;
		this.newName = newName;
		this.timestamp = Instant.now();
	}

	public String getNodeId() { return nodeId; }
	public String getOldName() { return oldName; }
	public String getNewName() { return newName; }
	public Instant getTimestamp() { return timestamp; }

	@Override
	public String toString() {
		return String.format("[%s] Rename %s: %s -> %s", timestamp, nodeId, oldName, newName);
	}
}
