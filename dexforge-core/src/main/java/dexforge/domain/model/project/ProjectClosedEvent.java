package dexforge.domain.model.project;

import java.time.LocalDateTime;

import dexforge.domain.event.DomainEvent;

/**
 * Domain Event: Project Closed
 */
public class ProjectClosedEvent implements DomainEvent {
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
	public LocalDateTime getOccurredAt() {
		return LocalDateTime.ofInstant(
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
