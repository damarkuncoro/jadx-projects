package dexforge.application.usecase;

import dexforge.application.port.EventPublisher;
import dexforge.application.port.ProjectRepository;
import dexforge.domain.model.project.Project;
import dexforge.domain.model.project.ProjectId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CloseProjectUseCaseTest {
	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private EventPublisher eventPublisher;

	private CloseProjectUseCase useCase;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		useCase = new CloseProjectUseCase(projectRepository, eventPublisher);
	}

	@Test
	public void testExecuteSuccessfully() throws Exception {
		// Given
		ProjectId projectId = ProjectId.of("/test-project");
		Project mockProject = mock(Project.class);

		when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
		when(eventPublisher.publishAll(anyList())).thenReturn(CompletableFuture.completedFuture(null));
		when(mockProject.getUncommittedEvents()).thenReturn(List.of());

		// When
		useCase.execute(projectId);

		// Then
		verify(projectRepository, times(1)).findById(projectId);
		verify(mockProject, times(1)).close();
	}

	@Test
	public void testExecuteProjectNotFound() {
		// Given
		ProjectId projectId = ProjectId.of("/nonexistent");

		when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

		// When & Then: throws ProjectNotOpenException
		assertThatThrownBy(() -> useCase.execute(projectId))
				.isInstanceOf(ProjectNotOpenException.class)
				.hasMessageContaining("Project not found");
	}
}