package jadx.gui.device.protocol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.impl.NoOpCodeCache;
import jadx.api.impl.SimpleCodeWriter;
import jadx.api.usage.impl.EmptyUsageInfoCache;
import jadx.cli.plugins.JadxFilesGetter;
import jadx.gui.device.protocol.AdbService.AdbUser;
import jadx.plugins.tools.JadxExternalPluginsLoader;

public class DeviceExplorerCLI {
	private static final Logger LOG = LoggerFactory.getLogger(DeviceExplorerCLI.class);

	public static void main(String[] args) {
		if (args.length < 2) {
			printUsage();
			System.exit(1);
		}

		String command = args[1];
		try {
			switch (command) {
				case "list-devices":
					executeListDevices();
					break;
				case "list-users":
					if (args.length < 3) {
						System.err.println("Error: Missing device serial.");
						printUsage();
						System.exit(1);
					}
					executeListUsers(args[2]);
					break;
				case "list-packages":
					if (args.length < 4) {
						System.err.println("Error: Missing device serial and user ID.");
						printUsage();
						System.exit(1);
					}
					String filter = args.length >= 5 ? args[4] : "all";
					executeListPackages(args[2], Integer.parseInt(args[3]), filter);
					break;
				case "paths":
					if (args.length < 4) {
						System.err.println("Error: Missing device serial and package name.");
						printUsage();
						System.exit(1);
					}
					executePaths(args[2], args[3]);
					break;
				case "pull":
					if (args.length < 5) {
						System.err.println("Error: Missing device serial, package name, and output directory.");
						printUsage();
						System.exit(1);
					}
					int userIdPull = args.length >= 6 ? Integer.parseInt(args[5]) : 0;
					executePull(args[2], args[3], args[4], userIdPull);
					break;
				case "pull-and-decompile":
					if (args.length < 5) {
						System.err.println("Error: Missing device serial, package name, and output directory.");
						printUsage();
						System.exit(1);
					}
					int userIdDecompile = args.length >= 6 ? Integer.parseInt(args[5]) : 0;
					executePullAndDecompile(args[2], args[3], args[4], userIdDecompile);
					break;
				default:
					System.err.println("Unknown command: " + command);
					printUsage();
					System.exit(1);
			}
		} catch (Exception e) {
			System.err.println("Error executing command '" + command + "': " + e.getMessage());
			LOG.error("Command execution failed", e);
			System.exit(1);
		}
	}

	private static void printUsage() {
		System.out.println("JADX Device Explorer CLI Helper Usage:");
		System.out.println("  ./gradlew :jadx-gui:run --args=\"device-explorer list-devices\"");
		System.out.println("  ./gradlew :jadx-gui:run --args=\"device-explorer list-users <serial>\"");
		System.out.println(
				"  ./gradlew :jadx-gui:run --args=\"device-explorer list-packages <serial> <user_id> [filter]\" (filters: all, user, system)");
		System.out.println("  ./gradlew :jadx-gui:run --args=\"device-explorer paths <serial> <package_name>\"");
		System.out.println("  ./gradlew :jadx-gui:run --args=\"device-explorer pull <serial> <package_name> <out_dir> [user_id]\"");
		System.out.println(
				"  ./gradlew :jadx-gui:run --args=\"device-explorer pull-and-decompile <serial> <package_name> <out_dir> [user_id]\"");
	}

	private static ADBDevice findDevice(String serial) throws IOException, AdbException {
		List<ADBDevice> devices = AdbService.listDevices("localhost", 5037);
		for (ADBDevice device : devices) {
			if (device.getSerial().equals(serial)) {
				return device;
			}
		}
		throw new AdbException("Device with serial '" + serial + "' not found.");
	}

	private static void executeListDevices() throws IOException {
		System.out.println("[*] Fetching connected devices...");
		List<ADBDevice> devices = AdbService.listDevices("localhost", 5037);
		if (devices.isEmpty()) {
			System.out.println("No devices connected.");
			return;
		}
		for (ADBDevice device : devices) {
			ADBDeviceInfo info = device.getDeviceInfo();
			System.out.printf("Device: %s | Serial: %s | State: %s\n", info.getModel(), info.getSerial(), info.getState());
		}
	}

