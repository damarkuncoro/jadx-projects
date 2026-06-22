package dexforge.application.usecase;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dexforge.application.dto.SearchCodeRequest;
import dexforge.application.port.EventPublisher;
import dexforge.domain.model.project.ProjectId;
import dexforge.domain.model.search.SearchQuery;
import dexforge.domain.model.search.SearchType;
import dexforge.domain.service.SearchService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SearchCodeUseCaseTest {
	@Mock
	private SearchService searchService;

	@Mock
	private EventPublisher eventPublisher;

	private SearchCodeUseCase useCase;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		useCase = new SearchCodeUseCase(searchService, eventPublisher);
	}

	@Test
	public void testExecuteSuccessfully() throws Exception {
		// Given
		SearchCodeRequest request = new SearchCodeRequest("testQuery", SearchType.CLASS, ProjectId.of("/test"));
		SearchQuery mockQuery = mock(SearchQuery.class);

		when(searchService.search(anyString(), any(SearchType.class), any(ProjectId.class)))
				.thenReturn(mockQuery);
		when(eventPublisher.publishAll(anyList()))
				.thenReturn(CompletableFuture.completedFuture(null));

		// When
		SearchQuery result = useCase.execute(request);

		// Then
		assertThat(result).isEqualTo(mockQuery);
		verify(searchService, times(1)).search("testQuery", SearchType.CLASS, ProjectId.of("/test"));
	}

	@Test
	public void testExecuteWithServiceError() throws Exception {
		// Given
		SearchCodeRequest request = new SearchCodeRequest("testQuery", SearchType.CLASS, ProjectId.of("/test"));

		when(searchService.search(anyString(), any(SearchType.class), any(ProjectId.class)))
				.thenThrow(new RuntimeException("Search error"));

		// When & Then: throws SearchFailedException
		assertThatThrownBy(() -> useCase.execute(request))
				.isInstanceOf(SearchFailedException.class)
				.hasMessageContaining("Search failed");
	}

	@Test
	public void testExecuteWithEmptyQuery() throws Exception {
		// Given
		SearchCodeRequest request = new SearchCodeRequest("", SearchType.CLASS, ProjectId.of("/test"));
		SearchQuery mockQuery = mock(SearchQuery.class);

		when(searchService.search(anyString(), any(SearchType.class), any(ProjectId.class)))
				.thenReturn(mockQuery);
		when(eventPublisher.publishAll(anyList()))
				.thenReturn(CompletableFuture.completedFuture(null));

		// When
		SearchQuery result = useCase.execute(request);

		// Then
		assertThat(result).isEqualTo(mockQuery);
		verify(searchService, times(1)).search("", SearchType.CLASS, ProjectId.of("/test"));
	}
}
