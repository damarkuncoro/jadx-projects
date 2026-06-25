package dexforge.gui.presentation.viewmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import dexforge.api.analysis.DexForgeFinding;
import dexforge.api.model.DexForgeApkMetadata;
import dexforge.api.model.DexForgeNode;
import dexforge.api.model.DexForgePackage;
import dexforge.api.resource.DexForgeResourceFile;
import dexforge.api.plugin.IDexForgePlugin;
import dexforge.gui.application.usecase.*;
import dexforge.gui.domain.model.GuiProject;

/**
 * ViewModel for the main window.
 */
public final class MainViewModel {
	private final OpenProjectUseCase openProjectUseCase;
	private final GetPackagesUseCase getPackagesUseCase;
	private final GetResourcesUseCase getResourcesUseCase;
	private final GetCodeUseCase getCodeUseCase;
	private final GetApkInfoUseCase getApkInfoUseCase;
	private final RunAnalysisUseCase runAnalysisUseCase;
	private final SearchUseCase searchUseCase;
	private final GetSmaliUseCase getSmaliUseCase;
	private final GetProjectInsightsUseCase getProjectInsightsUseCase;

	private GuiProject currentProject;
	private String currentCode = "";
	private String currentSmali = "";
	private List<DexForgeFinding> lastFindings = new ArrayList<>();
	private List<DexForgeNode> searchResults = new ArrayList<>();
	private List<DexForgeResourceFile> resources = new ArrayList<>();
	private Map<String, Object> projectInsights = new HashMap<>();
	private DexForgeApkMetadata apkMetadata;
	private String selectedEngine = "fast";
	private boolean loading = false;

	private final List<Consumer<GuiProject>> projectListeners = new ArrayList<>();
	private final List<Consumer<List<DexForgePackage>>> packagesListeners = new ArrayList<>();
	private final List<Consumer<List<DexForgeResourceFile>>> resourcesListeners = new ArrayList<>();
	private final List<Consumer<String>> codeListeners = new ArrayList<>();
	private final List<Consumer<String>> smaliListeners = new ArrayList<>();
	private final List<Consumer<List<DexForgeFinding>>> findingsListeners = new ArrayList<>();
	private final List<Consumer<List<DexForgeNode>>> searchListeners = new ArrayList<>();
	private final List<Consumer<DexForgeApkMetadata>> apkInfoListeners = new ArrayList<>();
	private final List<Consumer<Map<String, Object>>> insightsListeners = new ArrayList<>();
	private final List<Consumer<Boolean>> loadingListeners = new ArrayList<>();
	private final List<Consumer<String>> logListeners = new ArrayList<>();
	private final List<Consumer<String>> classRequestListeners = new ArrayList<>();
	private final List<Consumer<DexForgeResourceFile>> resourceRequestListeners = new ArrayList<>();
	private final List<Consumer<String>> errorListeners = new ArrayList<>();
	private final List<Consumer<List<IDexForgePlugin>>> pluginListeners = new ArrayList<>();

	public MainViewModel(OpenProjectUseCase openProjectUseCase, GetPackagesUseCase getPackagesUseCase,
						 GetResourcesUseCase getResourcesUseCase, GetCodeUseCase getCodeUseCase,
						 GetApkInfoUseCase getApkInfoUseCase, RunAnalysisUseCase runAnalysisUseCase,
						 SearchUseCase searchUseCase, GetSmaliUseCase getSmaliUseCase,
						 GetProjectInsightsUseCase getProjectInsightsUseCase) {
		this.openProjectUseCase = openProjectUseCase;
		this.getPackagesUseCase = getPackagesUseCase;
		this.getResourcesUseCase = getResourcesUseCase;
		this.getCodeUseCase = getCodeUseCase;
		this.getApkInfoUseCase = getApkInfoUseCase;
		this.runAnalysisUseCase = runAnalysisUseCase;
		this.searchUseCase = searchUseCase;
		this.getSmaliUseCase = getSmaliUseCase;
		this.getProjectInsightsUseCase = getProjectInsightsUseCase;
	}

	public void openFile(File file) {
		setLoading(true);
		new Thread(() -> {
			try {
				this.currentProject = openProjectUseCase.execute(file, selectedEngine);
				notifyProjectChanged();
				loadPackages();
				loadResources();
				loadApkInfo();
				loadInsights();
				loadPlugins();
			} catch (Exception e) {
				notifyError("Failed to open file: " + e.getMessage());
			} finally {
				setLoading(false);
			}
		}).start();
	}

	private void loadInsights() {
		this.projectInsights = getProjectInsightsUseCase.execute();
		for (Consumer<Map<String, Object>> listener : insightsListeners) {
			listener.accept(projectInsights);
		}
	}

	private void loadPlugins() {
		// Placeholder for plugin loading via UseCase
		List<IDexForgePlugin> plugins = new ArrayList<>();
		for (Consumer<List<IDexForgePlugin>> listener : pluginListeners) {
			listener.accept(plugins);
		}
	}

	private void loadPackages() {
		List<DexForgePackage> packages = getPackagesUseCase.execute();
		for (Consumer<List<DexForgePackage>> listener : packagesListeners) {
			listener.accept(packages);
		}
	}

	private void loadResources() {
		this.resources = getResourcesUseCase.execute();
		for (Consumer<List<DexForgeResourceFile>> listener : resourcesListeners) {
			listener.accept(resources);
		}
	}

