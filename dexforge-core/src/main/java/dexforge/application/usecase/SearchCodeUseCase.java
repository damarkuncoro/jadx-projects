package dexforge.application.usecase;

import dexforge.application.dto.SearchCodeRequest;
import dexforge.application.port.EventPublisher;
import dexforge.domain.model.search.SearchQuery;
import dexforge.domain.service.SearchService;

/**
 * Use Case: SearchCodeUseCase
 * Orchestrates code search operations.
 */
@UseCase
public class SearchCodeUseCase {
	private final SearchService searchService;
	private final EventPublisher eventPublisher;

	public SearchCodeUseCase(SearchService searchService,
			EventPublisher eventPublisher) {
		this.searchService = searchService;
		this.eventPublisher = eventPublisher;
	}

	public SearchQuery execute(SearchCodeRequest request) throws SearchFailedException {
		try {
			SearchQuery query = searchService.search(
					request.getQueryText(),
					request.getSearchType(),
					request.getProjectId());
			return query;
		} catch (Exception e) {
			throw new SearchFailedException("Search failed: " + e.getMessage(), e);
		}
	}
}
