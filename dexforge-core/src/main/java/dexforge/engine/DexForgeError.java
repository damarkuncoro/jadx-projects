package dexforge.engine;

import java.util.Objects;

/**
 * Public structured error for DexForge API and JSON consumers.
 */
public final class DexForgeError {
	private final String code;
	private final String message;

	private DexForgeError(String code, String message) {
		this.code = Objects.requireNonNull(code, "Code cannot be null");
		this.message = Objects.requireNonNull(message, "Message cannot be null");
	}

	public static DexForgeError of(String code, String message) {
		return new DexForgeError(code, message);
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
