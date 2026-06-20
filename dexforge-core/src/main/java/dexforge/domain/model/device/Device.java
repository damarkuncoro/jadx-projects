package dexforge.domain.model.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dexforge.domain.model.AggregateRoot;

/**
 * Aggregate Root: Device
 * Represents connected Android device.
 */
public class Device extends AggregateRoot {
	private final String serial;
	private final String model;
	private final String androidVersion;
	private final DeviceStatus status;
	private final List<String> users;

	private Device(String serial, String model, String androidVersion) {
		super(null); // Will be handled by package
		this.serial = Objects.requireNonNull(serial);
		this.model = model != null ? model : "Unknown";
		this.androidVersion = androidVersion != null ? androidVersion : "Unknown";
		this.status = DeviceStatus.CONNECTED;
		this.users = new ArrayList<>();
	}

	public static Device connected(String serial, String model, String androidVersion) {
		return new Device(serial, model, androidVersion);
	}

	public String getSerial() {
		return serial;
	}

	public String getModel() {
		return model;
	}

	public String getAndroidVersion() {
		return androidVersion;
	}

	public DeviceStatus getStatus() {
		return status;
	}

	public List<String> getUsers() {
		return Collections.unmodifiableList(users);
	}

	public void addUser(int userId) {
		users.add(String.valueOf(userId));
	}
}
