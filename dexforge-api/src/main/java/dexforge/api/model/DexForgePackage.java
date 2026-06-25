package dexforge.api.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.api.engine.DexForgeEngine;

public final class DexForgePackage implements DexForgeNode, Comparable<DexForgePackage> {
	private final Object delegate;
	private final DexForgeEngine engine;

	public DexForgePackage(Object delegate, DexForgeEngine engine) {
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

	public List<DexForgePackage> getSubPackages() {
		return DexForgeNodeFactory.wrapPackages(engine.getSubPackages(delegate), engine);
	}

	public List<DexForgeClass> getClasses() {
		return DexForgeNodeFactory.wrapClasses(engine.getClassesInPackage(delegate), engine);
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
		return 0;
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
		return DexForgeNodeType.PACKAGE;
	}

	@Override
	public String getId() {
		return "pkg:" + getFullName();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(DexForgePackage other) {
		return getFullName().compareTo(other.getFullName());
	}
}
