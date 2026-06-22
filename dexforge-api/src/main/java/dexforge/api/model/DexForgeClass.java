package dexforge.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import dexforge.api.diagnostic.DexForgeDiagnostic;
import dexforge.api.exception.DexForgeException;
import dexforge.core.infrastructure.jadx.JadxDiagnosticMapper;
import dexforge.core.infrastructure.jadx.JadxNodeHelper;

/**
 * DexForge implementation of a class node, wrapping an internal delegate.
 */
public final class DexForgeClass implements DexForgeNode {
	private final Object delegate;

	public DexForgeClass(Object delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	public String getName() {
		return JadxNodeHelper.getClassName(delegate);
	}

	@Override
	public String getFullName() {
		return JadxNodeHelper.getClassFullName(delegate);
	}

	public String getPackageName() {
		return JadxNodeHelper.getClassPackage(delegate);
	}

	public String getCode() {
		return getCodeInfo().getCode();
	}

	public DexForgeCodeInfo getCodeInfo() {
		try {
			return new DexForgeCodeInfo(JadxNodeHelper.decompile(delegate));
		} catch (RuntimeException e) {
			throw new DexForgeException("CLASS_DECOMPILE_FAILED", "Failed to decompile class: " + getFullName(), e);
		}
	}

	public DexForgeCodeInfo reload() {
		return new DexForgeCodeInfo(JadxNodeHelper.reloadCode(delegate));
	}

	public void unload() {
		JadxNodeHelper.unloadCode(delegate);
	}

	public boolean isNoCode() {
		return JadxNodeHelper.isClassNoCode(delegate);
	}

	public boolean isInner() {
		return JadxNodeHelper.isClassInner(delegate);
	}

	public String getSmali() {
		return JadxNodeHelper.getClassSmali(delegate);
	}

	public String getRawName() {
		return JadxNodeHelper.getClassRawName(delegate);
	}

	public List<DexForgeClass> getInnerClasses() {
		return DexForgeNodeFactory.wrapClasses(JadxNodeHelper.getInnerClasses(delegate));
	}

	public List<DexForgeClass> getInlinedClasses() {
		return DexForgeNodeFactory.wrapClasses(JadxNodeHelper.getInlinedClasses(delegate));
	}

	public List<DexForgeField> getFields() {
		return DexForgeNodeFactory.wrapFields(JadxNodeHelper.getFields(delegate));
	}

	public List<DexForgeMethod> getMethods() {
		return DexForgeNodeFactory.wrapMethods(JadxNodeHelper.getMethods(delegate));
	}

	public DexForgeMethod searchMethodByShortId(String shortId) {
		return DexForgeNodeFactory.wrapMethod(JadxNodeHelper.searchMethodByShortId(delegate, shortId));
	}

	public List<DexForgeClass> getDependencies() {
		return DexForgeNodeFactory.wrapClasses(JadxNodeHelper.getDependencies(delegate));
	}

	public int getTotalDependenciesCount() {
		return JadxNodeHelper.getTotalDepsCount(delegate);
	}

	public Optional<Integer> getSourceLine(int decompiledLine) {
		return getCodeInfo().getSourceLine(decompiledLine);
	}

	@Override
	public DexForgeClass getDeclaringClass() {
		return DexForgeNodeFactory.wrapClass(JadxNodeHelper.getParentClass(delegate));
	}

	@Override
	public DexForgeClass getTopParentClass() {
		return DexForgeNodeFactory.wrapClass(JadxNodeHelper.getTopParentClass(delegate));
	}

	public DexForgeClass getOriginalTopParentClass() {
		return getTopParentClass();
	}

	public DexForgeClass getCodeParent() {
		return DexForgeNodeFactory.wrapClass(JadxNodeHelper.getParentClass(delegate));
	}

	@Override
	public int getDefinitionPosition() {
		return JadxNodeHelper.getDefPos(delegate);
	}

	@Override
	public List<DexForgeNode> getUseIn() {
		return DexForgeNodeFactory.wrapNodes(JadxNodeHelper.getClassUseIn(delegate));
	}

	@Override
	public boolean isDecompiled() {
		return JadxNodeHelper.isClassDecompiled(delegate);
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
		return "cls:" + getFullName();
	}

	public List<Integer> getUsePlacesFor(DexForgeCodeInfo codeInfo, DexForgeNode node) {
		return codeInfo.getUsePlacesFor(node);
	}

	public List<DexForgeDiagnostic> getDiagnostics() {
		List<dexforge.engine.DexForgeDiagnostic> coreDiagnostics = JadxDiagnosticMapper.collectDiagnostics(delegate, getCode());
		if (coreDiagnostics.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeDiagnostic> apiDiagnostics = new ArrayList<>(coreDiagnostics.size());
		for (dexforge.engine.DexForgeDiagnostic diag : coreDiagnostics) {
			apiDiagnostics.add(mapToApi(diag));
		}
		return Collections.unmodifiableList(apiDiagnostics);
	}

	private static DexForgeDiagnostic mapToApi(dexforge.engine.DexForgeDiagnostic diag) {
		switch (diag.getSeverity()) {
			case INFO:
				return DexForgeDiagnostic.info(diag.getMessage(), diag.getSource());
			case WARNING:
				return DexForgeDiagnostic.warning(diag.getMessage(), diag.getSource());
			case ERROR:
			default:
				return DexForgeDiagnostic.error(diag.getMessage(), diag.getSource());
		}
	}

	/**
	 * JADX bridge kept for compatibility during migration.
	 */
	@Deprecated(forRemoval = false)
	public Object unwrap() {
		return delegate;
	}

	public Object delegate() {
		return delegate;
	}
}
