package dexforge.api.model.metadata;

/**
 * A reference to a semantic node within the decompiled code.
 */
public interface DexForgeNodeRef extends DexForgeAnnotation {

	/**
	 * Get the unique identifier of the referenced node.
	 */
	String getNodeId();

	/**
	 * Get the definition position (offset) of this node if it's a declaration.
	 */
	int getDefPosition();

	void setDefPosition(int pos);
}
