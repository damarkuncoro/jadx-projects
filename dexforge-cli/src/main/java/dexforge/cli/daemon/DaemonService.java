package dexforge.cli.daemon;

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
import dexforge.core.infrastructure.jadx.JadxDiagnosticMapper;
import dexforge.engine.DexForgeDiagnostic;
import dexforge.cli.dto.ClassDto;
import dexforge.cli.dto.DaemonResponse;

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
		result.put("diagnostics", toJsonDiagnostics(JadxDiagnosticMapper.collectDiagnostics(cls.getClassNode(), code)));

		return DaemonResponse.success(requestId, result);
	}

	private List<Map<String, Object>> toJsonDiagnostics(List<DexForgeDiagnostic> diagnostics) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (DexForgeDiagnostic diagnostic : diagnostics) {
			Map<String, Object> map = new HashMap<>();
			map.put("line", diagnostic.getLine());
			map.put("character", diagnostic.getColumn());
			map.put("severity", diagnostic.getSeverity().name());
			map.put("message", diagnostic.getMessage());
			list.add(map);
		}
		return list;
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
