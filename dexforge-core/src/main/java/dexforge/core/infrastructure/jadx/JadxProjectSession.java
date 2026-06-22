package dexforge.core.infrastructure.jadx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dexforge.domain.model.project.Project;
import dexforge.domain.model.project.ProjectModule;
import dexforge.engine.CodeCacheMode;
import dexforge.engine.UsageCacheMode;
import dexforge.engine.DexForgeClassDecompileResult;
import dexforge.engine.DexForgeClassInfo;
import dexforge.engine.DexForgeDefinitionInfo;
import dexforge.engine.DexForgeDecompilationSettings;
import dexforge.engine.DexForgeDiagnostic;
import dexforge.engine.DexForgeDiagnosticCategory;
import dexforge.engine.DexForgeDiagnosticSeverity;
import dexforge.engine.DexForgeHover;
import dexforge.engine.DexForgeOpenProjectRequest;
import dexforge.engine.DexForgeProgressReporter;
import dexforge.engine.DexForgeProjectIndex;
import dexforge.engine.DexForgeProjectSession;
import dexforge.engine.DexForgeSourceLocation;
import dexforge.engine.DexForgeSourcePosition;
import dexforge.engine.DexForgeSourceRange;
import dexforge.engine.DexForgeWorkspaceSymbol;
import dexforge.core.infrastructure.jadx.cache.code.CodeStringCache;
import dexforge.core.infrastructure.jadx.cache.code.disk.BufferCodeCache;
import dexforge.core.infrastructure.jadx.cache.code.disk.DiskCodeCache;
import dexforge.core.infrastructure.jadx.cache.usage.UsageInfoCache;
import dexforge.api.plugins.pass.JadxPassInfo;
import dexforge.api.plugins.pass.impl.SimpleJadxPassInfo;
import dexforge.api.plugins.pass.types.JadxPreparePass;

import jadx.api.CommentsLevel;
import jadx.api.DecompilationMode;
import jadx.api.ICodeInfo;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.api.ResourceFile;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.JadxError;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.ProcessState;
import jadx.core.utils.ErrorsCounter;

final class JadxProjectSession implements DexForgeProjectSession {
	private final JadxDecompiler decompiler;
	private final Project project;
	private final DexForgeProjectIndex index = new DexForgeProjectIndex();
	private volatile boolean indexingComplete = false;

	private JadxProjectSession(JadxDecompiler decompiler, Project project) {
		this.decompiler = decompiler;
		this.project = project;
	}

	static JadxProjectSession open(DexForgeOpenProjectRequest request) {
		return open(request, new JadxArgs());
	}

