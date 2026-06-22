package jadx.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dexforge.api.plugins.pass.JadxPassInfo;
import dexforge.api.plugins.pass.impl.SimpleJadxPassInfo;
import dexforge.api.plugins.pass.types.JadxPreparePass;
import dexforge.cli.DexforgeAppCommon;
import dexforge.cli.plugins.DexforgeFilesGetter;
import dexforge.core.infrastructure.jadx.JadxBackedDexForgeEngine;
import dexforge.engine.DexForgeDiagnostic;
import dexforge.engine.DexForgeEngine;
import dexforge.engine.DexForgeOpenProjectRequest;
import dexforge.engine.DexForgeProjectSession;

import jadx.api.ICodeInfo;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.JavaNode;
import jadx.api.JavaPackage;
import jadx.api.ResourceFile;
import jadx.api.impl.InMemoryCodeCache;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.usage.impl.EmptyUsageInfoCache;
import jadx.api.usage.impl.InMemoryUsageInfoCache;
import jadx.core.dex.nodes.RootNode;
import jadx.core.plugins.AppContext;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.gui.cache.code.CodeCacheMode;
import jadx.gui.cache.code.CodeStringCache;
import jadx.gui.cache.code.disk.BufferCodeCache;
import jadx.gui.cache.code.disk.DiskCodeCache;
import jadx.gui.cache.usage.UsageInfoCache;
import jadx.gui.plugins.context.CommonGuiPluginsContext;
import jadx.gui.settings.JadxProject;
import jadx.gui.settings.JadxSettings;
import jadx.gui.ui.MainWindow;
import jadx.gui.utils.CacheObject;
import jadx.plugins.tools.JadxExternalPluginsLoader;

@SuppressWarnings("ConstantConditions")
public class JadxWrapper {
	private static final Logger LOG = LoggerFactory.getLogger(JadxWrapper.class);

	private static final Object DECOMPILER_UPDATE_SYNC = new Object();

	private final MainWindow mainWindow;
	private volatile @Nullable DexForgeProjectSession projectSession;
	private CommonGuiPluginsContext guiPluginsContext;

