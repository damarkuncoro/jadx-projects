package dexforge.api.model;

import java.util.List;
import java.util.Objects;

import dexforge.core.infrastructure.jadx.JadxPackageHelper;

public final class DexForgePackage implements DexForgeNode, Comparable<DexForgePackage> {
	private final Object delegate;

	public DexForgePackage(Object delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	public String getName() {
		return JadxPackageHelper.getName(delegate);
	}

	@Override
	public String getFullName() {
		return JadxPackageHelper.getFullName(delegate);
	}

	public String getRawName() {
		return JadxPackageHelper.getRawName(delegate);
	}

	public String getRawFullName() {
		return JadxPackageHelper.getRawFullName(delegate);
	}

	public List<DexForgePackage> getSubPackages() {
		return DexForgeNodeFactory.wrapPackages(JadxPackageHelper.getSubPackages(delegate));
	}

	public List<DexForgeClass> getClasses() {
		return DexForgeNodeFactory.wrapClasses(JadxPackageHelper.getClasses(delegate));
	}

	public List<DexForgeClass> getClassesNoDup() {
		return DexForgeNodeFactory.wrapClasses(JadxPackageHelper.getClassesNoDup(delegate));
	}

	public boolean isRoot() {
		return JadxPackageHelper.isRoot(delegate);
	}

	public boolean isLeaf() {
		return JadxPackageHelper.isLeaf(delegate);
	}

	public boolean isDefault() {
		return JadxPackageHelper.isDefault(delegate);
	}

	public void rename(String newName) {
		JadxPackageHelper.rename(delegate, newName);
	}

	public boolean isParentRenamed() {
		return JadxPackageHelper.isParentRenamed(delegate);
	}

	public boolean isDescendantOf(DexForgePackage ancestor) {
		return JadxPackageHelper.isDescendantOf(delegate, ancestor.delegate);
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
		return JadxPackageHelper.getDefPos(delegate);
	}

	@Override
	public List<DexForgeNode> getUseIn() {
		return DexForgeNodeFactory.wrapNodes(JadxPackageHelper.getUseIn(delegate));
	}

	@Override
	public boolean isDecompiled() {
		return true; // Packages are just structural
	}

	@Override
	public void removeAlias() {
		JadxPackageHelper.removeAlias(delegate);
	}

	@Override
	public String getId() {
		return "pkg:" + getFullName();
	}

	/**
	 * bridge kept for internal use.
	 */
	@Deprecated(forRemoval = false)
	public Object unwrap() {
		return delegate;
	}

	@Override
	public int compareTo(DexForgePackage other) {
		return JadxPackageHelper.compare(delegate, other.delegate);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