	private static void executeListUsers(String serial) throws IOException, AdbException {
		ADBDevice device = findDevice(serial);
		System.out.println("[*] Fetching users for device: " + serial);
		List<AdbUser> users = AdbService.listUsers(device);
		for (AdbUser user : users) {
			System.out.printf("User ID: %d | Name: %s\n", user.getId(), user.getName());
		}
	}

	private static void executeListPackages(String serial, int userId, String filter) throws IOException, AdbException {
		ADBDevice device = findDevice(serial);
		System.out.printf("[*] Fetching packages for device: %s, User ID: %d, Filter: %s\n", serial, userId, filter);
		List<AdbPackage> packages;
		try {
			packages = AdbService.listPackages(device, userId, filter);
		} catch (IOException e) {
			if (userId != 0) {
				System.err.println("[!] Warning: Failed to list packages for user " + userId + " (" + e.getMessage().trim()
						+ "). Falling back to user 0.");
				packages = AdbService.listPackages(device, 0, filter);
			} else {
				throw e;
			}
		}
		if (packages.isEmpty()) {
			System.out.println("No packages found.");
			return;
		}
		for (AdbPackage pkg : packages) {
			System.out.println(pkg.getPackageName() + " (" + (pkg.isSystem() ? "system" : "user") + ")");
		}
	}

	private static void executePaths(String serial, String packageName) throws IOException, AdbException {
		ADBDevice device = findDevice(serial);
		System.out.printf("[*] Resolving APK paths for package '%s'...\n", packageName);
		List<ApkPath> paths = AdbService.resolveApkPaths(device, packageName, 0);
		if (paths.isEmpty()) {
			System.out.println("No paths resolved.");
			return;
		}
		for (ApkPath path : paths) {
			System.out.printf("Type: %-8s | Path: %s\n", path.getType(), path.getRemotePath());
		}
	}

	private static List<ApkPath> executePull(String serial, String packageName, String outDir, int userId)
			throws IOException, AdbException {
		ADBDevice device = findDevice(serial);
		String adbPath = AdbService.detectAdbPath();
		System.out.printf("[*] Pulling APKs for package '%s' to '%s'...\n", packageName, outDir);

		List<ApkPath> paths;
		try {
			paths = AdbService.resolveApkPaths(device, packageName, userId);
		} catch (IOException e) {
			if (userId != 0) {
				System.err.println("[!] Warning: Failed to resolve APK paths for user " + userId + " (" + e.getMessage().trim()
						+ "). Falling back to user 0.");
				paths = AdbService.resolveApkPaths(device, packageName, 0);
			} else {
				throw e;
			}
		}
		if (paths.isEmpty()) {
			throw new AdbException("Could not find any APK paths for package: " + packageName);
		}

		File apksDir = new File(outDir, "apks");
		if (!apksDir.exists()) {
			apksDir.mkdirs();
		}

		for (ApkPath path : paths) {
			File localFile = new File(apksDir, path.getLocalName());
			System.out.printf("Pulling: %s -> %s\n", path.getRemotePath(), localFile.getAbsolutePath());
			AdbService.pullApk(adbPath, device, path.getRemotePath(), localFile);
		}

		// Write metadata pull-report.json
		File reportsDir = new File(outDir, "reports");
		if (!reportsDir.exists()) {
			reportsDir.mkdirs();
		}

		Map<String, Object> report = new LinkedHashMap<>();
		report.put("packageName", packageName);
		report.put("deviceSerial", serial);
		report.put("androidUser", userId);
		report.put("pulledAt", java.time.Instant.now().toString());

		List<Map<String, Object>> apkFilesList = new ArrayList<>();
		for (ApkPath path : paths) {
			File localFile = new File(apksDir, path.getLocalName());
			Map<String, Object> apkInfo = new LinkedHashMap<>();
			apkInfo.put("remotePath", path.getRemotePath());
			apkInfo.put("localPath", "apks/" + path.getLocalName());
			apkInfo.put("sizeBytes", localFile.exists() ? localFile.length() : 0);
			apkInfo.put("type", path.getType());
			apkFilesList.add(apkInfo);
		}
		report.put("apkFiles", apkFilesList);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		File reportFile = new File(reportsDir, "pull-report.json");
		try (FileWriter writer = new FileWriter(reportFile)) {
			gson.toJson(report, writer);
		}
		System.out.println("[*] Metadata pull report saved to " + reportFile.getAbsolutePath());

		return paths;
	}

