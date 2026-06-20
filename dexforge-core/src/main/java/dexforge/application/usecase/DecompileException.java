package dexforge.application.usecase;

/**
 * Exception: Decompile Failed
 */
public class DecompileException extends Exception {
	public DecompileException(String message) {
		super(message);
	}

	public DecompileException(String message, Throwable cause) {
		super(message, cause);
	}
}
