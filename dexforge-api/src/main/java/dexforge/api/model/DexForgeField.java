package dexforge.api.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.core.infrastructure.jadx.JadxNodeHelper;

/**
 * DexForge implementation of a field node, wrapping an internal delegate.
 */
public final class DexForgeField implements DexForgeNode {
	private final Object delegate;

	public DexForgeField(Object delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	public String getName() {
		return JadxNodeHelper.getFieldAlias(delegate);
	}

	@Override
	public String getFullName() {
		return JadxNodeHelper.getFieldFullName(delegate);
	}

	public String getRawName() {
		return JadxNodeHelper.getFieldRawName(delegate);
	}

	public String getType() {
		return JadxNodeHelper.getFieldType(delegate);
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
		return DexForgeNodeFactory.wrapNodes(JadxNodeHelper.getFieldUseIn(delegate));
	}

	@Override
	public boolean isDecompiled() {
		return true; // Fields don't have separate decompilation state usually
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
		return "fld:" + getFullName();
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
