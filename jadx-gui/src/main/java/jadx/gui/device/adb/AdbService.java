package jadx.gui.device.adb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.gui.device.adb.parsers.AdbDevicesParser;
import jadx.gui.device.adb.parsers.AdbUserParser;
import jadx.gui.device.adb.parsers.ApkPathParser;
import jadx.gui.device.adb.parsers.PackageListParser;

public class AdbService {
	private static final Logger LOG = LoggerFactory.getLogger(AdbService.class);

	public static class AdbUser {
		private final int id;
		private final String name;

		public AdbUser(int id, String name) {
			this.id = id;
			this.name = name;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return id + ": " + name;
		}
	}

	public static String detectAdbPath() {
		String adbName = System.getProperty("os.name").toLowerCase().contains("win") ? "adb.exe" : "adb";

		String androidHome = System.getenv("ANDROID_HOME");
		if (androidHome != null && !androidHome.isEmpty()) {
			File f = new File(androidHome, "platform-tools" + File.separator + adbName);
			if (f.exists()) {
				return f.getAbsolutePath();
			}
		}

		String androidSdkRoot = System.getenv("ANDROID_SDK_ROOT");
		if (androidSdkRoot != null && !androidSdkRoot.isEmpty()) {
			File f = new File(androidSdkRoot, "platform-tools" + File.separator + adbName);
			if (f.exists()) {
				return f.getAbsolutePath();
			}
		}

		String home = System.getProperty("user.home");
		if (home != null) {
			File f = new File(home, "Library/Android/sdk/platform-tools/" + adbName);
			if (f.exists()) {
				return f.getAbsolutePath();
			}
		}

		String pathEnv = System.getenv("PATH");
		if (pathEnv != null) {
			String[] paths = pathEnv.split(File.pathSeparator);
			for (String path : paths) {
				File f = new File(path, adbName);
				if (f.exists()) {
					return f.getAbsolutePath();
				}
			}
		}

		return adbName;
	}

	public static List<ADBDevice> listDevices(String host, int port) throws IOException {
		ensureAdbServer(host, port);
		String cmd = "host:devices-l";
		String prefixedCmd = String.format("%04x%s", cmd.length(), cmd);
		byte[] response;
		try (Socket socket = ADB.connect(host, port)) {
			response = ADB.exec(prefixedCmd, socket.getOutputStream(), socket.getInputStream());
		}
		if (response == null) {
			return Collections.emptyList();
		}
		String output = new String(response, ADB.ADB_CHARSET);
		return AdbDevicesParser.parse(output, host, port);
	}

	private static void ensureAdbServer(String host, int port) throws IOException {
		if (ADB.isServerRunning(host, port)) {
			return;
		}
		if (!isLocalAdbHost(host)) {
			throw new IOException("ADB server is not running at " + host + ':' + port);
		}
		String adbPath = detectAdbPath();
		LOG.info("ADB server is not running at {}:{}, trying to start it with {}", host, port, adbPath);
		try {
			boolean started = ADB.startServer(adbPath, port);
			if (!started || !ADB.isServerRunning(host, port)) {
				throw new IOException("ADB server is not running at " + host + ':' + port
						+ " and could not be started using: " + adbPath);
			}
		} catch (IOException e) {
			throw new IOException("ADB server is not running at " + host + ':' + port
					+ ". Start it manually with `" + adbPath + " start-server` or check the ADB path in settings.", e);
		}
	}

	private static boolean isLocalAdbHost(String host) {
		return "localhost".equalsIgnoreCase(host)
				|| "127.0.0.1".equals(host)
				|| "::1".equals(host)
				|| "0:0:0:0:0:0:0:1".equals(host);
	}

	public static String execShell(ADBDevice device, String command) throws IOException {
		ADBDeviceInfo info = device.getDeviceInfo();
		try (Socket socket = ADB.connect(info.getAdbHost(), info.getAdbPort())) {
			byte[] bytes = ADB.execShellCommandRaw(device.getSerial(), command, socket.getOutputStream(), socket.getInputStream());
			if (bytes == null) {
				throw new IOException("Failed to execute shell command: " + command);
			}
			return new String(bytes, ADB.ADB_CHARSET);
		}
	}

	public static List<AdbUser> listUsers(ADBDevice device) throws IOException {
		String output = execShell(device, "pm list users");
		return parseUsers(output);
	}

	public static List<AdbUser> parseUsers(String output) {
		return AdbUserParser.parse(output);
	}

	public static List<AdbPackage> listPackages(ADBDevice device, int userId, String filterType) throws IOException {
		String cmd = "pm list packages -f --user " + userId;
		String output = execShell(device, cmd);

		if (output.contains("SecurityException") || output.contains("Permission Denial") || output.startsWith("Error:")) {
			throw new IOException("Permission denied listing packages for user " + userId + ": " + summarizeShellError(output));
		}

		return parsePackages(output, filterType);
	}

	private static String summarizeShellError(String output) {
		for (String line : output.split("\n")) {
			String trimmed = line.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			if (trimmed.contains("SecurityException")
					|| trimmed.contains("Permission Denial")
					|| trimmed.startsWith("Error:")) {
				return trimmed;
			}
		}
		String trimmed = output.trim();
		int firstLineEnd = trimmed.indexOf('\n');
		return firstLineEnd == -1 ? trimmed : trimmed.substring(0, firstLineEnd).trim();
	}

	public static List<AdbPackage> parsePackages(String output, String filterType) {
		return PackageListParser.parse(output, filterType);
	}

	public static List<ApkPath> resolveApkPaths(ADBDevice device, String packageName, int userId) throws IOException {
		String cmd = "pm path " + packageName + " --user " + userId;
		String output = execShell(device, cmd);
		if (output.trim().isEmpty() || output.contains("Error") || output.contains("not found")) {
			output = execShell(device, "pm path " + packageName);
		}
		return parseApkPaths(output);
	}

	public static List<ApkPath> parseApkPaths(String output) {
		return ApkPathParser.parse(output);
	}

	public static void pullApk(String adbPath, ADBDevice device, String remotePath, File localFile) throws IOException, AdbException {
		File parent = localFile.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}

		List<String> command = new ArrayList<>();
		command.add(adbPath);
		command.add("-s");
		command.add(device.getSerial());
		command.add("pull");
		command.add(remotePath);
		command.add(localFile.getAbsolutePath());

		LOG.info("Running ADB command: {}", String.join(" ", command));

		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);
		java.lang.Process process = pb.start();

		StringBuilder output = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}
		}

		try {
			int exitCode = process.waitFor();
			if (exitCode != 0) {
				throw new AdbException("ADB pull failed with exit code " + exitCode + ". Output:\n" + output.toString().trim());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AdbException("ADB pull command interrupted", e);
		}
	}
}
