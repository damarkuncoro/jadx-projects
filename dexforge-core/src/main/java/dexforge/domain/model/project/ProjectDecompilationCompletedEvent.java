package dexforge.domain.model.project;

import java.time.LocalDateTime;

import dexforge.domain.event.DomainEvent;

/**
 * Domain Event: Project Decompilation Completed
 */
public class ProjectDecompilationCompletedEvent implements DomainEvent {
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
	public LocalDateTime getOccurredAt() {
		return LocalDateTime.ofInstant(
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
