package dexforge.domain.model.search;

import dexforge.domain.model.EntityId;

/**
 * Value Object: Search Query ID
 */
public final class SearchQueryId extends EntityId<String> {
	private SearchQueryId(String value) {
		super(value);
	}

	public static SearchQueryId of(String id) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("SearchQueryId cannot be empty");
		}
		return new SearchQueryId(id);
	}
}
