package dexforge.application.usecase;

import java.nio.file.Paths;
import java.util.concurrent.CompletionException;

import dexforge.application.dto.OpenProjectRequest;
import dexforge.application.port.EventPublisher;
import dexforge.application.port.NotificationPort;
import dexforge.application.port.ProjectRepository;
import dexforge.domain.model.project.Project;
import dexforge.domain.model.project.ProjectConfig;
import dexforge.domain.model.project.ProjectId;

/**
 * Use Case: OpenProjectUseCase
 * Orchestrates opening a project for decompilation.
 */
public class OpenProjectUseCase {
	private final ProjectRepository projectRepository;
	private final NotificationPort notificationPort;
	private final EventPublisher eventPublisher;

	public OpenProjectUseCase(ProjectRepository projectRepository,
			NotificationPort notificationPort,
			EventPublisher eventPublisher) {
		this.projectRepository = projectRepository;
		this.notificationPort = notificationPort;
		this.eventPublisher = eventPublisher;
	}

	public void execute(OpenProjectRequest request) throws ProjectNotOpenException {
		try {
			ProjectId projectId = ProjectId.of(request.getProjectPath());
			if (projectRepository.existsById(projectId)) {
				throw new ProjectNotOpenException("Project already exists: " + projectId);
			}
			ProjectConfig config = ProjectConfig.create(request.getProjectName(), request.getDescription());
			Project project = Project.create(projectId, config, Paths.get(request.getProjectPath()));

			project.open();
			projectRepository.save(project);

			// Publish domain events
			eventPublisher.publishAll(project.getUncommittedEvents()).join();
			project.markEventsAsCommitted();

			notificationPort.notifySuccess("Project opened: " + request.getProjectName());
		} catch (ProjectNotOpenException e) {
			throw e;
		} catch (CompletionException e) {
			throw new ProjectNotOpenException("Failed to open project: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new ProjectNotOpenException("Failed to open project: " + e.getMessage(), e);
		}
	}
}
