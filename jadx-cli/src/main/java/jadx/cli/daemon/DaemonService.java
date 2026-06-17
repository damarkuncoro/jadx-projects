package jadx.cli.daemon;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jadx.api.CommentsLevel;
import jadx.api.DecompilationMode;
import jadx.api.ICodeInfo;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.JavaNode;
import jadx.cli.dto.ClassDto;
import jadx.cli.dto.DaemonResponse;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.attributes.nodes.JadxError;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.MethodNode;

public class DaemonService {
	private volatile JadxDecompiler decompiler;

	public JadxDecompiler getDecompiler() {
		return decompiler;
	}

	public synchronized DaemonResponse load(int requestId, String path, Map<String, Object> params) {
		close();
		try {
			JadxArgs jadxArgs = new JadxArgs();
			jadxArgs.getInputFiles().add(new File(path));

			if (params.containsKey("deobfuscationOn")) {
				jadxArgs.setDeobfuscationOn((Boolean) params.get("deobfuscationOn"));
			}
			if (params.containsKey("commentsLevel")) {
				jadxArgs.setCommentsLevel(CommentsLevel.valueOf(((String) params.get("commentsLevel")).toUpperCase()));
			}
			if (params.containsKey("decompilationMode")) {
				jadxArgs.setDecompilationMode(DecompilationMode.valueOf(((String) params.get("decompilationMode")).toUpperCase()));
			}

			decompiler = new JadxDecompiler(jadxArgs);
			decompiler.load();

			Map<String, Object> result = new HashMap<>();
			result.put("classesCount", decompiler.getClasses().size());
			result.put("resourcesCount", decompiler.getResources().size());
			return DaemonResponse.success(requestId, result);
		} catch (Exception e) {
			return DaemonResponse.error(requestId, "Load failed: " + e.getMessage());
		}
	}

	public DaemonResponse listClasses(int requestId) {
		if (decompiler == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		List<ClassDto> list = new ArrayList<>();
		for (JavaClass cls : decompiler.getClasses()) {
			list.add(new ClassDto(
					cls.getFullName(),
					cls.getName(),
					cls.getClassNode().getAlias(),
					cls.getPackage()));
		}
		return DaemonResponse.success(requestId, list);
	}

	public DaemonResponse decompile(int requestId, String className) {
		if (decompiler == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		JavaClass cls = decompiler.searchJavaClassByOrigFullName(className);
		if (cls == null) {
			cls = decompiler.searchJavaClassByAliasFullName(className);
		}
		if (cls == null) {
			return DaemonResponse.error(requestId, "Class not found: " + className);
		}

		ICodeInfo codeInfo = cls.getCodeInfo();
		String code = codeInfo.getCodeStr();

		Map<String, Object> result = new HashMap<>();
		result.put("code", code);
		result.put("lineMapping", codeInfo.getCodeMetadata().getLineMapping());

		// Collect and append diagnostics (warnings/errors)
		List<Map<String, Object>> diagnostics = collectDiagnostics(cls.getClassNode(), code);
		result.put("diagnostics", diagnostics);

		return DaemonResponse.success(requestId, result);
	}

	private List<Map<String, Object>> collectDiagnostics(ClassNode classNode, String code) {
		List<Map<String, Object>> list = new ArrayList<>();
		collectDiagnosticsRecursive(classNode, code, list);
		return list;
	}

	private void collectDiagnosticsRecursive(ClassNode cls, String code, List<Map<String, Object>> list) {
		// Class errors
		for (JadxError err : cls.getAll(AType.JADX_ERROR)) {
			list.add(createDiagnosticMap(cls, err.getError(), code));
		}
		// Method errors
		for (MethodNode mth : cls.getMethods()) {
			for (JadxError err : mth.getAll(AType.JADX_ERROR)) {
				list.add(createDiagnosticMap(mth, err.getError(), code));
			}
		}
		// Field errors
		for (FieldNode fld : cls.getFields()) {
			for (JadxError err : fld.getAll(AType.JADX_ERROR)) {
				list.add(createDiagnosticMap(fld, err.getError(), code));
			}
		}
		// Inner classes
		for (ClassNode inner : cls.getInnerClasses()) {
			collectDiagnosticsRecursive(inner, code, list);
		}
	}

	private Map<String, Object> createDiagnosticMap(jadx.core.dex.nodes.IDexNode node, String message, String code) {
		int defPos = 0;
		if (node instanceof jadx.core.dex.attributes.ILineAttributeNode) {
			defPos = ((jadx.core.dex.attributes.ILineAttributeNode) node).getDefPosition();
		}

		int line = 0;
		int character = 0;
		if (defPos > 0 && defPos < code.length()) {
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
		}

		Map<String, Object> diag = new HashMap<>();
		diag.put("line", line);
		diag.put("character", character);
		diag.put("severity", "ERROR");
		diag.put("message", message);
		return diag;
	}

	public DaemonResponse getDefinition(int requestId, String className, int pos) {
		if (decompiler == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		JavaClass cls = decompiler.searchJavaClassByOrigFullName(className);
		if (cls == null) {
			cls = decompiler.searchJavaClassByAliasFullName(className);
		}
		if (cls == null) {
			return DaemonResponse.error(requestId, "Class not found: " + className);
		}

		ICodeInfo codeInfo = cls.getCodeInfo();
		JavaNode node = decompiler.getJavaNodeAtPosition(codeInfo, pos);
		if (node == null) {
			node = decompiler.getClosestJavaNode(codeInfo, pos);
		}

		if (node == null) {
			return DaemonResponse.error(requestId, "No symbol found at position " + pos);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("name", node.getName());
		result.put("fullName", node.getFullName());
		result.put("declaringClass", node.getDeclaringClass() != null ? node.getDeclaringClass().getFullName() : null);
		result.put("defPos", node.getDefPos());
		return DaemonResponse.success(requestId, result);
	}

	public synchronized void close() {
		if (decompiler != null) {
			try {
				decompiler.close();
			} catch (Exception e) {
				// ignore
			}
			decompiler = null;
		}
	}
}
