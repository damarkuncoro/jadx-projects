package dexforge.infrastructure.adapter;

import dexforge.application.port.EventPublisher;
import dexforge.application.port.NotificationPort;
import dexforge.application.port.ProjectRepository;
import dexforge.application.usecase.CloseProjectUseCase;
import dexforge.application.usecase.OpenProjectUseCase;
import dexforge.application.usecase.SearchCodeUseCase;
import dexforge.core.application.decompile.DecompileApplicationService;
import dexforge.core.infrastructure.jadx.JadxDecompilerEngine;
import dexforge.domain.service.ProjectService;
import dexforge.domain.service.SearchService;
import dexforge.infrastructure.adapter.jadx.JadxProjectRepositoryAdapter;

import jadx.api.JadxArgs;

/**
 * Application Context: Dependency Injection container
 * Wires together all components for the application.
 */
public class ApplicationContext {
	private final ProjectRepository projectRepository;
	private final EventPublisher eventPublisher;
	private final NotificationPort notificationPort;

	private final ProjectService projectService;
	private final SearchService searchService;
	private final DecompileApplicationService decompileService;

	private final OpenProjectUseCase openProjectUseCase;
	private final CloseProjectUseCase closeProjectUseCase;
	private final SearchCodeUseCase searchCodeUseCase;

	public ApplicationContext(JadxArgs jadxArgs) {
		// Repositories
		this.projectRepository = new JadxProjectRepositoryAdapter();

		// Publishers
		this.eventPublisher = new SimpleEventPublisher();
		this.notificationPort = new SwingNotificationAdapter();

		// Services
		this.projectService = new ProjectService();
		this.searchService = new SearchService();
		this.decompileService = new DecompileApplicationService(new JadxDecompilerEngine(jadxArgs));

		// Use Cases
		this.openProjectUseCase = new OpenProjectUseCase(projectRepository, notificationPort, eventPublisher);
		this.closeProjectUseCase = new CloseProjectUseCase(projectRepository, eventPublisher);
		this.searchCodeUseCase = new SearchCodeUseCase(searchService, eventPublisher);
	}

	// Getters for use cases
	public OpenProjectUseCase getOpenProjectUseCase() {
		return openProjectUseCase;
	}

	public CloseProjectUseCase getCloseProjectUseCase() {
		return closeProjectUseCase;
	}

	public SearchCodeUseCase getSearchCodeUseCase() {
		return searchCodeUseCase;
	}

	// Getters for services
	public ProjectService getProjectService() {
		return projectService;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public DecompileApplicationService getDecompileService() {
		return decompileService;
	}

	// Getters for ports
	public ProjectRepository getProjectRepository() {
		return projectRepository;
	}

	public EventPublisher getEventPublisher() {
		return eventPublisher;
	}

	public NotificationPort getNotificationPort() {
		return notificationPort;
	}
}
