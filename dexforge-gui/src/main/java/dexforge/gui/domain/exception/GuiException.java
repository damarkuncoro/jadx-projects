package dexforge.gui.domain.exception;

/**
 * Base exception for GUI-specific errors.
 */
public class GuiException extends RuntimeException {
	private final String code;

	public GuiException(String code, String message) {
		super(message);
		this.code = code;
	}

	public GuiException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