	private static void executePullAndDecompile(String serial, String packageName, String outDir, int userId)
			throws IOException, AdbException {
		List<ApkPath> paths = executePull(serial, packageName, outDir, userId);

		File apksDir = new File(outDir, "apks");
		List<File> apkFiles = new ArrayList<>();
		for (ApkPath path : paths) {
			File file = new File(apksDir, path.getLocalName());
			if (file.exists()) {
				apkFiles.add(file);
			}
		}

		if (apkFiles.isEmpty()) {
			System.err.println("Error: No APK files were pulled.");
			return;
		}

		File jadxOutputDir = new File(outDir, "jadx-output");
		long startTime = System.currentTimeMillis();
		decompileApks(apkFiles, jadxOutputDir);
		long duration = System.currentTimeMillis() - startTime;

		// Write decompile-report.json (Milestone v0.4)
		File reportsDir = new File(outDir, "reports");
		if (!reportsDir.exists()) {
			reportsDir.mkdirs();
		}

		Map<String, Object> report = new LinkedHashMap<>();
		report.put("outputPath", jadxOutputDir.getAbsolutePath());
		report.put("durationMs", duration);
		report.put("status", "COMPLETE");
		report.put("jobsSkipped", 0);

		List<String> inputs = new ArrayList<>();
		for (File f : apkFiles) {
			inputs.add(f.getName());
		}
		report.put("inputApkFiles", inputs);
		report.put("generatedAt", java.time.Instant.now().toString());

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		File reportFile = new File(reportsDir, "decompile-report.json");
		try (FileWriter writer = new FileWriter(reportFile)) {
			gson.toJson(report, writer);
		}
		System.out.println("[*] Metadata decompilation report saved to " + reportFile.getAbsolutePath());

		File assistantReportFile = new File(reportsDir, "assistant-report.json");
		System.out.println("[*] Running Security Assistant analysis...");
		try {
			DeviceExplorerAssistant.runAnalysis(jadxOutputDir, assistantReportFile);
			System.out.println("[*] Security Assistant report saved to " + assistantReportFile.getAbsolutePath());
		} catch (Exception e) {
			System.err.println("[!] Security Assistant analysis failed: " + e.getMessage());
		}
	}

	private static void decompileApks(List<File> apkFiles, File outDir) {
		JadxArgs jadxArgs = new JadxArgs();
		jadxArgs.setInputFiles(apkFiles);
		jadxArgs.setOutDir(outDir);
		jadxArgs.setShowInconsistentCode(true);
		jadxArgs.setInlineMethods(false);
		jadxArgs.setAllowInlineKotlinLambda(false);
		jadxArgs.setReplaceConsts(false);

		jadxArgs.setPluginLoader(new JadxExternalPluginsLoader());
		jadxArgs.setFilesGetter(JadxFilesGetter.INSTANCE);
		jadxArgs.setCodeCache(new NoOpCodeCache());
		jadxArgs.setUsageInfoCache(new EmptyUsageInfoCache());
		jadxArgs.setCodeWriterProvider(SimpleCodeWriter::new);

		System.out.println("[*] Starting decompilation...");
		try (JadxDecompiler decompiler = new JadxDecompiler(jadxArgs)) {
			decompiler.load();
			decompiler.save(500, (done, total) -> {
				int progress = (int) (done * 100.0 / total);
				System.out.printf("INFO  - progress: %d of %d (%d%%)\r", done, total, progress);
			});
			System.out.println("\n[*] Decompilation finished. Saved to: " + outDir.getAbsolutePath());
		} catch (Exception e) {
			System.err.println("[!] Decompilation failed: " + e.getMessage());
			LOG.error("Decompilation failed", e);
		}
	}
}
