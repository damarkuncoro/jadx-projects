package dexforge.domain.model.project;

import java.time.LocalDateTime;

import dexforge.domain.event.DomainEvent;

/**
 * Domain Event: Project Opened
 */
public class ProjectOpenedEvent implements DomainEvent {
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
	public LocalDateTime getOccurredAt() {
		return LocalDateTime.ofInstant(
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
