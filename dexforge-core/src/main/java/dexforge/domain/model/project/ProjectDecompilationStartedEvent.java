package dexforge.domain.model.project;

import java.time.LocalDateTime;

import dexforge.domain.event.DomainEvent;

/**
 * Domain Event: Project Decompilation Started
 */
public class ProjectDecompilationStartedEvent implements DomainEvent {
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
	public LocalDateTime getOccurredAt() {
		return LocalDateTime.ofInstant(
				java.time.Instant.ofEpochMilli(occurredAtMs),
				java.time.ZoneId.systemDefault());
	}

	public ProjectId getProjectId() {
		return projectId;
	}
}
