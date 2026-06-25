package dexforge.engine.jadx.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import dexforge.domain.model.project.Project;
import dexforge.engine.*;
import dexforge.engine.jadx.utils.JadxIntegrityUtils;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;

public final class JadxProjectSession implements DexForgeProjectSession {
	private final JadxDecompiler decompiler;
	private final Project project;

	public JadxProjectSession(JadxDecompiler decompiler, Project project) {
		this.decompiler = Objects.requireNonNull(decompiler);
		this.project = Objects.requireNonNull(project);
	}

	@Override
	public Project getProject() {
		return project;
	}

	@Override
	public int getClassesCount() {
		return decompiler.getClasses().size();
	}

	@Override
	public int getResourcesCount() {
		return decompiler.getResources().size();
	}

	@Override
	public List<DexForgeClassInfo> listClasses() {
		return decompiler.getClasses().stream()
				.map(cls -> new DexForgeClassInfo(
						cls.getFullName(),
						cls.getName(),
						cls.getClassNode().getAlias(),
						cls.getPackage()))
				.collect(Collectors.toList());
	}

	@Override
	public DexForgeClassDecompileResult decompileClass(String className) {
		JavaClass cls = decompiler.searchJavaClassByAliasFullName(className);
		if (cls == null) {
			throw new IllegalArgumentException("Class not found: " + className);
		}
		String code = cls.getCode();
		return new DexForgeClassDecompileResult(
				code,
				cls.getCodeInfo().getCodeMetadata().getLineMapping(),
				java.util.Collections.emptyList());
	}

	@Override
	public DexForgeDefinitionInfo getDefinition(String className, int position) {
		return null; // TODO: Implement
	}

	@Override
	public DexForgeSourceLocation findDefinition(String uri, int line, int character) {
		return null; // TODO: Implement
	}

	@Override
	public List<DexForgeSourceLocation> findReferences(String uri, int line, int character) {
		return java.util.Collections.emptyList();
	}

	@Override
	public List<DexForgeWorkspaceSymbol> findWorkspaceSymbols(String query, int limit) {
		return java.util.Collections.emptyList();
	}

	@Override
	public DexForgeHover getHover(String uri, int line, int character) {
		return null;
	}

	@Override
	public void decompileProject(java.nio.file.Path outputPath, DexForgeProgressReporter progressReporter) {
		decompiler.getArgs().setOutDir(outputPath.toFile());
		decompiler.save(500, progressReporter::onProgress);
	}

	@Override
	public List<DexForgeDiagnostic> getDiagnostics() {
		return java.util.Collections.emptyList();
	}

	@Override
	public Map<String, String> calculateFingerprint() {
		return JadxIntegrityUtils.calculateFingerprint(decompiler);
	}

	@Override
	public void close() {
		decompiler.close();
	}
}
