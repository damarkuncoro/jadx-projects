package dexforge.application.usecase;

/**
 * Exception: Export Failed
 */
public class ExportException extends Exception {
	public ExportException(String message) {
		super(message);
	}

	public ExportException(String message, Throwable cause) {
		super(message, cause);
	}
}
