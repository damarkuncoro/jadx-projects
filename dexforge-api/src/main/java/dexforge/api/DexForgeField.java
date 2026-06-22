package dexforge.api;

import java.util.List;
import java.util.Objects;

import jadx.api.JavaField;

public final class DexForgeField implements DexForgeNode {
	private final JavaField delegate;

	DexForgeField(JavaField delegate) {
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

	public String getRawName() {
		return delegate.getRawName();
	}

	public String getType() {
		return delegate.getType().toString();
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
	public JavaField unwrap() {
		return delegate;
	}

	JavaField delegate() {
		return delegate;
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