	static JadxProjectSession open(DexForgeOpenProjectRequest request, JadxArgs args) {
		args.getInputFiles().clear();
		args.getInputFiles().add(request.getInputPath().toFile());
		if (request.getDeobfuscationOn() != null) {
			args.setDeobfuscationOn(request.getDeobfuscationOn());
		}
		if (request.getCommentsLevel() != null) {
			args.setCommentsLevel(CommentsLevel.valueOf(request.getCommentsLevel().toUpperCase()));
		}
		if (request.getDecompilationMode() != null) {
			args.setDecompilationMode(DecompilationMode.valueOf(request.getDecompilationMode().toUpperCase()));
		}

		DexForgeDecompilationSettings settings = request.getSettings();
		if (settings != null) {
			args.setThreadsCount(settings.getThreadsCount());
			args.setTypeUpdatesLimitCount(settings.getTypeUpdatesLimit());
		} else {
			args.setThreadsCount(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
		}

		JadxDecompiler decompiler = new JadxDecompiler(args);
		if (settings != null) {
			setupCache(decompiler, args, settings);
		}
		if (request.getDecompilerConfigurator() != null) {
			request.getDecompilerConfigurator().accept(decompiler);
		}
		if (request.getProgressReporter() != null) {
			request.getProgressReporter().onProgress(0, 1);
		}
		decompiler.load();
		if (request.getProgressReporter() != null) {
			request.getProgressReporter().onProgress(1, 1);
		}

		String pathStr = request.getInputPath().toAbsolutePath().toString();
		String name = request.getInputPath().getFileName().toString();
		Project project = Project.create(pathStr, name, "DexForge JADX Project");
		project.open();

		String ext = "";
		int dotIdx = name.lastIndexOf('.');
		if (dotIdx != -1) {
			ext = name.substring(dotIdx + 1).toUpperCase();
		}
		long size = request.getInputPath().toFile().length();
		project.addModule(new ProjectModule(name, ext, pathStr, size));

		JadxProjectSession session = new JadxProjectSession(decompiler, project);
		session.startIndexing();
		return session;
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
		JavaClass cls = searchClass(className);
		if (cls == null) {
			throw new IllegalArgumentException("Class not found: " + className);
		}

		ICodeInfo codeInfo = cls.getCodeInfo();
		String code = codeInfo.getCodeStr();
		return new DexForgeClassDecompileResult(
				code,
				codeInfo.getCodeMetadata().getLineMapping(),
				JadxDiagnosticMapper.collectDiagnostics(cls.getClassNode(), code));
	}

	@Override
	public DexForgeDefinitionInfo getDefinition(String className, int position) {
		JavaClass cls = searchClass(className);
		if (cls == null) {
			throw new IllegalArgumentException("Class not found: " + className);
		}

		ICodeInfo codeInfo = cls.getCodeInfo();
		JavaNode node = decompiler.getJavaNodeAtPosition(codeInfo, position);
		if (node == null) {
			node = decompiler.getClosestJavaNode(codeInfo, position);
		}
		if (node == null) {
			throw new IllegalArgumentException("No symbol found at position " + position);
		}

		JavaClass declaringClass = node.getDeclaringClass();
		return new DexForgeDefinitionInfo(
				node.getName(),
				node.getFullName(),
				declaringClass != null ? declaringClass.getFullName() : null,
				node.getDefPos());
	}

	@Override
	public DexForgeSourceLocation findDefinition(String uri, int line, int character) {
		NodeAtPosition nodeAtPosition = resolveNodeAtPosition(uri, line, character);
		JavaNode node = nodeAtPosition.node;
		JavaClass targetCls = getDeclaringClass(node);
		String targetCode = targetCls != null ? targetCls.getCodeInfo().getCodeStr() : nodeAtPosition.code;
		DexForgeSourcePosition start = getSourcePosition(targetCode, node.getDefPos());
		String targetUri = targetCls != null ? sourceUri(targetCls) : uri;
		return new DexForgeSourceLocation(targetUri, new DexForgeSourceRange(start, start));
	}

	@Override
	public List<DexForgeSourceLocation> findReferences(String uri, int line, int character) {
		NodeAtPosition nodeAtPosition = resolveNodeAtPosition(uri, line, character);
		List<DexForgeSourceLocation> locations = new ArrayList<>();
		List<JavaNode> usages = nodeAtPosition.node.getUseIn();
		for (JavaNode usageNode : usages) {
			JavaClass parentCls = usageNode.getTopParentClass();
			if (parentCls == null && usageNode instanceof JavaClass) {
				parentCls = (JavaClass) usageNode;
			}
			if (parentCls == null) {
				continue;
			}
			ICodeInfo usageCodeInfo = parentCls.getCodeInfo();
			String usageCode = usageCodeInfo.getCodeStr();
			List<Integer> positions = parentCls.getUsePlacesFor(usageCodeInfo, nodeAtPosition.node);
			for (int pos : positions) {
				DexForgeSourcePosition lspPos = getSourcePosition(usageCode, pos);
				locations.add(new DexForgeSourceLocation(sourceUri(parentCls), new DexForgeSourceRange(lspPos, lspPos)));
			}
		}
		return locations;
	}

	@Override
	public List<DexForgeWorkspaceSymbol> findWorkspaceSymbols(String query, int limit) {
		return index.search(query, limit);
	}

	@Override
	public boolean isIndexingComplete() {
		return indexingComplete;
	}

	@Override
	public int getIndexedSymbolsCount() {
		return index.size();
	}

	private void startIndexing() {
		java.util.concurrent.ForkJoinPool.commonPool().execute(() -> {
			try {
				for (JavaClass cls : decompiler.getClasses()) {
					index.addSymbol(createSymbol(cls.getName(), 5, cls, cls.getPackage()));
					for (JavaMethod mth : cls.getMethods()) {
						index.addSymbol(createSymbol(mth.getName(), 6, mth, cls.getFullName()));
					}
					for (JavaField fld : cls.getFields()) {
						index.addSymbol(createSymbol(fld.getName(), 8, fld, cls.getFullName()));
					}
				}
				for (ResourceFile res : decompiler.getResources()) {
					index.addSymbol(new DexForgeWorkspaceSymbol(
							res.getOriginalName(),
							14, // Kind 14 is File/Resource in LSP
							new DexForgeSourceLocation("file:///resources/" + res.getOriginalName(),
									new DexForgeSourceRange(new DexForgeSourcePosition(0, 0), new DexForgeSourcePosition(0, 0))),
							"resources"));
				}
				indexingComplete = true;
			} catch (Exception e) {
				// ignore
			}
		});
	}

	@Override
	public DexForgeHover getHover(String uri, int line, int character) {
		NodeAtPosition nodeAtPosition = resolveNodeAtPosition(uri, line, character);
		return new DexForgeHover("```java\n" + getSymbolSignature(nodeAtPosition.node) + "\n```");
	}

	@Override
	public void decompileProject(java.nio.file.Path outputPath, DexForgeProgressReporter progressReporter) {
		decompiler.getArgs().setOutDir(outputPath.toFile());
		decompiler.save(500, progressReporter::onProgress);
	}

	@Override
	public List<DexForgeDiagnostic> getDiagnostics() {
		List<DexForgeDiagnostic> list = new ArrayList<>();
		ErrorsCounter errorsCounter = decompiler.getRoot().getErrorsCounter();
		for (jadx.core.dex.attributes.IAttributeNode node : errorsCounter.getErrorNodes()) {
			for (JadxError err : node.getAll(AType.JADX_ERROR)) {
				String sourceName = extractSourceName(node);
				String methodName = extractMethodName(node);
				String errorMessage = err.getError();
				DexForgeDiagnosticCategory category = categorizeError(errorMessage);
				if (category == DexForgeDiagnosticCategory.OVERFLOW_REGION) {
					continue;
				}
				DexForgeDiagnostic.Builder builder = DexForgeDiagnostic.builder(DexForgeDiagnosticSeverity.ERROR, errorMessage)
						.source(sourceName);
				if (methodName != null) {
					builder.method(methodName);
				}
				list.add(builder.build());
			}
		}
		return list;
	}

	/**
	 * Returns errors grouped by category for analysis.
	 */
	public Map<DexForgeDiagnosticCategory, Integer> getErrorCountsByCategory() {
		Map<DexForgeDiagnosticCategory, Integer> counts = new java.util.HashMap<>();
		ErrorsCounter errorsCounter = decompiler.getRoot().getErrorsCounter();
		for (jadx.core.dex.attributes.IAttributeNode node : errorsCounter.getErrorNodes()) {
			for (JadxError err : node.getAll(AType.JADX_ERROR)) {
				String errorMessage = err.getError();
				DexForgeDiagnosticCategory category = categorizeError(errorMessage);
				if (category == null) {
					continue;
				}
				counts.merge(category, 1, Integer::sum);
			}
		}
		return counts;
	}

	private DexForgeDiagnosticCategory categorizeError(String errorMessage) {
		if (errorMessage.contains("Regions count limit reached")) {
			return DexForgeDiagnosticCategory.OVERFLOW_REGION;
		}
		if (errorMessage.contains("Code restructure failed")) {
			return DexForgeDiagnosticCategory.CODE_RESTRUCTURE_FAILED;
		}
		if (errorMessage.contains("Type inference failed")) {
			return DexForgeDiagnosticCategory.TYPE_INFERENCE_FAILED;
		}
		return null;
	}

	private String extractSourceName(jadx.core.dex.attributes.IAttributeNode node) {
		if (node instanceof ClassNode) {
			return ((ClassNode) node).getClassInfo().getFullName();
		}
		if (node instanceof jadx.core.dex.nodes.MethodNode) {
			jadx.core.dex.nodes.MethodNode mth = (jadx.core.dex.nodes.MethodNode) node;
			return mth.getParentClass().getClassInfo().getFullName();
		}
		return node.toString();
	}

	private String extractMethodName(jadx.core.dex.attributes.IAttributeNode node) {
		if (node instanceof jadx.core.dex.nodes.MethodNode) {
			jadx.core.dex.nodes.MethodNode mth = (jadx.core.dex.nodes.MethodNode) node;
			return mth.getMethodInfo().getName();
		}
		return null;
	}

	@Override
	public void unloadClasses() {
		for (ClassNode cls : decompiler.getRoot().getClasses()) {
			ProcessState clsState = cls.getState();
			cls.unload();
			cls.setState(clsState == ProcessState.PROCESS_COMPLETE ? ProcessState.GENERATED_AND_UNLOADED : ProcessState.NOT_LOADED);
		}
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		if (type.isInstance(decompiler)) {
			return type.cast(decompiler);
		}
		return DexForgeProjectSession.super.unwrap(type);
	}

	@Override
	public void close() {
		decompiler.close();
		if (project.getStatus().isOpen()) {
			project.close();
		}
	}

	private JavaClass searchClass(String className) {
		JavaClass cls = decompiler.searchJavaClassByOrigFullName(className);
		if (cls == null) {
			cls = decompiler.searchJavaClassByAliasFullName(className);
		}
		return cls;
	}

	private NodeAtPosition resolveNodeAtPosition(String uri, int line, int character) {
		JavaClass cls = searchClassByUri(uri);
		if (cls == null) {
			throw new IllegalArgumentException("Class not found for URI: " + uri);
		}

		ICodeInfo codeInfo = cls.getCodeInfo();
		String code = codeInfo.getCodeStr();
		int offset = getPositionOffset(code, line, character);
		JavaNode node = decompiler.getJavaNodeAtPosition(codeInfo, offset);
		if (node == null) {
			node = decompiler.getClosestJavaNode(codeInfo, offset);
		}
		if (node == null) {
			throw new IllegalArgumentException("No symbol found at position " + offset);
		}
		return new NodeAtPosition(node, code);
	}

	private JavaClass searchClassByUri(String uri) {
		int dotIdx = uri.lastIndexOf('.');
		if (dotIdx == -1) {
			throw new IllegalArgumentException("Invalid document URI extension: " + uri);
		}
		String cleanPath = uri.substring(0, dotIdx).replace('\\', '/');
		return decompiler.getClasses().stream()
				.filter(cls -> cleanPath.endsWith(cls.getFullName().replace('.', '/')))
				.findFirst().orElse(null);
	}

	private int getPositionOffset(String code, int line, int character) {
		int pos = 0;
		int currentLine = 0;
		while (currentLine < line && pos < code.length()) {
			int nextNewline = code.indexOf('\n', pos);
			if (nextNewline == -1) {
				break;
			}
			pos = nextNewline + 1;
			currentLine++;
		}
		return Math.min(code.length(), pos + character);
	}

	private DexForgeSourcePosition getSourcePosition(String code, int defPos) {
		int line = 0;
		int character = 0;
		int pos = 0;
		while (pos < defPos && pos < code.length()) {
			if (code.charAt(pos) == '\n') {
				line++;
				character = 0;
			} else {
				character++;
			}
			pos++;
		}
		return new DexForgeSourcePosition(line, character);
	}

	private DexForgeWorkspaceSymbol createSymbol(String name, int kind, JavaNode node, String containerName) {
		JavaClass declClass = getDeclaringClass(node);
		String targetUri = declClass != null ? sourceUri(declClass) : "file:///sources/unknown.java";
		DexForgeSourcePosition start = new DexForgeSourcePosition(0, 0);
		return new DexForgeWorkspaceSymbol(name, kind, new DexForgeSourceLocation(targetUri, new DexForgeSourceRange(start, start)),
				containerName);
	}

	private JavaClass getDeclaringClass(JavaNode node) {
		JavaClass declClass = node.getDeclaringClass();
		if (declClass == null && node instanceof JavaClass) {
			declClass = (JavaClass) node;
		}
		return declClass;
	}

	private String sourceUri(JavaClass cls) {
		return "file:///sources/" + cls.getFullName().replace('.', '/') + ".java";
	}

	private String getSymbolSignature(JavaNode node) {
		if (node instanceof JavaClass) {
			JavaClass cls = (JavaClass) node;
			String kind = "class";
			if (cls.getAccessInfo().isInterface()) {
				kind = "interface";
			} else if (cls.getAccessInfo().isEnum()) {
				kind = "enum";
			} else if (cls.getAccessInfo().isAnnotation()) {
				kind = "@interface";
			}
			return cls.getAccessInfo().makeString(false) + kind + " " + cls.getFullName();
		}
		if (node instanceof JavaMethod) {
			JavaMethod mth = (JavaMethod) node;
			StringBuilder sb = new StringBuilder();
			sb.append(mth.getAccessFlags().makeString(false));
			if (!mth.isConstructor()) {
				sb.append(mth.getReturnType()).append(" ");
			}
			sb.append(mth.getName()).append("(");
			List<jadx.core.dex.instructions.args.ArgType> args = mth.getArguments();
			for (int i = 0; i < args.size(); i++) {
				sb.append(args.get(i)).append(" arg").append(i);
				if (i < args.size() - 1) {
					sb.append(", ");
				}
			}
			sb.append(")");
			return sb.toString();
		}
		if (node instanceof JavaField) {
			JavaField fld = (JavaField) node;
			return fld.getAccessFlags().makeString(false) + fld.getType() + " " + fld.getName();
		}
		return node.getFullName();
	}

	private static final class NodeAtPosition {
		private final JavaNode node;
		private final String code;

		private NodeAtPosition(JavaNode node, String code) {
			this.node = node;
			this.code = code;
		}
	}

	private static void setupCache(JadxDecompiler decompiler, JadxArgs args, DexForgeDecompilationSettings settings) {
		// 1. Setup Usage Info Cache
		if (settings.getUsageCacheMode() != null && settings.getCacheDir() != null) {
			switch (settings.getUsageCacheMode()) {
				case NONE:
					args.setUsageInfoCache(new jadx.api.usage.impl.EmptyUsageInfoCache());
					break;
				case MEMORY:
					args.setUsageInfoCache(new jadx.api.usage.impl.InMemoryUsageInfoCache());
					break;
				case DISK:
					args.setUsageInfoCache(new dexforge.core.infrastructure.jadx.cache.usage.UsageInfoCache(
							settings.getCacheDir(), args.getInputFiles()));
					break;
			}
		}

		// 2. Setup Code Cache
		if (settings.getCodeCacheMode() != null) {
			switch (settings.getCodeCacheMode()) {
				case MEMORY:
					args.setCodeCache(new jadx.api.impl.InMemoryCodeCache());
					break;
				case DISK_WITH_CACHE:
					decompiler.addCustomPass(new JadxPreparePass() {
						@Override
						public JadxPassInfo getInfo() {
							return new SimpleJadxPassInfo("CacheInit");
						}

						@Override
						public void init(jadx.core.dex.nodes.RootNode root) {
							root.getArgs().setCodeCache(new CodeStringCache(
									new BufferCodeCache(
											new DiskCodeCache(root, settings.getCacheDir())
									)
							));
						}
					});
					break;
				case DISK:
					decompiler.addCustomPass(new JadxPreparePass() {
						@Override
						public JadxPassInfo getInfo() {
							return new SimpleJadxPassInfo("CacheInit");
						}

						@Override
						public void init(jadx.core.dex.nodes.RootNode root) {
							root.getArgs().setCodeCache(new BufferCodeCache(
									new DiskCodeCache(root, settings.getCacheDir())
							));
						}
					});
					break;
			}
		}
	}
}
