package dexforge.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jadx.api.JavaMethod;

public final class DexForgeMethod implements DexForgeNode {
	private final JavaMethod delegate;

	DexForgeMethod(JavaMethod delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public String getFullName() {
		return delegate.getFullName();
	}

	public List<String> getArguments() {
		if (delegate.getArguments().isEmpty()) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>(delegate.getArguments().size());
		for (Object arg : delegate.getArguments()) {
			result.add(arg.toString());
		}
		return Collections.unmodifiableList(result);
	}

	public String getReturnType() {
		return delegate.getReturnType().toString();
	}

	public String getCode() {
		return delegate.getCodeStr();
	}

	public boolean isConstructor() {
		return delegate.isConstructor();
	}

	public boolean isClassInitializer() {
		return delegate.isClassInit();
	}

	public boolean callsSelf() {
		return delegate.callsSelf();
	}

	public List<DexForgeNode> getUsed() {
		return DexForgeNodeFactory.wrapNodes(delegate.getUsed());
	}

	public List<DexForgeMethod> getOverrideRelatedMethods() {
		return DexForgeNodeFactory.wrapMethods(delegate.getOverrideRelatedMethods());
	}

	@Override
	public DexForgeClass getDeclaringClass() {
		return new DexForgeClass(delegate.getDeclaringClass());
	}

	@Override
	public DexForgeClass getTopParentClass() {
		return new DexForgeClass(delegate.getTopParentClass());
	}

	@Override
	public int getDefinitionPosition() {
		return delegate.getDefPos();
	}

	@Override
	public List<DexForgeNode> getUseIn() {
		return DexForgeNodeFactory.wrapNodes(delegate.getUseIn());
	}

	@Override
	public void removeAlias() {
		delegate.removeAlias();
	}

	/**
	 * JADX bridge kept for compatibility during migration.
	 * New code should use DexForge API methods instead of unwrapping.
	 */
	@Deprecated(forRemoval = false)
	public JavaMethod unwrap() {
		return delegate;
	}

	JavaMethod delegate() {
		return delegate;
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
