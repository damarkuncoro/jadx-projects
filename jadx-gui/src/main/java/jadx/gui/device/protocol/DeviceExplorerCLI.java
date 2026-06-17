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
import jadx.cli.LogHelper;
import jadx.cli.plugins.JadxFilesGetter;
import jadx.gui.device.protocol.AdbService.AdbUser;
import jadx.plugins.tools.JadxExternalPluginsLoader;

public class DeviceExplorerCLI {
	private static final Logger LOG = LoggerFactory.getLogger(DeviceExplorerCLI.class);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static void main(String[] args) {
		CliOptions options = CliOptions.parse(args);
		args = options.getArgs();
		if (options.getFormat().isJson()) {
			LogHelper.setLogLevel(LogHelper.LogLevelEnum.ERROR);
		}
		if (args.length < 2) {
			printUsage();
			System.exit(1);
		}

		String command = args[1];
		try {
			switch (command) {
				case "list-devices":
					executeListDevices(options.getFormat());
					break;
				case "list-users":
					if (args.length < 3) {
						System.err.println("Error: Missing device serial.");
						printUsage();
						System.exit(1);
					}
					executeListUsers(args[2], options.getFormat());
					break;
				case "list-packages":
					if (args.length < 4) {
						System.err.println("Error: Missing device serial and user ID.");
						printUsage();
						System.exit(1);
					}
					String filter = args.length >= 5 ? args[4] : "all";
					executeListPackages(args[2], Integer.parseInt(args[3]), filter, options.getFormat());
					break;
				case "paths":
					if (args.length < 4) {
						System.err.println("Error: Missing device serial and package name.");
						printUsage();
						System.exit(1);
					}
					executePaths(args[2], args[3], options.getFormat());
					break;
				case "pull":
					if (args.length < 5) {
						System.err.println("Error: Missing device serial, package name, and output directory.");
						printUsage();
						System.exit(1);
					}
					int userIdPull = args.length >= 6 ? Integer.parseInt(args[5]) : 0;
					PullResult pullResult = executePull(args[2], args[3], args[4], userIdPull, options.getFormat());
					if (options.getFormat().isJson()) {
						printJson(pullResult.getSummary());
					}
					break;
				case "pull-and-decompile":
					if (args.length < 5) {
						System.err.println("Error: Missing device serial, package name, and output directory.");
						printUsage();
						System.exit(1);
					}
					int userIdDecompile = args.length >= 6 ? Integer.parseInt(args[5]) : 0;
					Map<String, Object> decompileResult = executePullAndDecompile(args[2], args[3], args[4],
							userIdDecompile, options.getFormat());
					if (options.getFormat().isJson()) {
						printJson(decompileResult);
					}
					break;
				default:
					System.err.println("Unknown command: " + command);
					printUsage();
					System.exit(1);
			}
		} catch (Exception e) {
			if (options.getFormat().isJson()) {
				printJson(createError(command, e));
				System.exit(1);
			}
			System.err.println("Error executing command '" + command + "': " + e.getMessage());
			LOG.error("Command execution failed", e);
			System.exit(1);
		}
	}

