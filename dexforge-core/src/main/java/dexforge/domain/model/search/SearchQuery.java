package dexforge.domain.model.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.domain.model.AggregateRoot;
import dexforge.domain.model.project.ProjectId;

/**
 * Aggregate Root: SearchQuery
 * Represents query pencarian dengan hasil.
 */
public class SearchQuery extends AggregateRoot {
	private final String queryText;
	private final SearchType searchType;
	private final List<SearchResult> results;

	private SearchQuery(SearchQueryId id, String queryText, SearchType searchType, ProjectId projectId) {
		super(id);
		this.queryText = Objects.requireNonNull(queryText, "Query text cannot be null");
		this.searchType = Objects.requireNonNull(searchType, "Search type cannot be null");
		this.results = new ArrayList<>();
	}

	public static SearchQuery create(String queryText, SearchType searchType, ProjectId projectId) {
		SearchQueryId id = SearchQueryId.of(queryText + ":" + projectId.getValue());
		return new SearchQuery(id, queryText, searchType, projectId);
	}

	public void addResult(SearchResult result) {
		Objects.requireNonNull(result, "Result cannot be null");
		results.add(result);
	}

	public SearchQueryId getSearchQueryId() {
		return (SearchQueryId) id;
	}

	public String getQueryText() {
		return queryText;
	}

	public SearchType getSearchType() {
		return searchType;
	}

	public List<SearchResult> getResults() {
		return Collections.unmodifiableList(results);
	}

	public int getResultCount() {
		return results.size();
	}
}
