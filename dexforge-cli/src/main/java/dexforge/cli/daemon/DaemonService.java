package dexforge.cli.daemon;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import dexforge.api.core.DexForgeDecompiler;
import dexforge.api.core.DexForgeProject;
import dexforge.api.core.DexForgeSettings;
import dexforge.api.model.DexForgeClass;
import dexforge.api.model.DexForgeNode;
import dexforge.api.persistence.DexForgeProjectState;
import dexforge.api.persistence.DexForgeProjectStore;
import dexforge.cli.dto.ClassDto;
import dexforge.cli.dto.DaemonResponse;
import dexforge.core.infrastructure.persistence.JsonProjectStore;

public class DaemonService {
	private volatile DexForgeProject project;

	public DexForgeProject getProject() {
		return project;
	}

	public dexforge.engine.DexForgeProjectSession getProjectSession() {
		if (project == null) {
			return null;
		}
		dexforge.api.engine.DexForgeEngine apiEngine = project.getEngine();
		if (apiEngine instanceof dexforge.engine.jadx.JadxEngine) {
			return ((dexforge.engine.jadx.JadxEngine) apiEngine).getProjectSession();
		}
		return null;
	}

	public synchronized DaemonResponse load(int requestId, String path, Map<String, Object> params) {
		close();
		try {
			File inputFile = new File(path);
			DexForgeDecompiler decompiler = DexForgeDecompiler.builder()
					.engine("jadx")
					.inputFile(inputFile);

			// Apply settings from params
			DexForgeSettings.Builder settingsBuilder = DexForgeSettings.builder();
			if (params.containsKey("threadsCount")) {
				settingsBuilder.threadsCount(((Double) params.get("threadsCount")).intValue());
			}
			if (params.containsKey("commentsLevel")) {
				settingsBuilder.commentsLevel(dexforge.api.core.DexForgeCommentsLevel.valueOf(
						((String) params.get("commentsLevel")).toUpperCase()));
			}
			if (params.containsKey("decompilationMode")) {
				settingsBuilder.decompilationMode(dexforge.api.core.DexForgeDecompilationMode.valueOf(
						((String) params.get("decompilationMode")).toUpperCase()));
			}
			if (params.containsKey("deobfuscationOn")) {
				settingsBuilder.deobfuscationOn((Boolean) params.get("deobfuscationOn"));
			}
			decompiler.settings(settingsBuilder.build());

			project = decompiler.build();
			project.load();

			Map<String, Object> result = new HashMap<>();
			result.put("classesCount", project.getClasses().size());
			result.put("resourcesCount", project.getResources().size());
			return DaemonResponse.success(requestId, result);
		} catch (Exception e) {
			e.printStackTrace();
			return DaemonResponse.error(requestId, "Load failed: " + e.getMessage());
		}
	}

	public DaemonResponse listClasses(int requestId) {
		if (project == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		List<ClassDto> list = project.getClasses().stream()
				.map(cls -> new ClassDto(
						cls.getFullName(),
						cls.getName(),
						cls.getName(), // Alias
						cls.getPackageName()))
				.collect(Collectors.toList());
		return DaemonResponse.success(requestId, list);
	}

	public DaemonResponse decompile(int requestId, String className) {
		if (project == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		DexForgeClass cls = project.searchClassByAlias(className);
		if (cls == null) {
			return DaemonResponse.error(requestId, "Class not found: " + className);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("code", cls.getCode());
		result.put("diagnostics", java.util.Collections.emptyList());
		// result.put("lineMapping", cls.getCodeInfo().getLineMapping()); // TODO: add lineMapping to API if needed

		return DaemonResponse.success(requestId, result);
	}

	public DaemonResponse getDefinition(int requestId, String className, int pos) {
		if (project == null) {
			return DaemonResponse.error(requestId, "No active decompiler. Call 'load' first.");
		}
		DexForgeClass cls = project.searchClassByAlias(className);
		if (cls == null) {
			return DaemonResponse.error(requestId, "Class not found: " + className);
		}

		DexForgeNode node = cls.getNodeAt(pos);
		if (node == null) {
			return DaemonResponse.error(requestId, "No symbol found at position " + pos);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("name", node.getName());
		result.put("fullName", node.getFullName());
		result.put("declaringClass", node.getDeclaringClass() != null ? node.getDeclaringClass().getFullName() : null);
		result.put("defPos", node.getDefinitionPosition());
		return DaemonResponse.success(requestId, result);
	}

	public DaemonResponse saveProject(int requestId, String path) {
		if (project == null) {
			return DaemonResponse.error(requestId, "No active session to save");
		}
		try {
			JsonProjectStore store = new JsonProjectStore();
			project.save(new File(path), store);
			return DaemonResponse.success(requestId, "Project saved to " + path);
		} catch (Exception e) {
			return DaemonResponse.error(requestId, "Save failed: " + e.getMessage());
		}
	}

	public DaemonResponse loadProject(int requestId, String path) {
		try {
			JsonProjectStore store = new JsonProjectStore();
			DexForgeProjectState state = store.load(new File(path));

			if (state.getInputFiles().isEmpty()) {
				return DaemonResponse.error(requestId, "Stored project has no input files");
			}

			Map<String, Object> params = new HashMap<>();
			DaemonResponse response = load(requestId, state.getInputFiles().get(0), params);

			if (project != null && response.getStatus().equals("SUCCESS")) {
				// Verify integrity
				dexforge.api.core.DexForgeIntegrityStatus integrity = project.verifyIntegrity(state);

				if (!integrity.isValid()) {
					@SuppressWarnings("unchecked")
					Map<String, Object> result = (Map<String, Object>) response.getResult();
					result.put("integrityWarning", "Input files have changed since last save: " + integrity.getModifiedFiles());
				}

				// Recover renames
				project.loadState(state);
			}
			return response;
		} catch (Exception e) {
			return DaemonResponse.error(requestId, "Load project failed: " + e.getMessage());
		}
	}

	public synchronized void close() {
		if (project != null) {
			try {
				project.close();
			} catch (Exception e) {
				// ignore
			}
			project = null;
		}
	}
}
