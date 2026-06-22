package dexforge.api.core;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.api.diagnostic.DexForgeDiagnostic;
import dexforge.api.diagnostic.DexForgeDiagnostic;
import dexforge.api.diagnostic.DexForgeDiagnosticManager;
import dexforge.api.event.DexForgeEventBus;
import dexforge.api.exception.DexForgeException;
import dexforge.api.model.DexForgeClass;
import dexforge.api.model.DexForgeNodeFactory;
import dexforge.api.model.DexForgePackage;
import dexforge.api.query.DexForgeSearch;
import dexforge.api.rename.DexForgeRenameManager;
import dexforge.api.resource.DexForgeResourceFile;
import dexforge.core.infrastructure.jadx.JadxDecompilerHelper;

/**
 * Domain Aggregate representing a decompilation project.
 */
public final class DexForgeProject implements Closeable {
	private final Object delegate;
	private final DexForgeSettings settings;
	private final DexForgeEventBus eventBus = new DexForgeEventBusImpl();
	private final DexForgeRenameManager renameManager = new DexForgeRenameManagerImpl(this);
	private final DexForgeDiagnosticManager diagnosticManager = new DexForgeDiagnosticManagerImpl(this);
	private DexForgeSearch search;

	DexForgeProject(Object delegate, DexForgeSettings settings) {
		this.delegate = Objects.requireNonNull(delegate);
		this.settings = Objects.requireNonNull(settings);
	}

	/**
	 * Access the Event Bus for this project.
	 */
	public DexForgeEventBus events() {
		return eventBus;
	}

	/**
	 * Access the Rename Manager for semantic refactoring.
	 */
	public DexForgeRenameManager renames() {
		return renameManager;
	}

	/**
	 * Access the Diagnostic Manager for analysis and errors.
	 */
	public DexForgeDiagnosticManager diagnostics() {
		return diagnosticManager;
	}

	/**
	 * Access the modern Query API for this project.
	 */
	public DexForgeSearch search() {
		if (search == null) {
			search = new DexForgeSearch(this);
		}
		return search;
	}

	public void load() {
		try {
			JadxDecompilerHelper.load(delegate);
			eventBus.publish(new dexforge.api.event.DexForgeProjectEvent(this, dexforge.api.event.DexForgeProjectEvent.Type.LOADED));
		} catch (RuntimeException e) {
			throw new DexForgeException("LOAD_FAILED", "Failed to load DexForge project", e);
		}
	}

	public void save() {
		try {
			JadxDecompilerHelper.save(delegate);
			eventBus.publish(new dexforge.api.event.DexForgeProjectEvent(this, dexforge.api.event.DexForgeProjectEvent.Type.SAVED));
		} catch (RuntimeException e) {
			throw new DexForgeException("SAVE_FAILED", "Failed to save decompiled output", e);
		}
	}

	public List<DexForgeClass> getClasses() {
		return DexForgeNodeFactory.wrapClasses(JadxDecompilerHelper.getClasses(delegate));
	}

	public List<DexForgeClass> getClassesWithInners() {
		return DexForgeNodeFactory.wrapClasses(JadxDecompilerHelper.getClassesWithInners(delegate));
	}

	public List<DexForgePackage> getPackages() {
		return DexForgeNodeFactory.wrapPackages(JadxDecompilerHelper.getPackages(delegate));
	}

	public List<DexForgeResourceFile> getResources() {
		List<?> resources = JadxDecompilerHelper.getResources(delegate);
		if (resources.isEmpty()) {
			return Collections.emptyList();
		}
		List<DexForgeResourceFile> result = new ArrayList<>(resources.size());
		for (Object resourceFile : resources) {
			result.add(new DexForgeResourceFile(resourceFile));
		}
		return Collections.unmodifiableList(result);
	}

	public DexForgeClass searchClassByOriginalName(String fullName) {
		Object cls = JadxDecompilerHelper.searchClassByOrigFullName(delegate, fullName);
		return cls == null ? null : new DexForgeClass(cls);
	}

	public DexForgeClass searchClassByAlias(String fullName) {
		Object cls = JadxDecompilerHelper.searchClassByAliasFullName(delegate, fullName);
		return cls == null ? null : new DexForgeClass(cls);
	}

	public int getErrorsCount() {
		return JadxDecompilerHelper.getErrorsCount(delegate);
	}

	public int getWarningsCount() {
		return JadxDecompilerHelper.getWarningsCount(delegate);
	}

	public DexForgeSettings getSettings() {
		return settings;
	}

	@Override
	public void close() {
		JadxDecompilerHelper.close(delegate);
		eventBus.publish(new dexforge.api.event.DexForgeProjectEvent(this, dexforge.api.event.DexForgeProjectEvent.Type.CLOSED));
	}

	/**
	 * bridge kept for internal use.
	 */
	@Deprecated(forRemoval = false)
	public Object unwrap() {
		return delegate;
	}
}
