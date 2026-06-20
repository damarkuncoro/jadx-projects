package dexforge.application.usecase;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dexforge.application.dto.OpenProjectRequest;
import dexforge.application.port.EventPublisher;
import dexforge.application.port.NotificationPort;
import dexforge.application.port.ProjectRepository;
import dexforge.domain.model.project.Project;
import dexforge.domain.model.project.ProjectId;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests untuk OpenProjectUseCase.
 * Menunjukkan how use case orchestrates domain objects dan ports.
 *
 * Tests ini tidak depend pada UI atau infrastructure - pure business logic testing.
 */
public class OpenProjectUseCaseTest {
	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private EventPublisher eventPublisher;

	@Mock
	private NotificationPort notificationPort;

	private OpenProjectUseCase useCase;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		useCase = new OpenProjectUseCase(projectRepository, notificationPort, eventPublisher);
	}

	@Test
	public void testExecuteSuccessfully() throws Exception {
		// Given
		OpenProjectRequest request = new OpenProjectRequest(
				"/path/to/project",
				"My Project",
				"Test project");

		// Mock: project tidak ada
		when(projectRepository.existsById(any(ProjectId.class))).thenReturn(false);

		// Mock: event publisher returns completed future
		when(eventPublisher.publishAll(anyList()))
				.thenReturn(CompletableFuture.completedFuture(null));

		// When
		useCase.execute(request);

		// Then
		verify(projectRepository, times(1)).save(any(Project.class));
		verify(eventPublisher, times(1)).publishAll(anyList());
		verify(notificationPort, times(1)).notifySuccess(anyString());
	}

	@Test
	public void testExecuteProjectAlreadyExists() {
		// Given
		OpenProjectRequest request = new OpenProjectRequest(
				"/path/to/project",
				"My Project",
				"Test project");

		// Mock: project sudah ada
		when(projectRepository.existsById(any(ProjectId.class))).thenReturn(true);

		// When / Then
		assertThatThrownBy(() -> useCase.execute(request))
				.isInstanceOf(ProjectNotOpenException.class)
				.hasMessageContaining("already exists");

		verify(projectRepository, times(0)).save(any());
		verify(eventPublisher, times(0)).publishAll(anyList());
		verify(notificationPort, times(0)).notifySuccess(anyString());
	}

	@Test
	public void testExecuteWithEmptyPath() {
		// Given
		OpenProjectRequest request = new OpenProjectRequest(
				"",
				"My Project",
				"Test project");

		// When / Then
		assertThatThrownBy(() -> useCase.execute(request))
				.isInstanceOf(ProjectNotOpenException.class)
				.hasMessageContaining("Failed");

		verify(projectRepository, times(0)).save(any());
		verify(notificationPort, times(0)).notifyError(anyString());
	}

	@Test
	public void testExecuteWithEventPublishingError() {
		// Given
		OpenProjectRequest request = new OpenProjectRequest(
				"/path/to/project",
				"My Project",
				"Test project");

		when(projectRepository.existsById(any(ProjectId.class))).thenReturn(false);

		// Mock: event publisher throws error
		CompletableFuture<Void> failedFuture = new CompletableFuture<>();
		failedFuture.completeExceptionally(new RuntimeException("Event bus error"));
		when(eventPublisher.publishAll(anyList())).thenReturn(failedFuture);

		// When / Then
		assertThatThrownBy(() -> useCase.execute(request))
				.isInstanceOf(ProjectNotOpenException.class)
				.hasMessageContaining("Failed");

		verify(projectRepository, times(1)).save(any(Project.class));
		verify(notificationPort, times(0)).notifySuccess(anyString());
	}
}
