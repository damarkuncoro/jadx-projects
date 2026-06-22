package dexforge.cli.daemon;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dexforge.cli.dto.ClassDto;
import dexforge.cli.dto.DaemonResponse;
import dexforge.core.infrastructure.jadx.JadxBackedDexForgeEngine;
import dexforge.engine.DexForgeClassDecompileResult;
import dexforge.engine.DexForgeClassInfo;
import dexforge.engine.DexForgeDefinitionInfo;
import dexforge.engine.DexForgeOpenProjectRequest;
import dexforge.engine.DexForgeProjectSession;

public class DaemonService {
	private volatile DexForgeProjectSession projectSession;

	public DexForgeProjectSession getProjectSession() {
		return projectSession;
	}

	public synchronized DaemonResponse load(int requestId, String path, Map<String, Object> params) {
		close();
		try {
			DexForgeOpenProjectRequest.Builder requestBuilder = DexForgeOpenProjectRequest.builder(Path.of(path));
			if (params.containsKey("deobfuscationOn")) {
				requestBuilder.deobfuscationOn((Boolean) params.get("deobfuscationOn"));
			}
			if (params.containsKey("commentsLevel")) {
				requestBuilder.commentsLevel((String) params.get("commentsLevel"));
			}
			if (params.containsKey("decompilationMode")) {
				requestBuilder.decompilationMode((String) params.get("decompilationMode"));
			}

			projectSession = JadxBackedDexForgeEngine.create().openProject(requestBuilder.build());

			Map<String, Object> result = new HashMap<>();
			result.put("classesCount", projectSession.getClassesCount());
			result.put("resourcesCount", projectSession.getResourcesCount());
			return DaemonResponse.success(requestId, result);
		} catch (Exception e) {
			return DaemonResponse.error(requestId, "Load failed: " + e.getMessage());
		}
	}

	public DaemonResponse listClasses(int requestId) {
		if (projectSession == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		List<ClassDto> list = new ArrayList<>();
		for (DexForgeClassInfo cls : projectSession.listClasses()) {
			list.add(new ClassDto(
					cls.getFullName(),
					cls.getShortName(),
					cls.getAlias(),
					cls.getPackageName()));
		}
		return DaemonResponse.success(requestId, list);
	}

	public DaemonResponse decompile(int requestId, String className) {
		if (projectSession == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		DexForgeClassDecompileResult decompileResult;
		try {
			decompileResult = projectSession.decompileClass(className);
		} catch (IllegalArgumentException e) {
			return DaemonResponse.error(requestId, "Class not found: " + className);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("code", decompileResult.getCode());
		result.put("lineMapping", decompileResult.getLineMapping());

		// Collect and append diagnostics (warnings/errors)
		result.put("diagnostics", DaemonDiagnosticJsonMapper.toJsonDiagnostics(decompileResult.getDiagnostics()));

		return DaemonResponse.success(requestId, result);
	}

	public DaemonResponse getDefinition(int requestId, String className, int pos) {
		if (projectSession == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		DexForgeDefinitionInfo definitionInfo;
		try {
			definitionInfo = projectSession.getDefinition(className, pos);
		} catch (IllegalArgumentException e) {
			return DaemonResponse.error(requestId, e.getMessage());
		}

		Map<String, Object> result = new HashMap<>();
		result.put("name", definitionInfo.getName());
		result.put("fullName", definitionInfo.getFullName());
		result.put("declaringClass", definitionInfo.getDeclaringClass());
		result.put("defPos", definitionInfo.getDefinitionPosition());
		return DaemonResponse.success(requestId, result);
	}

	public synchronized void close() {
		if (projectSession != null) {
			try {
				projectSession.close();
			} catch (Exception e) {
				// ignore
			}
			projectSession = null;
		}
	}
}
