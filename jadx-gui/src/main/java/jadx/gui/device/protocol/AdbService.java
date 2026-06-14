package jadx.gui.device.protocol;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdbService {
	private static final Logger LOG = LoggerFactory.getLogger(AdbService.class);

	private static final Pattern USER_PATTERN = Pattern.compile("UserInfo\\{(\\d+):([^:]+):\\d+\\}");

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

		// 1. ANDROID_HOME environment variable
		String androidHome = System.getenv("ANDROID_HOME");
		if (androidHome != null && !androidHome.isEmpty()) {
			File f = new File(androidHome, "platform-tools" + File.separator + adbName);
			if (f.exists()) {
				return f.getAbsolutePath();
			}
		}

		// 2. ANDROID_SDK_ROOT environment variable
		String androidSdkRoot = System.getenv("ANDROID_SDK_ROOT");
		if (androidSdkRoot != null && !androidSdkRoot.isEmpty()) {
			File f = new File(androidSdkRoot, "platform-tools" + File.separator + adbName);
			if (f.exists()) {
				return f.getAbsolutePath();
			}
		}

		// 3. User's local Library directory on macOS
		String home = System.getProperty("user.home");
		if (home != null) {
			File f = new File(home, "Library/Android/sdk/platform-tools/" + adbName);
			if (f.exists()) {
				return f.getAbsolutePath();
			}
		}

		// 4. Default system PATH search
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

		return adbName; // Fallback
	}

	public static List<ADBDevice> listDevices(String host, int port) throws IOException {
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
		List<AdbUser> users = new ArrayList<>();
		for (String line : output.split("\n")) {
			line = line.trim();
			Matcher m = USER_PATTERN.matcher(line);
			if (m.find()) {
				int id = Integer.parseInt(m.group(1));
				String name = m.group(2);
				users.add(new AdbUser(id, name));
			}
		}
		if (users.isEmpty()) {
			users.add(new AdbUser(0, "Owner"));
		}
		return users;
	}

	public static List<AdbPackage> listPackages(ADBDevice device, int userId, String filterType) throws IOException {
		String cmd = "pm list packages -f --user " + userId;
		String output = execShell(device, cmd);

		if (output.contains("SecurityException") || output.contains("Permission Denial") || output.startsWith("Error:")) {
			throw new IOException("Permission denied listing packages for user " + userId + ": " + output.trim());
		}

		return parsePackages(output, filterType);
	}

	public static List<AdbPackage> parsePackages(String output, String filterType) {
		List<AdbPackage> packages = new ArrayList<>();
		for (String line : output.split("\n")) {
			line = line.trim();
			if (line.isEmpty() || !line.startsWith("package:")) {
				continue;
			}
			int lastEq = line.lastIndexOf('=');
			if (lastEq > 0) {
				String rawPath = line.substring("package:".length(), lastEq);
				String pkgName = line.substring(lastEq + 1);

				AdbPackage pkg = new AdbPackage(pkgName, rawPath);
				boolean include = false;
				if ("all".equalsIgnoreCase(filterType)) {
					include = true;
				} else if ("system".equalsIgnoreCase(filterType)) {
					include = pkg.isSystem();
				} else if ("user".equalsIgnoreCase(filterType)) {
					include = !pkg.isSystem();
				}
				if (include) {
					packages.add(pkg);
				}
			}
		}
		return packages;
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
		List<ApkPath> paths = new ArrayList<>();
		for (String line : output.split("\n")) {
			line = line.trim();
			if (line.startsWith("package:")) {
				paths.add(new ApkPath(line.substring("package:".length())));
			}
		}
		return paths;
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