	private static void printUsage() {
		System.out.println("DexForge Device Explorer CLI Helper Usage:");
		System.out.println("  dexforge device-explorer list-devices [--format json]");
		System.out.println("  dexforge device-explorer list-users <serial> [--format json]");
		System.out.println(
				"  dexforge device-explorer list-packages <serial> <user_id> [filter] [--format json] (filters: all, user, system)");
		System.out.println("  dexforge device-explorer paths <serial> <package_name> [--format json]");
		System.out.println("  dexforge device-explorer pull <serial> <package_name> <out_dir> [user_id] [--format json]");
		System.out.println(
				"  dexforge device-explorer pull-and-decompile <serial> <package_name> <out_dir> [user_id] [--format json]");
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

	private static void executeListDevices(OutputFormat format) throws IOException {
		printStatus(format, "[*] Fetching connected devices...");
		List<ADBDevice> devices = AdbService.listDevices("localhost", 5037);
		if (format.isJson()) {
			List<Map<String, Object>> result = new ArrayList<>();
			for (ADBDevice device : devices) {
				ADBDeviceInfo info = device.getDeviceInfo();
				Map<String, Object> deviceJson = new LinkedHashMap<>();
				deviceJson.put("serial", info.getSerial());
				deviceJson.put("status", info.getState());
				deviceJson.put("model", info.getModel());
				deviceJson.put("androidVersion", device.getAndroidReleaseVersion());
				result.add(deviceJson);
			}
			printJson(result);
			return;
		}
		if (devices.isEmpty()) {
			System.out.println("No devices connected.");
			return;
		}
		for (ADBDevice device : devices) {
			ADBDeviceInfo info = device.getDeviceInfo();
			System.out.printf("Device: %s | Serial: %s | State: %s\n", info.getModel(), info.getSerial(), info.getState());
		}
	}

	private static void executeListUsers(String serial, OutputFormat format) throws IOException, AdbException {
		ADBDevice device = findDevice(serial);
		printStatus(format, "[*] Fetching users for device: " + serial);
		List<AdbUser> users = AdbService.listUsers(device);
		if (format.isJson()) {
			List<Map<String, Object>> result = new ArrayList<>();
			for (AdbUser user : users) {
				Map<String, Object> userJson = new LinkedHashMap<>();
				userJson.put("id", user.getId());
				userJson.put("name", user.getName());
				result.add(userJson);
			}
			printJson(result);
			return;
		}
		for (AdbUser user : users) {
			System.out.printf("User ID: %d | Name: %s\n", user.getId(), user.getName());
		}
	}

	private static void executeListPackages(String serial, int userId, String filter, OutputFormat format)
			throws IOException, AdbException {
		ADBDevice device = findDevice(serial);
		printStatus(format, String.format("[*] Fetching packages for device: %s, User ID: %d, Filter: %s",
				serial, userId, filter));
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
		if (format.isJson()) {
			List<Map<String, Object>> result = new ArrayList<>();
			for (AdbPackage pkg : packages) {
				result.add(toPackageJson(pkg, userId));
			}
			printJson(result);
			return;
		}
		if (packages.isEmpty()) {
			System.out.println("No packages found.");
			return;
		}
		for (AdbPackage pkg : packages) {
			System.out.println(pkg.getPackageName() + " (" + (pkg.isSystem() ? "system" : "user") + ")");
		}
	}

	private static void executePaths(String serial, String packageName, OutputFormat format)
			throws IOException, AdbException {
		ADBDevice device = findDevice(serial);
		printStatus(format, String.format("[*] Resolving APK paths for package '%s'...", packageName));
		List<ApkPath> paths = AdbService.resolveApkPaths(device, packageName, 0);
		if (format.isJson()) {
			printJson(toApkPathJson(paths));
			return;
		}
		if (paths.isEmpty()) {
			System.out.println("No paths resolved.");
			return;
		}
		for (ApkPath path : paths) {
			System.out.printf("Type: %-8s | Path: %s\n", path.getType(), path.getRemotePath());
		}
	}

	private static PullResult executePull(String serial, String packageName, String outDir, int userId,
			OutputFormat format)
			throws IOException, AdbException {
		ADBDevice device = findDevice(serial);
		String adbPath = AdbService.detectAdbPath();
		printStatus(format, String.format("[*] Pulling APKs for package '%s' to '%s'...", packageName, outDir));

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
			printStatus(format, String.format("Pulling: %s -> %s", path.getRemotePath(), localFile.getAbsolutePath()));
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

		File reportFile = new File(reportsDir, "pull-report.json");
		try (FileWriter writer = new FileWriter(reportFile)) {
			GSON.toJson(report, writer);
		}
		printStatus(format, "[*] Metadata pull report saved to " + reportFile.getAbsolutePath());

		return new PullResult(paths, createPullSummary(packageName, serial, userId, outDir, paths, reportFile));
	}

	private static Map<String, Object> executePullAndDecompile(String serial, String packageName, String outDir,
			int userId, OutputFormat format)
			throws IOException, AdbException {
		PullResult pullResult = executePull(serial, packageName, outDir, userId, format);
		List<ApkPath> paths = pullResult.getPaths();

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
			return pullResult.getSummary();
		}

		File jadxOutputDir = new File(outDir, "decompiled");
		long startTime = System.currentTimeMillis();
		decompileApks(apkFiles, jadxOutputDir, format);
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

		File reportFile = new File(reportsDir, "decompile-report.json");
		try (FileWriter writer = new FileWriter(reportFile)) {
			GSON.toJson(report, writer);
		}
		printStatus(format, "[*] Metadata decompilation report saved to " + reportFile.getAbsolutePath());

		File manifestReportFile = new File(reportsDir, "manifest.json");
		writeManifestReport(jadxOutputDir, manifestReportFile);
		printStatus(format, "[*] Manifest report saved to " + manifestReportFile.getAbsolutePath());

		File securityReportFile = new File(reportsDir, "security.json");
		printStatus(format, "[*] Running Security Assistant analysis...");
		try {
			DeviceExplorerAssistant.runAnalysis(jadxOutputDir, securityReportFile);
			printStatus(format, "[*] Security Assistant report saved to " + securityReportFile.getAbsolutePath());
		} catch (Exception e) {
			System.err.println("[!] Security Assistant analysis failed: " + e.getMessage());
		}

		Map<String, Object> result = pullResult.getSummary();
		result.put("decompiledDir", "decompiled");
		result.put("decompiledPath", jadxOutputDir.getAbsolutePath());
		result.put("durationMs", duration);
		@SuppressWarnings("unchecked")
		Map<String, Object> reports = (Map<String, Object>) result.get("reports");
		reports.put("decompile", "reports/" + reportFile.getName());
		reports.put("manifest", "reports/manifest.json");
		reports.put("security", "reports/" + securityReportFile.getName());
		return result;
	}

