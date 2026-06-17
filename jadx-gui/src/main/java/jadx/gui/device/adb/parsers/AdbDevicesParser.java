package jadx.gui.device.adb.parsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jadx.gui.device.adb.ADBDevice;
import jadx.gui.device.adb.ADBDeviceInfo;

public final class AdbDevicesParser {
	private AdbDevicesParser() {}

	public static List<ADBDevice> parse(String output, String host, int port) {
		if (output == null || output.trim().isEmpty()) {
			return Collections.emptyList();
		}
		List<ADBDevice> devices = new ArrayList<>();
		for (String line : output.split("\n")) {
			line = line.trim();
			if (!line.isEmpty()) {
				ADBDeviceInfo info = new ADBDeviceInfo(line, host, port);
				devices.add(new ADBDevice(info));
			}
		}
		return devices;
	}
}
