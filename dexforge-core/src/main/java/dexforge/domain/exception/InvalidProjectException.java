package dexforge.domain.exception;

/**
 * Exception saat project tidak valid atau tidak bisa dibuka.
 */
public class InvalidProjectException extends DomainException {
	public InvalidProjectException(String message) {
		super(message);
	}

	public InvalidProjectException(String message, Throwable cause) {
		super(message, cause);
	}
}
