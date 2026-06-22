package dexforge.api.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dexforge.api.plugin.DexForgePlugin;
import dexforge.api.plugin.DexForgePluginRegistry;
import dexforge.core.infrastructure.jadx.JadxDecompilerHelper;

/**
 * Entry point for DexForge API.
 * SRP: Responsible for creating and configuring DexForgeProject instances.
 */
public final class DexForgeDecompiler {
	private final List<File> inputFiles = new ArrayList<>();
	private DexForgeSettings settings = DexForgeSettings.builder().useDexForgeApi(true).build();
	private File outDir;
	private DexForgePluginRegistry pluginRegistry;

	public DexForgeDecompiler() {
	}

	public static DexForgeDecompiler builder() {
		return new DexForgeDecompiler();
	}

	public static DexForgeProject open(File inputFile) {
		return builder()
				.inputFile(inputFile)
				.loadProject();
	}

	public DexForgeDecompiler settings(DexForgeSettings settings) {
		this.settings = Objects.requireNonNull(settings);
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
		Object args = JadxDecompilerHelper.createArgs();
		for (File file : inputFiles) {
			JadxDecompilerHelper.addInputFile(args, file);
		}
		if (outDir != null) {
			JadxDecompilerHelper.setOutDir(args, outDir);
		}

		settings.applyTo(args);

		Object delegate = JadxDecompilerHelper.createDecompiler(args);
		if (pluginRegistry != null) {
			JadxDecompilerHelper.setPluginLoader(JadxDecompilerHelper.getArgs(delegate), pluginRegistry);
		}

		return new DexForgeProject(delegate, settings);
	}

	private DexForgeProject loadProject() {
		DexForgeProject project = build();
		project.load();
		return project;
	}
}
