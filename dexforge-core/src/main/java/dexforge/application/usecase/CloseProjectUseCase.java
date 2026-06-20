package dexforge.application.usecase;

import dexforge.application.port.EventPublisher;
import dexforge.application.port.ProjectRepository;
import dexforge.domain.event.DomainEvent;
import dexforge.domain.model.project.Project;
import dexforge.domain.model.project.ProjectId;

import java.util.List;

/**
 * Use Case: CloseProjectUseCase
 * Orchestrates closing a project.
 */
@UseCase
public class CloseProjectUseCase {
	private final ProjectRepository projectRepository;
	private final EventPublisher eventPublisher;

	public CloseProjectUseCase(ProjectRepository projectRepository,
			EventPublisher eventPublisher) {
		this.projectRepository = projectRepository;
		this.eventPublisher = eventPublisher;
	}

	public void execute(ProjectId projectId) throws ProjectNotOpenException {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ProjectNotOpenException("Project not found: " + projectId));

		project.close();
		List<DomainEvent> events = project.getUncommittedEvents();
		if (!events.isEmpty()) {
			eventPublisher.publishAll(events).join();
		}
		project.markEventsAsCommitted();
	}
}
