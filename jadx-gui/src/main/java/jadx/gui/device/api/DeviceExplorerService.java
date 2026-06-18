package jadx.gui.device.api;

import java.io.IOException;
import java.util.List;

public interface DeviceExplorerService {
	List<DeviceInfo> listDevices() throws IOException;

	List<AndroidUser> listUsers(String serial) throws IOException, DeviceExplorerException;

	List<AndroidPackage> listPackages(String serial, int userId, String filterType) throws IOException, DeviceExplorerException;

	List<ApkPath> resolveApkPaths(String serial, String packageName, int userId) throws IOException, DeviceExplorerException;

	PullResult pullApk(String serial, String packageName, String outDir, int userId) throws IOException, DeviceExplorerException;

	PullResult pullAndDecompile(String serial, String packageName, String outDir, int userId) throws IOException, DeviceExplorerException;
}
