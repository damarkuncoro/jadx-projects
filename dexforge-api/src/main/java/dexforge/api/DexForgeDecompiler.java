package dexforge.api;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.api.plugins.JadxPlugin;
import dexforge.api.plugins.loader.JadxPluginLoader;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;

public final class DexForgeDecompiler implements Closeable {
	private final JadxDecompiler delegate;
	private final DexForgeSettings settings;

	public DexForgeDecompiler() {
		this(DexForgeSettings.create());
	}

	public DexForgeDecompiler(DexForgeSettings settings) {
		this(settings, new JadxArgs());
	}

	/**
	 * JADX bridge constructor kept for gradual migration only.
	 * Prefer {@link #builder()} for new DexForge API consumers.
	 */
	@Deprecated(forRemoval = false)
	public DexForgeDecompiler(DexForgeSettings settings, JadxArgs args) {
		this.settings = Objects.requireNonNull(settings);
		this.delegate = new JadxDecompiler(settings.applyTo(Objects.requireNonNull(args)));
	}

	public static Builder builder() {
		return new Builder();
	}

	public static DexForgeDecompiler open(File inputFile) {
		DexForgeDecompiler decompiler = builder()
				.inputFile(inputFile)
				.load();
		return decompiler;
	}

	private static DexForgeDecompiler create(Builder builder) {
		JadxArgs args = new JadxArgs();
		args.getInputFiles().addAll(builder.inputFiles);
		if (builder.outDir != null) {
			args.setOutDir(builder.outDir);
		}
		DexForgeDecompiler decompiler = new DexForgeDecompiler(builder.settings, args);
		if (builder.pluginRegistry != null) {
			decompiler.setPluginRegistry(builder.pluginRegistry);
		}
		return decompiler;
	}

	private DexForgeDecompiler loadAndReturn() {
		load();
		return this;
	}

	public DexForgeSettings getSettings() {
		return settings;
	}

	public void setPluginRegistry(DexForgePluginRegistry registry) {
		delegate.getArgs().setPluginLoader(Objects.requireNonNull(registry));
	}

	/**
	 * Compatibility bridge for existing JADX plugin loaders.
	 * Prefer {@link #setPluginRegistry(DexForgePluginRegistry)} for new code.
	 */
	@Deprecated(forRemoval = false)
	public void setPluginLoader(JadxPluginLoader loader) {
		delegate.getArgs().setPluginLoader(Objects.requireNonNull(loader));
	}

	public void registerPlugin(DexForgePlugin plugin) {
		delegate.registerPlugin(Objects.requireNonNull(plugin));
	}

	/**
	 * Compatibility bridge for existing JADX plugins.
	 * Prefer {@link #registerPlugin(DexForgePlugin)} for new code.
	 */
	@Deprecated(forRemoval = false)
	public void registerPlugin(JadxPlugin plugin) {
		delegate.registerPlugin(Objects.requireNonNull(plugin));
	}

	public void load() {
		try {
			delegate.load();
		} catch (RuntimeException e) {
			throw new DexForgeException("LOAD_FAILED", "Failed to load DexForge project", e);
		}
	}

	public void save() {
		try {
			delegate.save();
		} catch (RuntimeException e) {
			throw new DexForgeException("SAVE_FAILED", "Failed to save decompiled output", e);
		}
	}

	public List<DexForgeClass> getClasses() {
		return DexForgeNodeFactory.wrapClasses(delegate.getClasses());
	}

	public List<DexForgeClass> getClassesWithInners() {
		return DexForgeNodeFactory.wrapClasses(delegate.getClassesWithInners());
	}

	public List<DexForgePackage> getPackages() {
		return DexForgeNodeFactory.wrapPackages(delegate.getPackages());
	}

	public List<DexForgeResourceFile> getResources() {
		if (delegate.getResources().isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeResourceFile> result = new ArrayList<>(delegate.getResources().size());
		for (jadx.api.ResourceFile resourceFile : delegate.getResources()) {
			result.add(new DexForgeResourceFile(resourceFile));
		}
		return Collections.unmodifiableList(result);
	}

	public DexForgeClass searchClassByOriginalName(String fullName) {
		JavaClass cls = delegate.searchJavaClassByOrigFullName(fullName);
		return cls == null ? null : new DexForgeClass(cls);
	}

	public DexForgeClass searchClassByAlias(String fullName) {
		JavaClass cls = delegate.searchJavaClassByAliasFullName(fullName);
		return cls == null ? null : new DexForgeClass(cls);
	}

	public List<DexForgeDiagnostic> getDiagnostics() {
		List<DexForgeDiagnostic> diagnostics = new ArrayList<>();
		for (DexForgeClass dexForgeClass : getClasses()) {
			diagnostics.addAll(dexForgeClass.getDiagnostics());
		}
		int warnsCount = delegate.getWarnsCount();
		if (warnsCount > 0) {
			diagnostics.add(DexForgeDiagnostic.warning("JADX reported " + warnsCount + " warnings", "jadx"));
		}
		return Collections.unmodifiableList(diagnostics);
	}

	public int getErrorsCount() {
		return delegate.getErrorsCount();
	}

	public int getWarningsCount() {
		return delegate.getWarnsCount();
	}

	/**
	 * JADX bridge kept for compatibility during migration.
	 * New code should use DexForge API methods instead of unwrapping.
	 */
	@Deprecated(forRemoval = false)
	public JadxDecompiler unwrap() {
		return delegate;
	}

	@Override
	public void close() {
		delegate.close();
	}

	public static final class Builder {
		private final List<File> inputFiles = new ArrayList<>();
		private DexForgeSettings settings = DexForgeSettings.create().useDexForgeApi(true);
		private File outDir;
		private DexForgePluginRegistry pluginRegistry;

		private Builder() {
		}

		public Builder settings(DexForgeSettings settings) {
			this.settings = Objects.requireNonNull(settings);
			return this;
		}

		public Builder inputFile(File inputFile) {
			this.inputFiles.add(Objects.requireNonNull(inputFile));
			return this;
		}

		public Builder inputFiles(List<File> inputFiles) {
			this.inputFiles.addAll(Objects.requireNonNull(inputFiles));
			return this;
		}

		public Builder outDir(File outDir) {
			this.outDir = Objects.requireNonNull(outDir);
			return this;
		}

		public Builder pluginRegistry(DexForgePluginRegistry pluginRegistry) {
			this.pluginRegistry = Objects.requireNonNull(pluginRegistry);
			return this;
		}

		public DexForgeDecompiler build() {
			if (inputFiles.isEmpty()) {
				throw new DexForgeException("MISSING_INPUT", "At least one input file is required");
			}
			return DexForgeDecompiler.create(this);
		}

		public DexForgeDecompiler load() {
			return build().loadAndReturn();
		}
	}
}