	public JadxWrapper(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	public void open() {
		close();
		try {
			synchronized (DECOMPILER_UPDATE_SYNC) {
				JadxProject project = getProject();
				List<java.nio.file.Path> filePaths = project.getFilePaths();
				if (filePaths.isEmpty()) {
					throw new JadxRuntimeException("No input files selected");
				}
				java.nio.file.Path inputPath = filePaths.get(0);

				JadxArgs jadxArgs = getSettings().toJadxArgs();
				jadxArgs.setPluginLoader(new JadxExternalPluginsLoader());
				jadxArgs.setFilesGetter(DexforgeFilesGetter.INSTANCE);
				project.fillJadxArgs(jadxArgs);
				DexforgeAppCommon.applyEnvVars(jadxArgs);

				DexForgeEngine engine = JadxBackedDexForgeEngine.create(jadxArgs);
				DexForgeOpenProjectRequest openRequest = DexForgeOpenProjectRequest.builder(inputPath)
						.deobfuscationOn(getSettings().isDeobfuscationOn())
						.decompilerConfigurator(obj -> {
							JadxDecompiler dec = (JadxDecompiler) obj;
							this.guiPluginsContext = initGuiPluginsContext(dec, mainWindow);
							initUsageCache(jadxArgs);
							registerCodeCache(dec);
							dec.setEventsImpl(mainWindow.events());
						})
						.build();

				projectSession = engine.openProject(openRequest);
			}
		} catch (Exception e) {
			LOG.error("Jadx decompiler wrapper init error", e);
			close();
		}
	}

	// TODO: check and move into core package
	public void unloadClasses() {
		if (projectSession != null) {
			projectSession.unloadClasses();
		}
	}

	public void close() {
		try {
			synchronized (DECOMPILER_UPDATE_SYNC) {
				if (projectSession != null) {
					projectSession.close();
					projectSession = null;
				}
				if (guiPluginsContext != null) {
					resetGuiPluginsContext();
					guiPluginsContext = null;
				}
			}
		} catch (Exception e) {
			LOG.error("Jadx decompiler close error", e);
		} finally {
			mainWindow.getCacheObject().reset();
		}
	}

	/**
	 * Disk cache require loaded classes to operate, but cache should be set before 'after load' event
	 * to allow plugins decompile classes with cache enabled.
	 * To resolve this, register last 'prepare' pass for cache initialization.
	 */
	private void registerCodeCache(JadxDecompiler jadxDecompiler) {
		CodeCacheMode codeCacheMode = getSettings().getCodeCacheMode();
		if (codeCacheMode == CodeCacheMode.MEMORY) {
			jadxDecompiler.getArgs().setCodeCache(new InMemoryCodeCache());
			return;
		}
		jadxDecompiler.addCustomPass(new JadxPreparePass() {
			@Override
			public JadxPassInfo getInfo() {
				return new SimpleJadxPassInfo("CacheInit");
			}

			@Override
			public void init(RootNode root) {
				switch (getSettings().getCodeCacheMode()) {
					case DISK_WITH_CACHE:
						root.getArgs().setCodeCache(new CodeStringCache(buildBufferedDiskCache(root)));
						break;
					case DISK:
						root.getArgs().setCodeCache(buildBufferedDiskCache(root));
						break;
				}
			}
		});
	}

	private BufferCodeCache buildBufferedDiskCache(RootNode root) {
		DiskCodeCache diskCache = new DiskCodeCache(root, getProject().getCacheDir());
		return new BufferCodeCache(diskCache);
	}

	private void initUsageCache(JadxArgs jadxArgs) {
		switch (getSettings().getUsageCacheMode()) {
			case NONE:
				jadxArgs.setUsageInfoCache(new EmptyUsageInfoCache());
				break;
			case MEMORY:
				jadxArgs.setUsageInfoCache(new InMemoryUsageInfoCache());
				break;
			case DISK:
				jadxArgs.setUsageInfoCache(new UsageInfoCache(getProject().getCacheDir(), jadxArgs.getInputFiles()));
				break;
		}
	}

	public static CommonGuiPluginsContext initGuiPluginsContext(JadxDecompiler decompiler, MainWindow mainWindow) {
		CommonGuiPluginsContext guiPluginsContext = new CommonGuiPluginsContext(mainWindow);
		decompiler.getPluginManager().registerAddPluginListener(pluginContext -> {
			AppContext appContext = new AppContext();
			appContext.setGuiContext(guiPluginsContext.buildForPlugin(pluginContext));
			appContext.setFilesGetter(decompiler.getArgs().getFilesGetter());
			pluginContext.setAppContext(appContext);
		});
		return guiPluginsContext;
	}

	public CommonGuiPluginsContext getGuiPluginsContext() {
		return guiPluginsContext;
	}

	public void resetGuiPluginsContext() {
		guiPluginsContext.reset();
	}

	public void reloadPasses() {
		resetGuiPluginsContext();
		getDecompiler().reloadPasses();
	}

	/**
	 * Get the complete list of classes
	 */
	public List<JavaClass> getClasses() {
		return getDecompiler().getClasses();
	}

	/**
	 * Get all classes that are not excluded by the excluded packages settings
	 */
	public List<JavaClass> getIncludedClasses() {
		List<JavaClass> classList = getDecompiler().getClasses();
		List<String> excludedPackages = getExcludedPackages();
		if (excludedPackages.isEmpty()) {
			return classList;
		}
		return classList.stream()
				.filter(cls -> isClassIncluded(excludedPackages, cls))
				.collect(Collectors.toList());
	}

	/**
	 * Get all classes that are not excluded by the excluded packages settings including inner classes
	 */
	public List<JavaClass> getIncludedClassesWithInners() {
		List<JavaClass> classes = getDecompiler().getClassesWithInners();
		List<String> excludedPackages = getExcludedPackages();
		if (excludedPackages.isEmpty()) {
			return classes;
		}
		return classes.stream()
				.filter(cls -> isClassIncluded(excludedPackages, cls))
				.collect(Collectors.toList());
	}

	private static boolean isClassIncluded(List<String> excludedPackages, JavaClass cls) {
		for (String exclude : excludedPackages) {
			String clsFullName = cls.getFullName();
			if (clsFullName.equals(exclude)
					|| clsFullName.startsWith(exclude + '.')) {
				return false;
			}
		}
		return true;
	}

	public List<List<JavaClass>> buildDecompileBatches(List<JavaClass> classes) {
		return getDecompiler().getDecompileScheduler().buildBatches(classes);
	}

	// TODO: move to CLI and filter classes in JadxDecompiler
	public List<String> getExcludedPackages() {
		String excludedPackages = getSettings().getExcludedPackages().trim();
		if (excludedPackages.isEmpty()) {
			return Collections.emptyList();
		}
		return Arrays.asList(excludedPackages.split(" +"));
	}

	public void setExcludedPackages(List<String> packagesToExclude) {
		getSettings().setExcludedPackages(String.join(" ", packagesToExclude).trim());
		getSettings().sync();
	}

	public void addExcludedPackage(String packageToExclude) {
		String newExclusion = getSettings().getExcludedPackages() + ' ' + packageToExclude;
		getSettings().setExcludedPackages(newExclusion.trim());
		getSettings().sync();
	}

	public void removeExcludedPackage(String packageToRemoveFromExclusion) {
		List<String> list = new ArrayList<>(getExcludedPackages());
		list.remove(packageToRemoveFromExclusion);
		getSettings().setExcludedPackages(String.join(" ", list));
		getSettings().sync();
	}

	public Optional<JadxDecompiler> getCurrentDecompiler() {
		synchronized (DECOMPILER_UPDATE_SYNC) {
			if (projectSession == null) {
				return Optional.empty();
			}
			return Optional.ofNullable(projectSession.unwrap(JadxDecompiler.class));
		}
	}

	public Optional<DexForgeProjectSession> getProjectSession() {
		synchronized (DECOMPILER_UPDATE_SYNC) {
			return Optional.ofNullable(projectSession);
		}
	}

	public List<DexForgeDiagnostic> getDiagnostics() {
		synchronized (DECOMPILER_UPDATE_SYNC) {
			if (projectSession == null) {
				return List.of();
			}
			return projectSession.getDiagnostics();
		}
	}

	/**
	 * TODO: make method private
	 * Do not store JadxDecompiler in fields to not leak old instances
	 */
	public @NotNull JadxDecompiler getDecompiler() {
		if (projectSession == null) {
			throw new JadxRuntimeException("Session not yet loaded");
		}
		JadxDecompiler dec = projectSession.unwrap(JadxDecompiler.class);
		if (dec == null || dec.getRoot() == null) {
			throw new JadxRuntimeException("Decompiler not yet loaded");
		}
		return dec;
	}

	// TODO: forbid usage of this method
	public RootNode getRootNode() {
		return getDecompiler().getRoot();
	}

	public void reloadCodeData() {
		getDecompiler().reloadCodeData();
	}

	public JavaNode getJavaNodeByRef(ICodeNodeRef nodeRef) {
		return getDecompiler().getJavaNodeByRef(nodeRef);
	}

	public @Nullable JavaNode getEnclosingNode(ICodeInfo codeInfo, int pos) {
		return getDecompiler().getEnclosingNode(codeInfo, pos);
	}

	public List<JavaPackage> getPackages() {
		return getDecompiler().getPackages();
	}

	public List<ResourceFile> getResources() {
		return getDecompiler().getResources();
	}

	public JadxArgs getArgs() {
		return getDecompiler().getArgs();
	}

	public JadxProject getProject() {
		return mainWindow.getProject();
	}

	public JadxSettings getSettings() {
		return mainWindow.getSettings();
	}

	public CacheObject getCache() {
		return mainWindow.getCacheObject();
	}

	/**
	 * @param fullName Full name of an outer class. Inner classes are not supported.
	 */
	public @Nullable JavaClass searchJavaClassByFullAlias(String fullName) {
		return getDecompiler().getClasses().stream()
				.filter(cls -> cls.getFullName().equals(fullName))
				.findFirst()
				.orElse(null);
	}

	public @Nullable JavaClass searchJavaClassByOrigClassName(String fullName) {
		return getDecompiler().searchJavaClassByOrigFullName(fullName);
	}

	/**
	 * @param rawName Full raw name of an outer class. Inner classes are not supported.
	 */
	public @Nullable JavaClass searchJavaClassByRawName(String rawName) {
		return getDecompiler().getClasses().stream()
				.filter(cls -> cls.getRawName().equals(rawName))
				.findFirst()
				.orElse(null);
	}
}
