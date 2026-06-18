package jadx.gui.device.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MockDeviceExplorerService implements DeviceExplorerService {

	private final List<DeviceInfo> devices = new ArrayList<>();

	public MockDeviceExplorerService() {
		devices.add(new DeviceInfo("mock-device-1 device product:Mock_Pixel_6 model:Mock_Pixel_6 device:mock-device-1 transport_id:1",
				"localhost", 5037));
		devices.add(new DeviceInfo("mock-device-2 offline product:Mock_Pixel_5 model:Mock_Pixel_5 device:mock-device-2 transport_id:2",
				"localhost", 5037));
		devices.add(new DeviceInfo("mock-device-3 unauthorized product:Mock_Pixel_4 model:Mock_Pixel_4 device:mock-device-3 transport_id:3",
				"localhost", 5037));
	}

	private void checkDevice(String serial) throws DeviceExplorerException {
		for (DeviceInfo dev : devices) {
			if (dev.getSerial().equals(serial)) {
				if ("offline".equals(dev.getState())) {
					throw new DeviceExplorerException(DeviceExplorerException.DeviceExplorerErrorCode.DEVICE_OFFLINE,
							"Device is offline: " + serial);
				}
				if ("unauthorized".equals(dev.getState())) {
					throw new DeviceExplorerException(DeviceExplorerException.DeviceExplorerErrorCode.UNAUTHORIZED,
							"Device is unauthorized: " + serial);
				}
				return;
			}
		}
		throw new DeviceExplorerException(DeviceExplorerException.DeviceExplorerErrorCode.ADB_NOT_FOUND, "Device not found: " + serial);
	}

	@Override
	public List<DeviceInfo> listDevices() throws IOException {
		return devices;
	}

	@Override
	public List<AndroidUser> listUsers(String serial) throws IOException, DeviceExplorerException {
		checkDevice(serial);
		return List.of(
				new AndroidUser(0, "Owner"),
				new AndroidUser(10, "Work"));
	}

	@Override
	public List<AndroidPackage> listPackages(String serial, int userId, String filterType) throws IOException, DeviceExplorerException {
		checkDevice(serial);
		if (userId != 0 && userId != 10) {
			throw new DeviceExplorerException(DeviceExplorerException.DeviceExplorerErrorCode.INTERNAL_ERROR, "User not found: " + userId);
		}

		List<AndroidPackage> result = new ArrayList<>();
		AndroidPackage pkg1 = new AndroidPackage("com.mock.app1", "/data/app/com.mock.app1/base.apk");
		AndroidPackage pkg2 = new AndroidPackage("com.mock.app2", "/data/app/com.mock.app2/base.apk");
		AndroidPackage sysPkg = new AndroidPackage("com.android.settings", "/system/priv-app/Settings/Settings.apk");

		if ("all".equalsIgnoreCase(filterType)) {
			result.add(pkg1);
			result.add(pkg2);
			result.add(sysPkg);
		} else if ("system".equalsIgnoreCase(filterType)) {
			result.add(sysPkg);
		} else if ("user".equalsIgnoreCase(filterType)) {
			result.add(pkg1);
			result.add(pkg2);
		}
		return result;
	}

	@Override
	public List<ApkPath> resolveApkPaths(String serial, String packageName, int userId) throws IOException, DeviceExplorerException {
		checkDevice(serial);
		if (packageName.equals("com.mock.app1")) {
			return List.of(
					new ApkPath("/data/app/com.mock.app1/base.apk"),
					new ApkPath("/data/app/com.mock.app1/split_config.arm64_v8a.apk"));
		} else if (packageName.equals("com.mock.app2")) {
			return List.of(new ApkPath("/data/app/com.mock.app2/base.apk"));
		} else if (packageName.equals("com.android.settings")) {
			return List.of(new ApkPath("/system/priv-app/Settings/Settings.apk"));
		}
		throw new DeviceExplorerException(DeviceExplorerException.DeviceExplorerErrorCode.PACKAGE_NOT_FOUND,
				"Package not found: " + packageName);
	}

	@Override
	public PullResult pullApk(String serial, String packageName, String outDir, int userId) throws IOException, DeviceExplorerException {
		checkDevice(serial);
		List<ApkPath> paths = resolveApkPaths(serial, packageName, userId);

		File out = new File(outDir);
		if (!out.exists()) {
			out.mkdirs();
		}

		for (ApkPath path : paths) {
			File f = new File(out, path.getLocalName());
			try (FileWriter fw = new FileWriter(f)) {
				fw.write("Simulated APK content");
			}
		}
		return new PullResult(paths);
	}

	@Override
	public PullResult pullAndDecompile(String serial, String packageName, String outDir, int userId)
			throws IOException, DeviceExplorerException {
		return pullApk(serial, packageName, outDir, userId);
	}
}
