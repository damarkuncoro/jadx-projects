package jadx.gui.device.adb;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class ShellAdbClient implements AdbClient {
	private final String host;
	private final int port;

	public ShellAdbClient() {
		this("localhost", 5037);
	}

	public ShellAdbClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public List<ADBDevice> listDevices() throws IOException {
		return AdbService.listDevices(host, port);
	}

	@Override
	public List<AdbService.AdbUser> listUsers(ADBDevice device) throws IOException {
		return AdbService.listUsers(device);
	}

	@Override
	public List<AdbPackage> listPackages(ADBDevice device, int userId, String filterType) throws IOException {
		return AdbService.listPackages(device, userId, filterType);
	}

	@Override
	public List<ApkPath> resolveApkPaths(ADBDevice device, String packageName, int userId) throws IOException {
		return AdbService.resolveApkPaths(device, packageName, userId);
	}

	@Override
	public void pullApk(ADBDevice device, String remotePath, File localFile) throws IOException, AdbException {
		AdbService.pullApk(AdbService.detectAdbPath(), device, remotePath, localFile);
	}
}
