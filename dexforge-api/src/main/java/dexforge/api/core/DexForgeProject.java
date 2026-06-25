package dexforge.api.core;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import dexforge.api.analysis.AnalysisApplicationService;
import dexforge.api.analysis.DexForgeFinding;
import dexforge.api.diagnostic.DexForgeDiagnostic;
import dexforge.api.diagnostic.DexForgeDiagnosticManager;
import dexforge.api.engine.DexForgeEngine;
import dexforge.api.event.DexForgeEventBus;
import dexforge.api.exception.DexForgeException;
import dexforge.api.model.DexForgeClass;
import dexforge.api.model.DexForgeNodeFactory;
import dexforge.api.model.DexForgePackage;
import dexforge.api.persistence.DexForgeProjectState;
import dexforge.api.persistence.DexForgeProjectStore;
import dexforge.api.query.DexForgeSearch;
import dexforge.api.rename.DexForgeRenameManager;
import dexforge.api.resource.DexForgeResourceFile;
import dexforge.api.intelligence.IProjectIntelligence;
import dexforge.api.ui.IUiEditor;

/**
 * Domain Aggregate representing a decompilation project.
 */
public final class DexForgeProject implements Closeable {
	private final DexForgeEngine engine;
	private final DexForgeSettings settings;
	private final List<File> inputFiles;
	private final DexForgeEventBus eventBus = new DexForgeEventBusImpl();
	private final DexForgeRenameManager renameManager = new DexForgeRenameManagerImpl(this);
	private final DexForgeDiagnosticManager diagnosticManager = new DexForgeDiagnosticManagerImpl(this);
	private final AnalysisApplicationService analysisService = new AnalysisApplicationService();
	private final dexforge.api.resource.IResourceDecoder resourceDecoder;
	private DexForgeSearch search;
	private IProjectIntelligence intelligence;

	DexForgeProject(DexForgeEngine engine, DexForgeSettings settings, List<File> inputFiles) {
		this.engine = Objects.requireNonNull(engine);
		this.settings = Objects.requireNonNull(settings);
		this.inputFiles = Collections.unmodifiableList(new ArrayList<>(inputFiles));
		this.resourceDecoder = new dexforge.api.resource.DexForgeResourceDecoder(engine);
		this.intelligence = engine.getIntelligence();
	}

	public void setIntelligence(IProjectIntelligence intelligence) {
		this.intelligence = intelligence;
	}

	public IProjectIntelligence getIntelligence() {
		return intelligence;
	}

	public IUiEditor getUiEditor(Object rootNode) {
		return engine.getUiEditor(rootNode);
	}

	public dexforge.api.resource.IResourceDecoder getResourceDecoder() {
		return resourceDecoder;
	}

	public DexForgeEventBus events() { return eventBus; }
	public DexForgeRenameManager renames() { return renameManager; }
	public DexForgeDiagnosticManager diagnostics() { return diagnosticManager; }

	/**
	 * Run deep analysis on the project.
	 */
	public List<DexForgeFinding> runAnalysis() {
		return analysisService.runAllAnalyses(this);
	}

	public DexForgeSearch search() {
		if (search == null) {
			search = new DexForgeSearch(this);
		}
		return search;
	}

	public void load() {
		try {
			engine.load();
			eventBus.publish(new dexforge.api.event.DexForgeProjectEvent(this, dexforge.api.event.DexForgeProjectEvent.Type.LOADED));
		} catch (RuntimeException e) {
			throw new DexForgeException("LOAD_FAILED", "Failed to load DexForge project", e);
		}
	}

	public List<DexForgeClass> getClasses() {
		return DexForgeNodeFactory.wrapClasses(engine.getRawClasses(), engine);
	}

	public List<DexForgePackage> getPackages() {
		return DexForgeNodeFactory.wrapPackages(engine.getRawPackages(), engine);
	}

	public List<DexForgeResourceFile> getResources() {
		List<?> resources = engine.getRawResources();
		if (resources.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeResourceFile> result = new ArrayList<>(resources.size());
		for (Object resourceFile : resources) {
			result.add(new DexForgeResourceFile(resourceFile, engine));
		}
		return Collections.unmodifiableList(result);
	}

	public dexforge.api.model.DexForgeApkMetadata getApkMetadata() {
		return engine.getApkMetadata();
	}

	public DexForgeClass searchClassByAlias(String fullName) {
		Object cls = engine.searchClass(fullName);
		return cls == null ? null : DexForgeNodeFactory.wrapClass(cls, engine);
	}

	public int getErrorsCount() { return 0; }
	public int getWarningsCount() { return 0; }
	public DexForgeSettings getSettings() { return settings; }
	public List<File> getInputFiles() { return inputFiles; }

	public DexForgeProjectState getState() {
		DexForgeProjectState state = new DexForgeProjectState();
		state.setName("Project " + System.currentTimeMillis()); // TODO: add name to DexForgeProject
		state.setEngineId(engine.getEngineId());
		state.setInputFiles(inputFiles.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
		state.setFingerprint(engine.calculateFingerprint());
		state.setRenameHistory(new ArrayList<>(renameManager.getHistory()));
		state.setLastModified(System.currentTimeMillis());
		return state;
	}

	public DexForgeIntegrityStatus verifyIntegrity(DexForgeProjectState storedState) {
		java.util.Map<String, String> currentFingerprint = engine.calculateFingerprint();
		java.util.Map<String, String> storedFingerprint = storedState.getFingerprint();

		List<String> modified = new ArrayList<>();
		for (java.util.Map.Entry<String, String> entry : storedFingerprint.entrySet()) {
			String currentHash = currentFingerprint.get(entry.getKey());
			if (currentHash == null || !currentHash.equals(entry.getValue())) {
				modified.add(entry.getKey());
			}
		}

		return new DexForgeIntegrityStatus(modified.isEmpty(), modified);
	}

	public void save(File file, DexForgeProjectStore store) throws IOException {
		store.save(getState(), file);
		eventBus.publish(new dexforge.api.event.DexForgeProjectEvent(this, dexforge.api.event.DexForgeProjectEvent.Type.SAVED));
	}

	/**
	 * Loads a previously saved project state into this active project.
	 * This triggers Rename Recovery.
	 */
	public void loadState(DexForgeProjectState state) {
		Objects.requireNonNull(state);
		// Apply renames
		if (!state.getRenameHistory().isEmpty()) {
			renameManager.loadHistory(state.getRenameHistory());
		}
		// In the future, we can load bookmarks, comments, etc.
	}

	public DexForgeEngine getEngine() {
		return engine;
	}

	@Override
	public void close() {
		engine.close();
		eventBus.publish(new dexforge.api.event.DexForgeProjectEvent(this, dexforge.api.event.DexForgeProjectEvent.Type.CLOSED));
	}
}
