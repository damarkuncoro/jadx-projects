package dexforge.domain.model.project;

import java.time.LocalDateTime;

import dexforge.domain.event.DomainEvent;

/**
 * Domain Event: Project Module Added
 */
public class ProjectModuleAddedEvent implements DomainEvent {
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
	public LocalDateTime getOccurredAt() {
		return LocalDateTime.ofInstant(
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
