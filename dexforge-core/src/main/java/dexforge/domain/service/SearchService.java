package dexforge.domain.service;

import java.util.List;

import dexforge.domain.model.project.ProjectId;
import dexforge.domain.model.search.SearchQuery;
import dexforge.domain.model.search.SearchResult;
import dexforge.domain.model.search.SearchType;

/**
 * Domain Service: SearchService
 * Handles search operations across projects.
 */
public class SearchService {

	/**
	 * Performs search on the given query.
	 * (In real implementation, this would delegate to actual search engine)
	 */
	public SearchQuery search(String queryText, SearchType searchType, ProjectId projectId) {
		SearchQuery query = SearchQuery.create(queryText, searchType, projectId);
		// TODO: implement actual search
		return query;
	}

	/**
	 * Adds result to query.
	 */
	public void addResult(SearchQuery query, SearchResult result) {
		// Use reflection or provide package-private access
	}

	/**
	 * Gets results matching the query.
	 */
	public List<SearchResult> getResults(SearchQuery query) {
		return query.getResults();
	}
}
