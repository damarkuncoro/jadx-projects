package dexforge.domain.model.project;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.domain.event.DomainEvent;
import dexforge.domain.model.AggregateRoot;

/**
 * Aggregate Root: Project
 *
 * Project adalah aggregate root yang merepresentasikan satu application/APK yang di-decompile.
 * Semua operasi terhadap project harus go through aggregate ini (tidak boleh modify entities di
 * dalamnya langsung).
 *
 * DDD Principles:
 * - Immutable state ketika possible
 * - Domain logic di-encapsulate (bukan hanya data holder)
 * - Raise domain events untuk state changes
 * - Enforce invariants (misalnya: tidak bisa add module ke closed project)
 */
public class Project extends AggregateRoot {
	private final ProjectConfig config;
	private final Path rootPath;
	private ProjectStatus status;
	private final List<ProjectModule> modules;
	private final long createdAt;
	private long openedAt;
	private long closedAt;

	private Project(ProjectId id, ProjectConfig config, Path rootPath) {
		super(id);
		this.config = Objects.requireNonNull(config, "ProjectConfig cannot be null");
		this.rootPath = Objects.requireNonNull(rootPath, "Root path cannot be null");
		this.status = ProjectStatus.CREATED;
		this.modules = new ArrayList<>();
		this.createdAt = System.currentTimeMillis();
		this.openedAt = 0;
		this.closedAt = 0;
	}

	/**
	 * Factory method: create new project.
	 */
	public static Project create(ProjectId id, ProjectConfig config, Path rootPath) {
		Objects.requireNonNull(id, "ProjectId cannot be null");
		Objects.requireNonNull(config, "ProjectConfig cannot be null");
		Objects.requireNonNull(rootPath, "Root path cannot be null");

		return new Project(id, config, rootPath);
	}

	/**
	 * Factory method: create project dari path string.
	 */
	public static Project create(String projectPath, String projectName, String description) {
		ProjectId id = ProjectId.of(projectPath);
		ProjectConfig config = ProjectConfig.create(projectName, description);
		Path path = Paths.get(projectPath);

		return new Project(id, config, path);
	}

	/**
	 * Domain Logic: Open project.
	 * Invariant: Hanya project dengan status CREATED yang bisa di-open.
	 */
	public void open() {
		if (status != ProjectStatus.CREATED) {
			throw new IllegalStateException(
					String.format("Cannot open project in %s state", status));
		}

		this.status = ProjectStatus.OPENED;
		this.openedAt = System.currentTimeMillis();

		// Raise domain event
		raise(new ProjectOpenedEvent((ProjectId) id, config));
	}

	/**
	 * Domain Logic: Close project.
	 * Invariant: Hanya open project yang bisa di-close.
	 */
	public void close() {
		if (!status.isOpen()) {
			throw new IllegalStateException(
					String.format("Cannot close project in %s state", status));
		}

		ProjectStatus previousStatus = status;
		this.status = ProjectStatus.CLOSED;
		this.closedAt = System.currentTimeMillis();

		raise(new ProjectClosedEvent((ProjectId) id, previousStatus));
	}

	/**
	 * Domain Logic: Start decompilation.
	 * Invariant: Project must be OPENED.
	 */
	public void startDecompilation() {
		if (status != ProjectStatus.OPENED) {
			throw new IllegalStateException(
					String.format("Cannot decompile project in %s state", status));
		}

		this.status = ProjectStatus.DECOMPILING;
		raise(new ProjectDecompilationStartedEvent((ProjectId) id));
	}

	/**
	 * Domain Logic: Complete decompilation.
	 * Invariant: Project must be DECOMPILING.
	 */
	public void completeDecompilation() {
		if (status != ProjectStatus.DECOMPILING) {
			throw new IllegalStateException(
					String.format("Cannot complete decompilation in %s state", status));
		}

		this.status = ProjectStatus.DECOMPILED;
		raise(new ProjectDecompilationCompletedEvent((ProjectId) id, modules.size()));
	}

	/**
	 * Domain Logic: Add module ke project.
	 * Invariant: Project must be open.
	 */
	public void addModule(ProjectModule module) {
		Objects.requireNonNull(module, "Module cannot be null");

		if (!status.isOpen()) {
			throw new IllegalStateException(
					String.format("Cannot add module to project in %s state", status));
		}

		if (modules.stream().anyMatch(m -> m.getName().equals(module.getName()))) {
			throw new IllegalStateException(
					String.format("Module with name '%s' already exists", module.getName()));
		}

		modules.add(module);
		raise(new ProjectModuleAddedEvent((ProjectId) id, module.getName()));
	}

	// Getters - immutable views
	public ProjectId getProjectId() {
		return (ProjectId) id;
	}

	public ProjectConfig getConfig() {
		return config;
	}

