package dexforge.api.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.api.engine.DexForgeEngine;

/**
 * DexForge implementation of a field node, wrapping an internal delegate.
 */
public final class DexForgeField implements DexForgeNode {
	private final Object delegate;
	private final DexForgeEngine engine;

	public DexForgeField(Object delegate, DexForgeEngine engine) {
		this.delegate = Objects.requireNonNull(delegate);
		this.engine = Objects.requireNonNull(engine);
	}

	@Override
	public String getName() {
		return engine.getName(delegate);
	}

	@Override
	public String getFullName() {
		return engine.getFullName(delegate);
	}

	public String getType() {
		return engine.getFieldType(delegate);
	}

	@Override
	public DexForgeClass getDeclaringClass() {
		return null;
	}

	@Override
	public DexForgeClass getTopParentClass() {
		return null;
	}

	@Override
	public int getDefinitionPosition() {
		return engine.getDefinitionPosition(delegate);
	}

	@Override
	public List<DexForgeNode> getUseIn() {
		return DexForgeNodeFactory.wrapNodes(engine.getUseIn(delegate), engine);
	}

	@Override
	public boolean isDecompiled() {
		return true;
	}

	@Override
	public void rename(String newName) {
		engine.rename(delegate, newName);
	}

	@Override
	public void removeAlias() {
		engine.removeAlias(delegate);
	}

	@Override
	public DexForgeNodeType getNodeType() {
		return DexForgeNodeType.FIELD;
	}

	@Override
	public String getId() {
		return "fld:" + getFullName();
	}
}
