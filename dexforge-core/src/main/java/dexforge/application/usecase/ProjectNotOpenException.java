package dexforge.application.usecase;

/**
 * Exception: Project Not Open
 */
public class ProjectNotOpenException extends Exception {
	public ProjectNotOpenException(String message) {
		super(message);
	}

	public ProjectNotOpenException(String message, Throwable cause) {
		super(message, cause);
	}
}
