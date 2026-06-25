package dexforge.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import dexforge.api.diagnostic.DexForgeDiagnostic;
import dexforge.api.engine.DexForgeEngine;
import dexforge.api.exception.DexForgeException;

/**
 * DexForge implementation of a class node, wrapping an internal delegate.
 */
public final class DexForgeClass implements DexForgeNode {
	private final Object delegate;
	private final DexForgeEngine engine;

	public DexForgeClass(Object delegate, DexForgeEngine engine) {
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

	public String getPackageName() {
		String fullName = getFullName();
		int lastDot = fullName.lastIndexOf('.');
		return lastDot == -1 ? "" : fullName.substring(0, lastDot);
	}

	public String getCode() {
		return engine.getCode(delegate);
	}

	public DexForgeCodeInfo getCodeInfo() {
		return new DexForgeCodeInfo(delegate, engine);
	}

	public String getSmali() {
		return engine.getSmali(delegate);
	}

	@Override
	public DexForgeClass getDeclaringClass() {
		return null;
	}

	@Override
	public DexForgeClass getTopParentClass() {
		return this;
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
		return !getCode().isEmpty();
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
		return DexForgeNodeType.CLASS;
	}

	@Override
	public String getId() {
		return "cls:" + getFullName();
	}

	public List<DexForgeDiagnostic> getDiagnostics() {
		return Collections.emptyList();
	}

	public List<DexForgeMethod> getMethods() {
		return DexForgeNodeFactory.wrapMethods(engine.getMethods(delegate), engine);
	}

	public List<DexForgeField> getFields() {
		return DexForgeNodeFactory.wrapFields(engine.getFields(delegate), engine);
	}

	public List<DexForgeClass> getInnerClasses() {
		return DexForgeNodeFactory.wrapClasses(engine.getInnerClasses(delegate), engine);
	}

	public DexForgeNode getNodeAt(int position) {
		Object rawNode = engine.getNodeAt(delegate, position);
		return DexForgeNodeFactory.wrap(rawNode, engine);
	}

	public String getSuperClass() {
		return engine.getSuperClass(delegate);
	}

	public List<String> getInterfaces() {
		return engine.getInterfaces(delegate);
	}

	public Object delegate() {
		return delegate;
	}

	@Override
	public String toString() {
		return getName();
	}
}
