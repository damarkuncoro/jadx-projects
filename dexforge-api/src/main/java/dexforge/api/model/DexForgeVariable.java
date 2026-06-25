package dexforge.api.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.api.engine.DexForgeEngine;

/**
 * Representation of a local variable within a method.
 */
public final class DexForgeVariable implements DexForgeNode {
	private final Object delegate;
	private final DexForgeEngine engine;
	private final DexForgeMethod declaringMethod;

	public DexForgeVariable(Object delegate, DexForgeEngine engine, DexForgeMethod method) {
		this.delegate = Objects.requireNonNull(delegate);
		this.engine = Objects.requireNonNull(engine);
		this.declaringMethod = Objects.requireNonNull(method);
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
		// Variable type name
		return "Object"; // Placeholder
	}

	public DexForgeMethod getMethod() {
		return declaringMethod;
	}

	@Override
	public DexForgeClass getDeclaringClass() {
		return declaringMethod.getDeclaringClass();
	}

	@Override
	public DexForgeClass getTopParentClass() {
		return declaringMethod.getTopParentClass();
	}

	@Override
	public int getDefinitionPosition() {
		return engine.getDefinitionPosition(delegate);
	}

	@Override
	public List<DexForgeNode> getUseIn() {
		return Collections.singletonList(declaringMethod);
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
		return DexForgeNodeType.VARIABLE;
	}

	@Override
	public String getId() {
		return "var:" + getFullName();
	}

	public Object unwrap() {
		return delegate;
	}
}