	public Path getRootPath() {
		return rootPath;
	}

	public ProjectStatus getStatus() {
		return status;
	}

	public List<ProjectModule> getModules() {
		return Collections.unmodifiableList(modules);
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public long getOpenedAt() {
		return openedAt;
	}

	public long getClosedAt() {
		return closedAt;
	}

	public int getModuleCount() {
		return modules.size();
	}

	@Override
	public String toString() {
		return "Project{"
				+ "id=" + id
				+ ", name='" + config.getName() + '\''
				+ ", status=" + status
				+ ", modules=" + modules.size()
				+ ", rootPath=" + rootPath
				+ '}';
	}

	// ===== Domain Events =====

	public static class ProjectOpenedEvent implements DomainEvent {
		private final ProjectId projectId;
		private final ProjectConfig config;
		private final long occurredAtMs;

		public ProjectOpenedEvent(ProjectId projectId, ProjectConfig config) {
			this.projectId = projectId;
			this.config = config;
			this.occurredAtMs = System.currentTimeMillis();
		}

		@Override
		public String getEventType() {
			return "ProjectOpened";
		}

		@Override
		public java.time.LocalDateTime getOccurredAt() {
			return java.time.LocalDateTime.ofInstant(
					java.time.Instant.ofEpochMilli(occurredAtMs),
					java.time.ZoneId.systemDefault());
		}

		public ProjectId getProjectId() {
			return projectId;
		}

		public ProjectConfig getConfig() {
			return config;
		}
	}

	public static class ProjectClosedEvent implements DomainEvent {
		private final ProjectId projectId;
		private final ProjectStatus previousStatus;
		private final long occurredAtMs;

		public ProjectClosedEvent(ProjectId projectId, ProjectStatus previousStatus) {
			this.projectId = projectId;
			this.previousStatus = previousStatus;
			this.occurredAtMs = System.currentTimeMillis();
		}

		@Override
		public String getEventType() {
			return "ProjectClosed";
		}

		@Override
		public java.time.LocalDateTime getOccurredAt() {
			return java.time.LocalDateTime.ofInstant(
					java.time.Instant.ofEpochMilli(occurredAtMs),
					java.time.ZoneId.systemDefault());
		}

		public ProjectId getProjectId() {
			return projectId;
		}

		public ProjectStatus getPreviousStatus() {
			return previousStatus;
		}
	}

	public static class ProjectDecompilationStartedEvent implements DomainEvent {
		private final ProjectId projectId;
		private final long occurredAtMs;

		public ProjectDecompilationStartedEvent(ProjectId projectId) {
			this.projectId = projectId;
			this.occurredAtMs = System.currentTimeMillis();
		}

		@Override
		public String getEventType() {
			return "ProjectDecompilationStarted";
		}

		@Override
		public java.time.LocalDateTime getOccurredAt() {
			return java.time.LocalDateTime.ofInstant(
					java.time.Instant.ofEpochMilli(occurredAtMs),
					java.time.ZoneId.systemDefault());
		}

		public ProjectId getProjectId() {
			return projectId;
		}
	}

	public static class ProjectDecompilationCompletedEvent implements DomainEvent {
		private final ProjectId projectId;
		private final int moduleCount;
		private final long occurredAtMs;

		public ProjectDecompilationCompletedEvent(ProjectId projectId, int moduleCount) {
			this.projectId = projectId;
			this.moduleCount = moduleCount;
			this.occurredAtMs = System.currentTimeMillis();
		}

		@Override
		public String getEventType() {
			return "ProjectDecompilationCompleted";
		}

		@Override
		public java.time.LocalDateTime getOccurredAt() {
			return java.time.LocalDateTime.ofInstant(
					java.time.Instant.ofEpochMilli(occurredAtMs),
					java.time.ZoneId.systemDefault());
		}

		public ProjectId getProjectId() {
			return projectId;
		}

		public int getModuleCount() {
			return moduleCount;
		}
	}

	public static class ProjectModuleAddedEvent implements DomainEvent {
		private final ProjectId projectId;
		private final String moduleName;
		private final long occurredAtMs;

		public ProjectModuleAddedEvent(ProjectId projectId, String moduleName) {
			this.projectId = projectId;
			this.moduleName = moduleName;
			this.occurredAtMs = System.currentTimeMillis();
		}

		@Override
		public String getEventType() {
			return "ProjectModuleAdded";
		}

		@Override
		public java.time.LocalDateTime getOccurredAt() {
			return java.time.LocalDateTime.ofInstant(
					java.time.Instant.ofEpochMilli(occurredAtMs),
					java.time.ZoneId.systemDefault());
		}

		public ProjectId getProjectId() {
			return projectId;
		}

		public String getModuleName() {
			return moduleName;
		}
	}
}
