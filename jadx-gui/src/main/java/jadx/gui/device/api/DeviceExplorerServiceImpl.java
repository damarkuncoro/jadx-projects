package jadx.gui.device.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.gui.device.adb.ADBDevice;
import jadx.gui.device.adb.ADBDeviceInfo;
import jadx.gui.device.adb.AdbClient;
import jadx.gui.device.adb.AdbException;
import jadx.gui.device.adb.AdbPackage;
import jadx.gui.device.adb.AdbService;
import jadx.gui.device.adb.ShellAdbClient;
import jadx.gui.device.workspace.DexForgeWorkspaceLayout;

public class DeviceExplorerServiceImpl implements DeviceExplorerService {
	private static final Logger LOG = LoggerFactory.getLogger(DeviceExplorerServiceImpl.class);

	private final AdbClient adbClient;

	public DeviceExplorerServiceImpl() {
		this(new ShellAdbClient());
	}

	public DeviceExplorerServiceImpl(AdbClient adbClient) {
		this.adbClient = adbClient;
	}

	private ADBDevice findDevice(String serial) throws IOException, DeviceExplorerException {
		List<ADBDevice> devices = adbClient.listDevices();
		for (ADBDevice device : devices) {
			if (device.getSerial().equals(serial)) {
				return device;
			}
		}
		throw new DeviceExplorerException("Device with serial '" + serial + "' not found.");
	}

	@Override
	public List<DeviceInfo> listDevices() throws IOException {
		List<ADBDevice> devices = adbClient.listDevices();
		List<DeviceInfo> result = new ArrayList<>();
		for (ADBDevice device : devices) {
			ADBDeviceInfo info = device.getDeviceInfo();
			result.add(new DeviceInfo(info.getAllInfo(), info.getAdbHost(), info.getAdbPort()));
		}
		return result;
	}

	@Override
	public List<AndroidUser> listUsers(String serial) throws IOException, DeviceExplorerException {
		ADBDevice device = findDevice(serial);
		List<AdbService.AdbUser> users = adbClient.listUsers(device);
		List<AndroidUser> result = new ArrayList<>();
		for (AdbService.AdbUser user : users) {
			result.add(new AndroidUser(user.getId(), user.getName()));
		}
		return result;
	}

	@Override
	public List<AndroidPackage> listPackages(String serial, int userId, String filterType)
			throws IOException, DeviceExplorerException {
		ADBDevice device = findDevice(serial);
		List<AdbPackage> packages;
		try {
			packages = adbClient.listPackages(device, userId, filterType);
		} catch (IOException e) {
			if (userId != 0) {
				LOG.warn("Failed to list packages for user {} ({}), falling back to user 0", userId, e.getMessage().trim());
				packages = adbClient.listPackages(device, 0, filterType);
			} else {
				throw e;
			}
		}
		List<AndroidPackage> result = new ArrayList<>();
		for (AdbPackage pkg : packages) {
			result.add(new AndroidPackage(pkg.getPackageName(), pkg.getPath()));
		}
		return result;
	}

	@Override
	public List<ApkPath> resolveApkPaths(String serial, String packageName, int userId)
			throws IOException, DeviceExplorerException {
		ADBDevice device = findDevice(serial);
		List<jadx.gui.device.adb.ApkPath> paths;
		try {
			paths = adbClient.resolveApkPaths(device, packageName, userId);
		} catch (IOException e) {
			if (userId != 0) {
				LOG.warn("Failed to resolve APK paths for user {} ({}), falling back to user 0", userId, e.getMessage().trim());
				paths = adbClient.resolveApkPaths(device, packageName, 0);
			} else {
				throw e;
			}
		}
		List<ApkPath> result = new ArrayList<>();
		for (jadx.gui.device.adb.ApkPath path : paths) {
			result.add(new ApkPath(path.getRemotePath()));
		}
		return result;
	}

	@Override
	public PullResult pullApk(String serial, String packageName, String outDir, int userId)
			throws IOException, DeviceExplorerException {
		ADBDevice device = findDevice(serial);
		List<ApkPath> paths = resolveApkPaths(serial, packageName, userId);
		if (paths.isEmpty()) {
			throw new DeviceExplorerException("Could not find any APK paths for package: " + packageName);
		}

		DexForgeWorkspaceLayout layout = new DexForgeWorkspaceLayout(outDir);
		File apksDir = layout.getApksDir();
		if (!apksDir.exists()) {
			apksDir.mkdirs();
		}

		for (ApkPath path : paths) {
			File localFile = new File(apksDir, path.getLocalName());
			try {
				adbClient.pullApk(device, path.getRemotePath(), localFile);
			} catch (AdbException e) {
				throw new DeviceExplorerException("ADB pull failed: " + e.getMessage(), e);
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
