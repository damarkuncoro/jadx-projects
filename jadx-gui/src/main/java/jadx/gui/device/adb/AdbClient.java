package jadx.gui.device.adb;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface AdbClient {
	List<ADBDevice> listDevices() throws IOException;

	List<AdbService.AdbUser> listUsers(ADBDevice device) throws IOException;

	List<AdbPackage> listPackages(ADBDevice device, int userId, String filterType) throws IOException;

	List<ApkPath> resolveApkPaths(ADBDevice device, String packageName, int userId) throws IOException;

	void pullApk(ADBDevice device, String remotePath, File localFile) throws IOException, AdbException;
}
