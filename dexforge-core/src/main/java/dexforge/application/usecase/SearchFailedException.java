package dexforge.application.usecase;

/**
 * Exception: Search Failed
 */
public class SearchFailedException extends Exception {
	public SearchFailedException(String message) {
		super(message);
	}

	public SearchFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
