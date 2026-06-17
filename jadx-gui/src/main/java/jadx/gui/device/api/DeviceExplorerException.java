package jadx.gui.device.api;

public class DeviceExplorerException extends Exception {
	private static final long serialVersionUID = 1L;

	public enum DeviceExplorerErrorCode {
		ADB_NOT_FOUND,
		DEVICE_OFFLINE,
		UNAUTHORIZED,
		PACKAGE_NOT_FOUND,
		PULL_FAILED,
		INTERNAL_ERROR
	}

	private final DeviceExplorerErrorCode errorCode;

	public DeviceExplorerException(String message) {
		this(DeviceExplorerErrorCode.INTERNAL_ERROR, message);
	}

	public DeviceExplorerException(String message, Throwable cause) {
		this(DeviceExplorerErrorCode.INTERNAL_ERROR, message, cause);
	}

	public DeviceExplorerException(DeviceExplorerErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public DeviceExplorerException(DeviceExplorerErrorCode errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public DeviceExplorerErrorCode getErrorCode() {
		return errorCode;
	}
}
