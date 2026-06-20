package dexforge.domain.exception;

/**
 * Base exception untuk domain layer.
 * Semua domain-specific exceptions harus extend class ini.
 */
public class DomainException extends RuntimeException {
	public DomainException(String message) {
		super(message);
	}

	public DomainException(String message, Throwable cause) {
		super(message, cause);
	}
}
