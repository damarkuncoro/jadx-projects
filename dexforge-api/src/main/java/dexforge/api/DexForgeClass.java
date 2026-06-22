package dexforge.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jadx.api.ICodeInfo;
import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.JadxError;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.MethodNode;

public final class DexForgeClass implements DexForgeNode {
	private final JavaClass delegate;

	DexForgeClass(JavaClass delegate) {
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

	public String getPackageName() {
		return delegate.getPackage();
	}

	public String getCode() {
		return getCodeInfo().getCode();
	}

	public DexForgeCodeInfo getCodeInfo() {
		try {
			return new DexForgeCodeInfo(delegate.getCodeInfo());
		} catch (RuntimeException e) {
			throw new DexForgeException("CLASS_DECOMPILE_FAILED", "Failed to decompile class: " + getFullName(), e);
		}
	}

	public DexForgeCodeInfo reload() {
		ICodeInfo codeInfo = delegate.reload();
		return new DexForgeCodeInfo(codeInfo);
	}

	public void unload() {
		delegate.unload();
	}

	public boolean isNoCode() {
		return delegate.isNoCode();
	}

	public boolean isInner() {
		return delegate.isInner();
	}

	public String getSmali() {
		return delegate.getSmali();
	}

	public String getRawName() {
		return delegate.getRawName();
	}

	public List<DexForgeClass> getInnerClasses() {
		return DexForgeNodeFactory.wrapClasses(delegate.getInnerClasses());
	}

	public List<DexForgeClass> getInlinedClasses() {
		return DexForgeNodeFactory.wrapClasses(delegate.getInlinedClasses());
	}

	public List<DexForgeField> getFields() {
		return DexForgeNodeFactory.wrapFields(delegate.getFields());
	}

	public List<DexForgeMethod> getMethods() {
		return DexForgeNodeFactory.wrapMethods(delegate.getMethods());
	}

	public DexForgeMethod searchMethodByShortId(String shortId) {
		JavaMethod method = delegate.searchMethodByShortId(shortId);
		return method == null ? null : new DexForgeMethod(method);
	}

	public List<DexForgeClass> getDependencies() {
		return DexForgeNodeFactory.wrapClasses(delegate.getDependencies());
	}

	public int getTotalDependenciesCount() {
		return delegate.getTotalDepsCount();
	}

	public Integer getSourceLine(int decompiledLine) {
		return delegate.getSourceLine(decompiledLine);
	}

	@Override
	public DexForgeClass getDeclaringClass() {
		JavaClass cls = delegate.getDeclaringClass();
		return cls == null ? null : new DexForgeClass(cls);
	}

	@Override
	public DexForgeClass getTopParentClass() {
		return new DexForgeClass(delegate.getTopParentClass());
	}

	public DexForgeClass getOriginalTopParentClass() {
		return new DexForgeClass(delegate.getOriginalTopParentClass());
	}

	public DexForgeClass getCodeParent() {
		JavaClass cls = delegate.getCodeParent();
		return cls == null ? null : new DexForgeClass(cls);
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

	public List<Integer> getUsePlacesFor(DexForgeCodeInfo codeInfo, DexForgeNode node) {
		if (!(node instanceof DexForgeClass) && !(node instanceof DexForgeMethod) && !(node instanceof DexForgeField)) {
			return Collections.emptyList();
		}
		JavaNode javaNode;
		if (node instanceof DexForgeClass) {
			javaNode = ((DexForgeClass) node).delegate();
		} else if (node instanceof DexForgeMethod) {
			javaNode = ((DexForgeMethod) node).delegate();
		} else {
			javaNode = ((DexForgeField) node).delegate();
		}
		return delegate.getUsePlacesFor(codeInfo.delegate(), javaNode);
	}

	public List<DexForgeDiagnostic> getDiagnostics() {
		List<DexForgeDiagnostic> diagnostics = new ArrayList<>();
		collectDiagnostics(delegate.getClassNode(), diagnostics);
		return Collections.unmodifiableList(diagnostics);
	}

	/**
	 * JADX bridge kept for compatibility during migration.
	 * New code should use DexForge API methods instead of unwrapping.
	 */
	@Deprecated(forRemoval = false)
	public JavaClass unwrap() {
		return delegate;
	}

	JavaClass delegate() {
		return delegate;
	}

	private static void collectDiagnostics(ClassNode cls, List<DexForgeDiagnostic> diagnostics) {
		for (JadxError error : cls.getAll(AType.JADX_ERROR)) {
			diagnostics.add(DexForgeDiagnostic.error(error.getError(), cls.toString()));
		}
		for (MethodNode method : cls.getMethods()) {
			for (JadxError error : method.getAll(AType.JADX_ERROR)) {
				diagnostics.add(DexForgeDiagnostic.error(error.getError(), method.toString()));
			}
		}
		for (FieldNode field : cls.getFields()) {
			for (JadxError error : field.getAll(AType.JADX_ERROR)) {
				diagnostics.add(DexForgeDiagnostic.error(error.getError(), field.toString()));
			}
		}
		for (ClassNode innerClass : cls.getInnerClasses()) {
			collectDiagnostics(innerClass, diagnostics);
		}
	}
}