	private void loadApkInfo() {
		this.apkMetadata = getApkInfoUseCase.execute();
		for (Consumer<DexForgeApkMetadata> listener : apkInfoListeners) {
			listener.accept(apkMetadata);
		}
	}

	public void selectClass(String className) {
		setLoading(true);
		new Thread(() -> {
			try {
				this.currentCode = getCodeUseCase.execute(className);
				this.currentSmali = getSmaliUseCase.execute(className);

				notifyCodeChanged();
				notifySmaliChanged();
			} catch (Exception e) {
				notifyError("Failed to decompile class: " + e.getMessage());
			} finally {
				setLoading(false);
			}
		}).start();
	}

	public void selectResource(DexForgeResourceFile res) {
		setLoading(true);
		new Thread(() -> {
			try {
				this.currentCode = res.getContent();
				notifyCodeChanged();
			} catch (Exception e) {
				notifyError("Failed to load resource: " + e.getMessage());
			} finally {
				setLoading(false);
			}
		}).start();
	}

	public void onResourcesLoaded(Consumer<List<DexForgeResourceFile>> listener) {
		resourcesListeners.add(listener);
	}

	public void onApkInfoLoaded(Consumer<DexForgeApkMetadata> listener) {
		apkInfoListeners.add(listener);
	}

	public void onInsightsLoaded(Consumer<Map<String, Object>> listener) {
		insightsListeners.add(listener);
	}

	public void runDeepAnalysis() {
		if (currentProject == null) return;
		log("Running deep analysis...");
		setLoading(true);
		new Thread(() -> {
			try {
				this.lastFindings = runAnalysisUseCase.execute();
				log("Analysis complete. Found " + lastFindings.size() + " issues.");
				notifyFindingsChanged();
			} catch (Exception e) {
				notifyError("Analysis failed: " + e.getMessage());
			} finally {
				setLoading(false);
			}
		}).start();
	}

	public void search(String query) {
		if (query == null || query.isBlank()) return;
		log("Searching for: " + query);
		setLoading(true);
		new Thread(() -> {
			try {
				this.searchResults = searchUseCase.execute(query);
				log("Search complete. Found " + searchResults.size() + " results.");
				notifySearchChanged();
			} catch (Exception e) {
				notifyError("Search failed: " + e.getMessage());
			} finally {
				setLoading(false);
			}
		}).start();
	}

	public void onSearchLoaded(Consumer<List<DexForgeNode>> listener) {
		searchListeners.add(listener);
	}

	public void toggleJadxEngine(boolean useJadx) {
		this.selectedEngine = useJadx ? "jadx" : "fast";
		log("Engine switched to: " + (useJadx ? "JADX (Standard)" : "DexForge (Fast)"));
	}

	private void setLoading(boolean loading) {
		this.loading = loading;
		for (Consumer<Boolean> listener : loadingListeners) {
			listener.accept(loading);
		}
	}

	public void onLoadingStatusChanged(Consumer<Boolean> listener) {
		loadingListeners.add(listener);
	}

	public void log(String message) {
		for (Consumer<String> listener : logListeners) {
			listener.accept(message);
		}
	}

	public void onLogReceived(Consumer<String> listener) {
		logListeners.add(listener);
	}

	public void requestClass(String className) {
		for (Consumer<String> listener : classRequestListeners) {
			listener.accept(className);
		}
	}

	public void onClassRequested(Consumer<String> listener) {
		classRequestListeners.add(listener);
	}

	public void requestResource(DexForgeResourceFile resourceFile) {
		for (Consumer<DexForgeResourceFile> listener : resourceRequestListeners) {
			listener.accept(resourceFile);
		}
	}

	public void onResourceRequested(Consumer<DexForgeResourceFile> listener) {
		resourceRequestListeners.add(listener);
	}

	public void onProjectChanged(Consumer<GuiProject> listener) {
		projectListeners.add(listener);
	}

	public void onPackagesLoaded(Consumer<List<DexForgePackage>> listener) {
		packagesListeners.add(listener);
	}

	public void onCodeChanged(Consumer<String> listener) {
		codeListeners.add(listener);
	}

	public void onSmaliChanged(Consumer<String> listener) {
		smaliListeners.add(listener);
	}

	public void onFindingsLoaded(Consumer<List<DexForgeFinding>> listener) {
		findingsListeners.add(listener);
	}

	public void onPluginsLoaded(Consumer<List<IDexForgePlugin>> listener) {
		pluginListeners.add(listener);
	}

	public void onError(Consumer<String> listener) {
		errorListeners.add(listener);
	}

	private void notifyProjectChanged() {
		for (Consumer<GuiProject> listener : projectListeners) {
			listener.accept(currentProject);
		}
	}

	private void notifyCodeChanged() {
		for (Consumer<String> listener : codeListeners) {
			listener.accept(currentCode);
		}
	}

	private void notifySmaliChanged() {
		for (Consumer<String> listener : smaliListeners) {
			listener.accept(currentSmali);
		}
	}

	private void notifyFindingsChanged() {
		for (Consumer<List<DexForgeFinding>> listener : findingsListeners) {
			listener.accept(lastFindings);
		}
	}

	private void notifySearchChanged() {
		for (Consumer<List<DexForgeNode>> listener : searchListeners) {
			listener.accept(searchResults);
		}
	}

	private void notifyError(String message) {
		for (Consumer<String> listener : errorListeners) {
			listener.accept(message);
		}
	}
}