	private static void decompileApks(List<File> apkFiles, File outDir, OutputFormat format) {
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

		printStatus(format, "[*] Starting decompilation...");
		try (JadxDecompiler decompiler = new JadxDecompiler(jadxArgs)) {
			decompiler.load();
			decompiler.save(500, (done, total) -> {
				if (format.isJson()) {
					return;
				}
				int progress = (int) (done * 100.0 / total);
				System.out.printf("INFO  - progress: %d of %d (%d%%)\r", done, total, progress);
			});
			printStatus(format, "\n[*] Decompilation finished. Saved to: " + outDir.getAbsolutePath());
		} catch (Exception e) {
			System.err.println("[!] Decompilation failed: " + e.getMessage());
			LOG.error("Decompilation failed", e);
		}
	}

	static Map<String, Object> createPullSummary(String packageName, String serial, int userId, String outDir,
			List<ApkPath> paths, File pullReportFile) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("packageName", packageName);
		result.put("deviceSerial", serial);
		result.put("androidUser", userId);
		result.put("workspace", new File(outDir).getAbsolutePath());

		List<String> apks = new ArrayList<>();
		for (ApkPath path : paths) {
			apks.add(path.getLocalName());
		}
		result.put("apks", apks);
		result.put("apkPaths", toApkPathJson(paths));

