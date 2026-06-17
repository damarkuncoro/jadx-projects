package jadx.gui.device.adb;

public class AdbException extends Exception {
	private static final long serialVersionUID = 1L;

	public AdbException(String message) {
		super(message);
	}

	public AdbException(String message, Throwable cause) {
		super(message, cause);
	}
}
