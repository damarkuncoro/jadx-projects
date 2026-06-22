package dexforge.api;

import java.util.List;

public interface DexForgeNode {
	String getName();

	String getFullName();

	DexForgeClass getDeclaringClass();

	DexForgeClass getTopParentClass();

	int getDefinitionPosition();

	List<DexForgeNode> getUseIn();

	void removeAlias();
}
