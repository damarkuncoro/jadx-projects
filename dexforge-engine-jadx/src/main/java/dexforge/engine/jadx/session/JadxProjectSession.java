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
import jadx.api.JavaField;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.api.ICodeInfo;
import java.util.HashMap;

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
		JavaClass cls = decompiler.searchJavaClassByAliasFullName(className);
		if (cls == null) {
			cls = decompiler.searchJavaClassByOrigFullName(className);
		}
		if (cls == null) {
			return null;
		}
		JavaNode node = decompiler.getJavaNodeAtPosition(cls.getCodeInfo(), position);
		if (node == null) {
			node = decompiler.getClosestJavaNode(cls.getCodeInfo(), position);
		}
		if (node == null) {
			return null;
		}
		return new DexForgeDefinitionInfo(
				node.getName(),
				node.getFullName(),
				node.getDeclaringClass() != null ? node.getDeclaringClass().getFullName() : null,
				node.getDefPos()
		);
	}

	@Override
	public DexForgeSourceLocation findDefinition(String uri, int line, int character) {
		int dotIdx = uri.lastIndexOf('.');
		if (dotIdx == -1) {
			return null;
		}
		String cleanPath = uri.substring(0, dotIdx).replace('\\', '/');
		JavaClass cls = decompiler.getClasses().stream()
				.filter(c -> cleanPath.endsWith(c.getFullName().replace('.', '/')))
				.findFirst().orElse(null);

		if (cls == null) {
			return null;
		}

		String code = cls.getCode();
		int offset = getPositionOffset(code, line, character);

		JavaNode node = decompiler.getJavaNodeAtPosition(cls.getCodeInfo(), offset);
		if (node == null) {
			node = decompiler.getClosestJavaNode(cls.getCodeInfo(), offset);
		}

		if (node == null) {
			return null;
		}

		JavaClass targetCls = node.getDeclaringClass();
		if (targetCls == null && node instanceof JavaClass) {
			targetCls = (JavaClass) node;
		}

		String targetCode = targetCls != null ? targetCls.getCodeInfo().getCodeStr() : code;
		int defPos = node.getDefPos();
		DexForgeSourcePosition startPos = getLspPosition(targetCode, defPos);

		String targetUri = uri;
		if (targetCls != null) {
			targetUri = "file:///sources/" + targetCls.getFullName().replace('.', '/') + ".java";
		}

		return new DexForgeSourceLocation(
				targetUri,
				new DexForgeSourceRange(startPos, startPos));
	}

	@Override
	public List<DexForgeSourceLocation> findReferences(String uri, int line, int character) {
		int dotIdx = uri.lastIndexOf('.');
		if (dotIdx == -1) {
			return java.util.Collections.emptyList();
		}
		String cleanPath = uri.substring(0, dotIdx).replace('\\', '/');
		JavaClass cls = decompiler.getClasses().stream()
				.filter(c -> cleanPath.endsWith(c.getFullName().replace('.', '/')))
				.findFirst().orElse(null);

		if (cls == null) {
			return java.util.Collections.emptyList();
		}

		String code = cls.getCode();
		int offset = getPositionOffset(code, line, character);

		JavaNode node = decompiler.getJavaNodeAtPosition(cls.getCodeInfo(), offset);
		if (node == null) {
			node = decompiler.getClosestJavaNode(cls.getCodeInfo(), offset);
		}

		if (node == null) {
			return java.util.Collections.emptyList();
		}

		List<DexForgeSourceLocation> locations = new ArrayList<>();
		try {
			List<JavaNode> usages = node.getUseIn();
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
				List<Integer> positions = parentCls.getUsePlacesFor(usageCodeInfo, node);
				for (int pos : positions) {
					DexForgeSourcePosition lspPos = getLspPosition(usageCode, pos);
					String parentUri = "file:///sources/" + parentCls.getFullName().replace('.', '/') + ".java";
					locations.add(new DexForgeSourceLocation(
							parentUri,
							new DexForgeSourceRange(lspPos, lspPos)));
				}
			}
		} catch (Exception e) {
			// Ignore
		}
		return locations;
	}

	@Override
	public List<DexForgeWorkspaceSymbol> findWorkspaceSymbols(String query, int limit) {
		String lowerQuery = query.toLowerCase();
		List<DexForgeWorkspaceSymbol> results = new ArrayList<>();
		int count = 0;

		for (JavaClass cls : decompiler.getClasses()) {
			if (count >= limit) {
				break;
			}
			String clsName = cls.getName();
			if (clsName.toLowerCase().contains(lowerQuery)) {
				results.add(createSymbol(cls.getName(), 5, cls, cls.getPackage()));
				count++;
			}

			for (JavaMethod mth : cls.getMethods()) {
				if (count >= limit) {
					break;
				}
				if (mth.getName().toLowerCase().contains(lowerQuery)) {
					results.add(createSymbol(mth.getName(), 6, mth, cls.getFullName()));
					count++;
				}
			}

			for (JavaField fld : cls.getFields()) {
				if (count >= limit) {
					break;
				}
				if (fld.getName().toLowerCase().contains(lowerQuery)) {
					results.add(createSymbol(fld.getName(), 8, fld, cls.getFullName()));
					count++;
				}
			}
		}
		return results;
	}

	private DexForgeWorkspaceSymbol createSymbol(String name, int kind, JavaNode node, String containerName) {
		JavaClass declClass = node.getDeclaringClass();
		if (declClass == null && node instanceof JavaClass) {
			declClass = (JavaClass) node;
		}

		String targetUri = "file:///sources/" + (declClass != null ? declClass.getFullName().replace('.', '/') : "unknown") + ".java";
		DexForgeSourcePosition startPos = new DexForgeSourcePosition(0, 0);
		DexForgeSourceLocation loc = new DexForgeSourceLocation(targetUri, new DexForgeSourceRange(startPos, startPos));
		return new DexForgeWorkspaceSymbol(name, kind, loc, containerName);
	}

	@Override
	public DexForgeHover getHover(String uri, int line, int character) {
		int dotIdx = uri.lastIndexOf('.');
		if (dotIdx == -1) {
			return null;
		}
		String cleanPath = uri.substring(0, dotIdx).replace('\\', '/');
		JavaClass cls = decompiler.getClasses().stream()
				.filter(c -> cleanPath.endsWith(c.getFullName().replace('.', '/')))
				.findFirst().orElse(null);

		if (cls == null) {
			return null;
		}

		String code = cls.getCode();
		int offset = getPositionOffset(code, line, character);

		JavaNode node = decompiler.getJavaNodeAtPosition(cls.getCodeInfo(), offset);
		if (node == null) {
			node = decompiler.getClosestJavaNode(cls.getCodeInfo(), offset);
		}

		if (node == null) {
			return null;
		}

		String sig = getSymbolSignature(node);
		return new DexForgeHover("```java\n" + sig + "\n```");
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

	private DexForgeSourcePosition getLspPosition(String code, int defPos) {
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
				sb.append(mth.getReturnType().toString()).append(" ");
			}
			sb.append(mth.getName()).append("(");
			List<jadx.core.dex.instructions.args.ArgType> args = mth.getArguments();
			for (int i = 0; i < args.size(); i++) {
				sb.append(args.get(i).toString()).append(" arg").append(i);
				if (i < args.size() - 1) {
					sb.append(", ");
				}
			}
			sb.append(")");
			return sb.toString();
		}
		if (node instanceof JavaField) {
			JavaField fld = (JavaField) node;
			return fld.getAccessFlags().makeString(false) + fld.getType().toString() + " " + fld.getName();
		}
		return node.getFullName();
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
