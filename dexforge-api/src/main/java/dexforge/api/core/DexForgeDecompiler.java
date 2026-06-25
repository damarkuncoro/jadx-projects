package dexforge.api.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import dexforge.api.engine.DexForgeEngine;
import dexforge.api.engine.EngineRegistry;
import dexforge.api.model.IDexForgeCodeCache;
import dexforge.api.persistence.DexForgeProjectState;
import dexforge.api.persistence.DexForgeProjectStore;
import dexforge.api.plugin.DexForgePlugin;
import dexforge.api.plugin.DexForgePluginRegistry;

/**
 * Entry point for DexForge API.
 * SRP: Responsible for creating and configuring DexForgeProject instances.
 */
public final class DexForgeDecompiler {
	private final List<File> inputFiles = new ArrayList<>();
	private DexForgeSettings settings = DexForgeSettings.builder().useDexForgeApi(true).build();
	private String engineId = "fast"; // Now Internal (Fast) is default
	private IDexForgeCodeCache codeCache;
	private File outDir;
	private DexForgePluginRegistry pluginRegistry;

	public DexForgeDecompiler() {
	}

	public static DexForgeDecompiler builder() {
		return new DexForgeDecompiler();
	}

	public static DexForgeProject open(File inputFile) {
		DexForgeProject project = builder()
				.inputFile(inputFile)
				.build();
		project.load();
		return project;
	}

	public static DexForgeProject loadProject(File projectFile, DexForgeProjectStore store) throws IOException {
		DexForgeProjectState state = store.load(projectFile);
		List<File> inputFiles = state.getInputFiles().stream()
				.map(File::new)
				.collect(Collectors.toList());

		DexForgeProject project = builder()
				.engine(state.getEngineId())
				.inputFiles(inputFiles)
				.build();

		project.load();
		project.renames().loadHistory(state.getRenameHistory());
		// Re-apply renames to the engine?
		// Actually DexForgeRenameAction should probably be applied when nodes are accessed or right after load.
		// For now we just load the history.
		return project;
	}

	private DexForgeDecompiler inputFiles(List<File> files) {
		this.inputFiles.addAll(files);
		return this;
	}

	public DexForgeDecompiler engine(String engineId) {
		this.engineId = Objects.requireNonNull(engineId);
		return this;
	}

	public DexForgeDecompiler settings(DexForgeSettings settings) {
		this.settings = Objects.requireNonNull(settings);
		return this;
	}

	public DexForgeDecompiler codeCache(IDexForgeCodeCache codeCache) {
		this.codeCache = codeCache;
		return this;
	}

	public DexForgeDecompiler inputFile(File inputFile) {
		this.inputFiles.add(Objects.requireNonNull(inputFile));
		return this;
	}

	public DexForgeDecompiler outDir(File outDir) {
		this.outDir = Objects.requireNonNull(outDir);
		return this;
	}

	public DexForgeDecompiler pluginRegistry(DexForgePluginRegistry pluginRegistry) {
		this.pluginRegistry = Objects.requireNonNull(pluginRegistry);
		return this;
	}

	public DexForgeDecompiler registerPlugin(DexForgePlugin plugin) {
		if (pluginRegistry == null) {
			pluginRegistry = new DexForgePluginRegistry();
		}
		pluginRegistry.addPlugin(plugin);
		return this;
	}

	public DexForgeProject build() {
		if (inputFiles.isEmpty()) {
			throw new dexforge.api.exception.DexForgeException("MISSING_INPUT", "At least one input file is required");
		}

		DexForgeEngine engine = EngineRegistry.get(engineId)
				.orElseThrow(() -> new IllegalStateException("Engine not found: " + engineId));

		engine.init(inputFiles, settings.asMap());
		if (codeCache != null) {
			engine.setCodeCache(codeCache);
		}

		return new DexForgeProject(engine, settings, inputFiles);
	}

	private DexForgeProject loadProject() {
		DexForgeProject project = build();
		project.load();
		return project;
	}
}
