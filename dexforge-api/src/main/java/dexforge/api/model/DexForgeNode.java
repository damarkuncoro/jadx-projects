package dexforge.api.model;

import java.util.List;

public interface DexForgeNode {
	String getName();

	String getFullName();

	DexForgeClass getDeclaringClass();

	DexForgeClass getTopParentClass();

	int getDefinitionPosition();

	List<DexForgeNode> getUseIn();

	/**
	 * Check if this node has been decompiled and has code available.
	 */
	boolean isDecompiled();

	void removeAlias();

	/**
	 * Get the type of this node.
	 */
	DexForgeNodeType getNodeType();

	/**
	 * Unique identifier for this node within the project.
	 */
	String getId();

	/**
	 * Rename this node with a new alias.
	 */
	void rename(String newName);
}
