package jadx.gui.device.cli.dto;

public final class DeviceDto {
	private final String serial;
	private final String status;
	private final String model;
	private final String androidVersion;

	public DeviceDto(String serial, String status, String model, String androidVersion) {
		this.serial = serial;
		this.status = status;
		this.model = model;
		this.androidVersion = androidVersion;
	}

	public String getSerial() {
		return serial;
	}

	public String getStatus() {
		return status;
	}

	public String getModel() {
		return model;
	}

	public String getAndroidVersion() {
		return androidVersion;
	}
}
