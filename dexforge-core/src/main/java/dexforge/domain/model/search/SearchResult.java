package dexforge.domain.model.search;

import java.util.Objects;

/**
 * Value Object: Search Result
 */
public final class SearchResult {
	private final String fullName;
	private final String fileName;
	private final int lineNumber;
	private final String matchType;

	private SearchResult(String fullName, String fileName, int lineNumber, String matchType) {
		this.fullName = Objects.requireNonNull(fullName, "Full name cannot be null");
		this.fileName = Objects.requireNonNull(fileName, "File name cannot be null");
		this.lineNumber = lineNumber;
		this.matchType = Objects.requireNonNull(matchType, "Match type cannot be null");
	}

	public static SearchResult of(String fullName, String fileName, int lineNumber, String matchType) {
		return new SearchResult(fullName, fileName, lineNumber, matchType);
	}

	public String getFullName() {
		return fullName;
	}

	public String getFileName() {
		return fileName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getMatchType() {
		return matchType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SearchResult that = (SearchResult) o;
		return lineNumber == that.lineNumber
				&& fullName.equals(that.fullName)
				&& fileName.equals(that.fileName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fullName, fileName, lineNumber);
	}

	@Override
	public String toString() {
		return "SearchResult{"
				+ "fullName='" + fullName + '\''
				+ ", line=" + lineNumber
				+ '}';
	}
}
