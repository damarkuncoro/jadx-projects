package dexforge.api.rename;

import java.time.Instant;
import dexforge.api.model.DexForgeNode;
import dexforge.api.model.DexForgeNodeType;

/**
 * Represents a single renaming operation that can be tracked, persisted, or undone.
 */
public final class DexForgeRenameAction {
	private final String nodeId;
	private final DexForgeNodeType nodeType;
	private final String oldName;
	private final String newName;
	private final Instant timestamp;

	public DexForgeRenameAction(String nodeId, DexForgeNodeType nodeType, String oldName, String newName) {
		this.nodeId = nodeId;
		this.nodeType = nodeType;
		this.oldName = oldName;
		this.newName = newName;
		this.timestamp = Instant.now();
	}

	public DexForgeRenameAction(DexForgeNode node, String oldName, String newName) {
		this(node.getId(), node.getNodeType(), oldName, newName);
	}

	public String getNodeId() { return nodeId; }
	public DexForgeNodeType getNodeType() { return nodeType; }
	public String getOldName() { return oldName; }
	public String getNewName() { return newName; }
	public Instant getTimestamp() { return timestamp; }

	@Override
	public String toString() {
		return String.format("[%s] Rename %s (%s): %s -> %s", timestamp, nodeId, nodeType, oldName, newName);
	}
}
