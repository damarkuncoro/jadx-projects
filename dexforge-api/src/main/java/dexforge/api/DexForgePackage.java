package dexforge.api;

import java.util.List;
import java.util.Objects;

import jadx.api.JavaPackage;

public final class DexForgePackage implements DexForgeNode, Comparable<DexForgePackage> {
	private final JavaPackage delegate;

	DexForgePackage(JavaPackage delegate) {
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

	public String getRawFullName() {
		return delegate.getRawFullName();
	}

	public List<DexForgePackage> getSubPackages() {
		return DexForgeNodeFactory.wrapPackages(delegate.getSubPackages());
	}

	public List<DexForgeClass> getClasses() {
		return DexForgeNodeFactory.wrapClasses(delegate.getClasses());
	}

	public List<DexForgeClass> getClassesNoDup() {
		return DexForgeNodeFactory.wrapClasses(delegate.getClassesNoDup());
	}

	public boolean isRoot() {
		return delegate.isRoot();
	}

	public boolean isLeaf() {
		return delegate.isLeaf();
	}

	public boolean isDefault() {
		return delegate.isDefault();
	}

	public void rename(String alias) {
		delegate.rename(alias);
	}

	public boolean isParentRenamed() {
		return delegate.isParentRenamed();
	}

	public boolean isDescendantOf(DexForgePackage ancestor) {
		return delegate.isDescendantOf(ancestor.delegate);
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
	public JavaPackage unwrap() {
		return delegate;
	}

	@Override
	public int compareTo(DexForgePackage other) {
		return delegate.compareTo(other.delegate);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
