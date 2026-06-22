package dexforge.api.exception;

public class DexForgeException extends RuntimeException {
	private final String code;

	public DexForgeException(String code, String message) {
		super(message);
		this.code = code;
	}

	public DexForgeException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