		Map<String, Object> reports = new LinkedHashMap<>();
		reports.put("pull", "reports/" + pullReportFile.getName());
		result.put("reports", reports);
		return result;
	}

	static void writeManifestReport(File decompiledDir, File manifestReportFile) throws IOException {
		File manifestFile = findFile(decompiledDir, "AndroidManifest.xml");
		Map<String, Object> manifestReport = new LinkedHashMap<>();
		manifestReport.put("status", manifestFile == null ? "MISSING" : "FOUND");
		if (manifestFile != null) {
			manifestReport.put("path", decompiledDir.toPath().relativize(manifestFile.toPath()).toString());
			manifestReport.put("absolutePath", manifestFile.getAbsolutePath());
		}
		File parent = manifestReportFile.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		try (FileWriter writer = new FileWriter(manifestReportFile)) {
			GSON.toJson(manifestReport, writer);
		}
	}

	private static File findFile(File dir, String fileName) {
		if (dir == null || !dir.exists()) {
			return null;
		}
		File[] files = dir.listFiles();
		if (files == null) {
			return null;
		}
		for (File file : files) {
			if (file.isFile() && fileName.equals(file.getName())) {
				return file;
			}
			if (file.isDirectory()) {
				File found = findFile(file, fileName);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	static List<Map<String, Object>> toApkPathJson(List<ApkPath> paths) {
		List<Map<String, Object>> result = new ArrayList<>();
		for (ApkPath path : paths) {
			Map<String, Object> pathJson = new LinkedHashMap<>();
			pathJson.put("type", path.getType());
			pathJson.put("remotePath", path.getRemotePath());
			pathJson.put("localName", path.getLocalName());
			result.add(pathJson);
		}
		return result;
	}

	static Map<String, Object> toPackageJson(AdbPackage pkg, int userId) {
		Map<String, Object> packageJson = new LinkedHashMap<>();
		packageJson.put("packageName", pkg.getPackageName());
		packageJson.put("label", pkg.getPackageName());
		packageJson.put("userId", userId);
		packageJson.put("type", pkg.isSystem() ? "system" : "user");
		packageJson.put("path", pkg.getPath());
		return packageJson;
	}

	private static void printJson(Object value) {
		System.out.println(GSON.toJson(value));
	}

	static Map<String, Object> createError(String command, Exception e) {
		Map<String, Object> error = new LinkedHashMap<>();
		error.put("status", "ERROR");
		error.put("command", command);
		error.put("message", e.getMessage());
		return error;
	}

	private static void printStatus(OutputFormat format, String message) {
		if (format.isJson()) {
			return;
		}
		System.out.println(message);
	}

	private enum OutputFormat {
		TEXT,
		JSON;

		boolean isJson() {
			return this == JSON;
		}
	}

	private static final class CliOptions {
		private final String[] args;
		private final OutputFormat format;

		private CliOptions(String[] args, OutputFormat format) {
			this.args = args;
			this.format = format;
		}

		static CliOptions parse(String[] args) {
			List<String> normalizedArgs = new ArrayList<>();
			OutputFormat format = OutputFormat.TEXT;
			for (int i = 0; i < args.length; i++) {
				String arg = args[i];
				if ("--format".equals(arg)) {
					if (i + 1 >= args.length) {
						throw new IllegalArgumentException("Missing value for --format. Expected: text or json.");
					}
					String value = args[++i];
					if ("json".equalsIgnoreCase(value)) {
						format = OutputFormat.JSON;
					} else if ("text".equalsIgnoreCase(value)) {
						format = OutputFormat.TEXT;
					} else {
						throw new IllegalArgumentException("Unsupported --format value: " + value);
					}
				} else if ("--json".equals(arg)) {
					format = OutputFormat.JSON;
				} else {
					normalizedArgs.add(arg);
				}
			}
			return new CliOptions(normalizedArgs.toArray(new String[0]), format);
		}

		String[] getArgs() {
			return args;
		}

		OutputFormat getFormat() {
			return format;
		}
	}

	private static final class PullResult {
		private final List<ApkPath> paths;
		private final Map<String, Object> summary;

		private PullResult(List<ApkPath> paths, Map<String, Object> summary) {
			this.paths = paths;
			this.summary = summary;
		}

		List<ApkPath> getPaths() {
			return paths;
		}

		Map<String, Object> getSummary() {
			return summary;
		}
	}
}
