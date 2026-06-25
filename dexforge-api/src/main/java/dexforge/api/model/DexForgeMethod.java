package dexforge.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.api.engine.DexForgeEngine;
import dexforge.api.model.insn.DexForgeInstruction;

/**
 * DexForge implementation of a method node, wrapping an internal delegate.
 */
public final class DexForgeMethod implements DexForgeNode {
	private final Object delegate;
	private final DexForgeEngine engine;
	private List<DexForgeInstruction> cachedInstructions;

	public DexForgeMethod(Object delegate, DexForgeEngine engine) {
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

	public String getReturnType() {
		return engine.getReturnType(delegate);
	}

	public List<String> getArgumentTypes() {
		return engine.getArgumentTypes(delegate);
	}

	public String getCode() {
		return engine.getCode(delegate);
	}

	public List<DexForgeInstruction> getInstructions() {
		// Instructions are usually part of the class decompile in JADX
		// For now returning empty or we can add engine support for instruction stream
		return Collections.emptyList();
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
		return DexForgeNodeType.METHOD;
	}

	@Override
	public String getId() {
		return "mth:" + getFullName();
	}

	public boolean isConstructor() {
		return engine.isConstructor(delegate);
	}
}
