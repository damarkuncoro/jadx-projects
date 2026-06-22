package dexforge.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.api.model.insn.DexForgeInstruction;
import dexforge.core.infrastructure.jadx.JadxNodeHelper;

/**
 * DexForge implementation of a method node, wrapping an internal delegate.
 */
public final class DexForgeMethod implements DexForgeNode {
	private final Object delegate;
	private List<DexForgeInstruction> cachedInstructions;

	public DexForgeMethod(Object delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	/**
	 * Get the structured bytecode instructions for this method.
	 */
	public List<DexForgeInstruction> getInstructions() {
		if (cachedInstructions == null) {
			List<?> rawInsns = JadxNodeHelper.getMethodInstructions(delegate);
			if (rawInsns.isEmpty()) {
				cachedInstructions = Collections.emptyList();
			} else {
				List<DexForgeInstruction> list = new ArrayList<>(rawInsns.size());
				for (Object insn : rawInsns) {
					list.add(new DexForgeInstructionImpl(insn));
				}
				cachedInstructions = Collections.unmodifiableList(list);
			}
		}
		return cachedInstructions;
	}

	@Override
	public String getName() {
		return JadxNodeHelper.getMethodAlias(delegate);
	}

	@Override
	public String getFullName() {
		return JadxNodeHelper.getMethodFullName(delegate);
	}

	public List<String> getArguments() {
		List<?> args = JadxNodeHelper.getMethodArgs(delegate);
		if (args.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>(args.size());
		for (Object arg : args) {
			result.add(arg.toString());
		}
		return Collections.unmodifiableList(result);
	}

	public String getReturnType() {
		return JadxNodeHelper.getMethodReturnType(delegate);
	}

	public String getCode() {
		return JadxNodeHelper.getMethodCode(delegate);
	}

	public boolean isConstructor() {
		return JadxNodeHelper.isConstructor(delegate);
	}

	public boolean isClassInitializer() {
		return JadxNodeHelper.isClassInit(delegate);
	}

	public boolean callsSelf() {
		return JadxNodeHelper.getMethodCallsSelf(delegate);
	}

	public List<DexForgeNode> getUsed() {
		return DexForgeNodeFactory.wrapNodes(JadxNodeHelper.getMethodUsed(delegate));
	}

	public List<DexForgeMethod> getOverrideRelatedMethods() {
		return DexForgeNodeFactory.wrapMethods(JadxNodeHelper.getMethodOverrideRelated(delegate));
	}

	@Override
	public DexForgeClass getDeclaringClass() {
		return DexForgeNodeFactory.wrapClass(JadxNodeHelper.getParentClass(delegate));
	}

	@Override
	public DexForgeClass getTopParentClass() {
		return DexForgeNodeFactory.wrapClass(JadxNodeHelper.getTopParentClass(delegate));
	}

	@Override
	public int getDefinitionPosition() {
		return JadxNodeHelper.getDefPos(delegate);
	}

	@Override
	public List<DexForgeNode> getUseIn() {
		return DexForgeNodeFactory.wrapNodes(JadxNodeHelper.getMethodUseIn(delegate));
	}

	@Override
	public boolean isDecompiled() {
		return JadxNodeHelper.isMethodDecompiled(delegate);
	}

	@Override
	public void removeAlias() {
		JadxNodeHelper.removeAlias(delegate);
	}

	@Override
	public void rename(String newName) {
		JadxNodeHelper.rename(delegate, newName);
	}

	@Override
	public String getId() {
		return "mth:" + getFullName();
	}

	/**
	 * JADX bridge kept for compatibility during migration.
	 */
	@Deprecated(forRemoval = false)
	public Object unwrap() {
		return JadxNodeHelper.getJavaNode(delegate);
	}

	public Object delegate() {
		return delegate;
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
