package dexforge.application.dto;

import java.util.Objects;

import dexforge.domain.model.project.ProjectId;
import dexforge.domain.model.search.SearchType;

/**
 * DTO: Request untuk Search Code use case.
 */
public class SearchCodeRequest {
	private final String queryText;
	private final SearchType searchType;
	private final ProjectId projectId;

	public SearchCodeRequest(String queryText, SearchType searchType, ProjectId projectId) {
		this.queryText = Objects.requireNonNull(queryText, "Query text cannot be null");
		this.searchType = Objects.requireNonNull(searchType, "Search type cannot be null");
		this.projectId = projectId;
	}

	public String getQueryText() {
		return queryText;
	}

	public SearchType getSearchType() {
		return searchType;
	}

	public ProjectId getProjectId() {
		return projectId;
	}
}
