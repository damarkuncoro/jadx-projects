package jadx.gui.device.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.impl.NoOpCodeCache;
import jadx.api.impl.SimpleCodeWriter;
import jadx.api.usage.impl.EmptyUsageInfoCache;
import dexforge.cli.LogHelper;
import dexforge.cli.plugins.DexforgeFilesGetter;
import jadx.gui.device.api.AndroidPackage;
import jadx.gui.device.api.AndroidUser;
import jadx.gui.device.api.ApkPath;
import jadx.gui.device.api.DeviceExplorerService;
import jadx.gui.device.api.DeviceExplorerServiceImpl;
import jadx.gui.device.api.DeviceInfo;
import jadx.gui.device.api.PullResult;
import jadx.gui.device.cli.dto.ApkPathDto;
import jadx.gui.device.cli.dto.ContractDto;
import jadx.gui.device.cli.dto.DeviceDto;
import jadx.gui.device.cli.dto.PackageDto;
import jadx.gui.device.cli.dto.PullResultDto;
import jadx.gui.device.cli.dto.RequestDto;
import jadx.gui.device.cli.dto.ResponseDto;
import jadx.gui.device.cli.dto.UserDto;
import jadx.gui.device.reports.DeviceExplorerAssistant;
import jadx.gui.device.reports.DeviceReportWriter;
import jadx.gui.device.workspace.DexForgeWorkspaceLayout;
import jadx.plugins.tools.JadxExternalPluginsLoader;

public class DeviceExplorerCLI {
	private static final Logger LOG = LoggerFactory.getLogger(DeviceExplorerCLI.class);

	private static final DeviceExplorerService SERVICE = new DeviceExplorerServiceImpl();

	public static void main(String[] args) {
		CliOptions options = CliOptions.parse(args);
		args = options.getArgs();
		if (options.getFormat().isJson()) {
			LogHelper.setLogLevel(LogHelper.LogLevelEnum.ERROR);
		}
		if (args.length < 2) {
			DeviceExplorerTextWriter.printUsage();
			System.exit(1);
		}

		String command = args[1];
		try {
			switch (command) {
				case "daemon":
					LogHelper.setLogLevel(LogHelper.LogLevelEnum.ERROR);
					executeDaemon();
					break;
				case "contract":
					executeContract(options.getFormat());
					break;
				case "list-devices":
					executeListDevices(options.getFormat());
					break;
				case "list-users":
					if (args.length < 3) {
						System.err.println("Error: Missing device serial.");
						DeviceExplorerTextWriter.printUsage();
						System.exit(1);
					}
					executeListUsers(args[2], options.getFormat());
					break;
				case "list-packages":
					if (args.length < 4) {
						System.err.println("Error: Missing device serial and user ID.");
						DeviceExplorerTextWriter.printUsage();
						System.exit(1);
					}
					String filter = args.length >= 5 ? args[4] : "all";
					executeListPackages(args[2], Integer.parseInt(args[3]), filter, options.getFormat());
					break;
				case "paths":
					if (args.length < 4) {
						System.err.println("Error: Missing device serial and package name.");
						DeviceExplorerTextWriter.printUsage();
						System.exit(1);
					}
					executePaths(args[2], args[3], options.getFormat());
					break;
				case "pull":
					if (args.length < 5) {
						System.err.println("Error: Missing device serial, package name, and output directory.");
						DeviceExplorerTextWriter.printUsage();
						System.exit(1);
					}
					int userIdPull = args.length >= 6 ? Integer.parseInt(args[5]) : 0;
					PullResultDto pullResult = executePull(args[2], args[3], args[4], userIdPull, options.getFormat());
					if (options.getFormat().isJson()) {
						DeviceExplorerJsonWriter.print(pullResult);
					} else {
						DeviceExplorerTextWriter.printPullResult(pullResult);
					}
					break;
				case "pull-and-decompile":
					if (args.length < 5) {
						System.err.println("Error: Missing device serial, package name, and output directory.");
						DeviceExplorerTextWriter.printUsage();
						System.exit(1);
					}
					int userIdDecompile = args.length >= 6 ? Integer.parseInt(args[5]) : 0;
					PullResultDto decompileResult = executePullAndDecompile(args[2], args[3], args[4],
							userIdDecompile, options.getFormat());
					if (options.getFormat().isJson()) {
						DeviceExplorerJsonWriter.print(decompileResult);
					} else {
						DeviceExplorerTextWriter.printPullResult(decompileResult);
					}
					break;
				default:
					System.err.println("Unknown command: " + command);
					DeviceExplorerTextWriter.printUsage();
					System.exit(1);
			}
		} catch (Exception e) {
			if (options.getFormat().isJson()) {
				DeviceExplorerJsonWriter.print(DeviceExplorerJsonWriter.createError(command, e));
				System.exit(1);
			}
			System.err.println("Error executing command '" + command + "': " + e.getMessage());
			LOG.error("Command execution failed", e);
			System.exit(1);
		}
	}

	private static void executeContract(OutputFormat format) {
		List<String> commands = List.of(
				"contract",
				"list-devices",
				"list-users",
				"list-packages",
				"paths",
				"pull",
				"pull-and-decompile");
		ContractDto contract = new ContractDto("1", commands);
		if (format.isJson()) {
			DeviceExplorerJsonWriter.print(contract);
		} else {
			DeviceExplorerTextWriter.printContract(contract);
		}
	}

	private static void executeListDevices(OutputFormat format) throws IOException {
		printStatus(format, "[*] Fetching connected devices...");
		List<DeviceInfo> devices = SERVICE.listDevices();
		List<DeviceDto> dtos = DeviceExplorerJsonWriter.toDevicesDto(devices);
		if (format.isJson()) {
			DeviceExplorerJsonWriter.print(dtos);
		} else {
			DeviceExplorerTextWriter.printDevices(dtos);
		}
	}

	private static void executeListUsers(String serial, OutputFormat format) throws Exception {
		printStatus(format, "[*] Fetching users for device: " + serial);
		List<AndroidUser> users = SERVICE.listUsers(serial);
		List<UserDto> dtos = DeviceExplorerJsonWriter.toUsersDto(users);
		if (format.isJson()) {
			DeviceExplorerJsonWriter.print(dtos);
		} else {
			DeviceExplorerTextWriter.printUsers(dtos);
		}
	}

	private static void executeListPackages(String serial, int userId, String filter, OutputFormat format)
			throws Exception {
		printStatus(format, String.format("[*] Fetching packages for device: %s, User ID: %d, Filter: %s",
				serial, userId, filter));
		List<AndroidPackage> packages = SERVICE.listPackages(serial, userId, filter);
		List<PackageDto> dtos = DeviceExplorerJsonWriter.toPackagesDto(packages, userId);
		if (format.isJson()) {
			DeviceExplorerJsonWriter.print(dtos);
		} else {
			DeviceExplorerTextWriter.printPackages(dtos);
		}
	}

	private static void executePaths(String serial, String packageName, OutputFormat format)
			throws Exception {
		printStatus(format, String.format("[*] Resolving APK paths for package '%s'...", packageName));
		List<ApkPath> paths = SERVICE.resolveApkPaths(serial, packageName, 0);
		List<ApkPathDto> dtos = DeviceExplorerJsonWriter.toApkPathsDto(paths);
		if (format.isJson()) {
			DeviceExplorerJsonWriter.print(dtos);
		} else {
			DeviceExplorerTextWriter.printApkPaths(dtos);
		}
	}

	private static PullResultDto executePull(String serial, String packageName, String outDir, int userId,
			OutputFormat format)
			throws Exception {
		printStatus(format, String.format("[*] Pulling APKs for package '%s' to '%s'...", packageName, outDir));
		PullResult pullResult = SERVICE.pullApk(serial, packageName, outDir, userId);

		DexForgeWorkspaceLayout layout = new DexForgeWorkspaceLayout(outDir);
		File apksDir = layout.getApksDir();

		// Write metadata pull-report.json
		File reportsDir = layout.getReportsDir();
		if (!reportsDir.exists()) {
			reportsDir.mkdirs();
		}

		Map<String, Object> report = new java.util.LinkedHashMap<>();
		report.put("packageName", packageName);
		report.put("deviceSerial", serial);
		report.put("androidUser", userId);
		report.put("pulledAt", java.time.Instant.now().toString());

		List<Map<String, Object>> apkFilesList = new ArrayList<>();
		for (ApkPath path : pullResult.getPaths()) {
			File localFile = new File(apksDir, path.getLocalName());
			Map<String, Object> apkInfo = new java.util.LinkedHashMap<>();
			apkInfo.put("remotePath", path.getRemotePath());
			apkInfo.put("localPath", layout.apkPath(path.getLocalName()));
			apkInfo.put("sizeBytes", localFile.exists() ? localFile.length() : 0);
			apkInfo.put("type", path.getType());
			apkFilesList.add(apkInfo);
		}
		report.put("apkFiles", apkFilesList);

		File reportFile = layout.getPullReportFile();
		DeviceReportWriter.writeJson(reportFile, report);
		printStatus(format, "[*] Metadata pull report saved to " + reportFile.getAbsolutePath());

		return DeviceExplorerJsonWriter.createPullSummary(packageName, serial, userId, layout, pullResult.getPaths());
	}

	private static PullResultDto executePullAndDecompile(String serial, String packageName, String outDir,
			int userId, OutputFormat format)
			throws Exception {
		PullResultDto pullResult = executePull(serial, packageName, outDir, userId, format);
		List<ApkPathDto> paths = pullResult.getApkPaths();

		DexForgeWorkspaceLayout layout = new DexForgeWorkspaceLayout(outDir);
		File apksDir = layout.getApksDir();
		List<File> apkFiles = new ArrayList<>();
		for (ApkPathDto path : paths) {
			File file = new File(apksDir, path.getLocalName());
			if (file.exists()) {
				apkFiles.add(file);
			}
		}

		if (apkFiles.isEmpty()) {
			System.err.println("Error: No APK files were pulled.");
			return pullResult;
		}

		File jadxOutputDir = layout.getDecompiledDir();
		long startTime = System.currentTimeMillis();
		decompileApks(apkFiles, jadxOutputDir, format);
		long duration = System.currentTimeMillis() - startTime;

		// Write decompile-report.json
		File reportFile = layout.getDecompileReportFile();
		DeviceReportWriter.writeDecompileReport(apkFiles, jadxOutputDir, reportFile, duration);
		printStatus(format, "[*] Metadata decompilation report saved to " + reportFile.getAbsolutePath());

		File manifestReportFile = layout.getManifestReportFile();
		DeviceReportWriter.writeManifestReport(jadxOutputDir, manifestReportFile);
		printStatus(format, "[*] Manifest report saved to " + manifestReportFile.getAbsolutePath());

		File securityReportFile = layout.getSecurityReportFile();
		printStatus(format, "[*] Running Security Assistant analysis...");
		try {
			DeviceExplorerAssistant.runAnalysis(jadxOutputDir, securityReportFile);
			printStatus(format, "[*] Security Assistant report saved to " + securityReportFile.getAbsolutePath());
		} catch (Exception e) {
			System.err.println("[!] Security Assistant analysis failed: " + e.getMessage());
		}

		pullResult.setDecompiledDir(layout.getDecompiledDirName());
		pullResult.setDecompiledPath(jadxOutputDir.getAbsolutePath());
		pullResult.setDurationMs(duration);

		Map<String, String> reports = pullResult.getReports();
		reports.put("decompile", layout.getDecompileReportPath());
		reports.put("manifest", layout.getManifestReportPath());
		reports.put("security", layout.getSecurityReportPath());

		return pullResult;
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
		jadxArgs.setFilesGetter(DexforgeFilesGetter.INSTANCE);
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

	private static void executeDaemon() {
		Gson gson = new Gson();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				RequestDto request;
				try {
					request = gson.fromJson(line, RequestDto.class);
				} catch (Exception e) {
					ResponseDto errorResponse = new ResponseDto(0, "ERROR", null,
							DeviceExplorerJsonWriter.createError("parse-request", e));
					System.out.println(gson.toJson(errorResponse));
					System.out.flush();
					continue;
				}

				if (request == null) {
					continue;
				}

				int id = request.getId();
				String method = request.getMethod();
				Map<String, Object> params = request.getParams();

				if ("exit".equals(method)) {
					ResponseDto successResponse = new ResponseDto(id, "SUCCESS", "Daemon exiting", null);
					System.out.println(gson.toJson(successResponse));
					System.out.flush();
					break;
				}

				try {
					Object result = dispatchDaemonMethod(method, params);
					ResponseDto successResponse = new ResponseDto(id, "SUCCESS", result, null);
					System.out.println(gson.toJson(successResponse));
					System.out.flush();
				} catch (Exception e) {
					ResponseDto errorResponse = new ResponseDto(id, "ERROR", null,
							DeviceExplorerJsonWriter.createError(method, e));
					System.out.println(gson.toJson(errorResponse));
					System.out.flush();
				}
			}
		} catch (IOException e) {
			LOG.error("Daemon IO error", e);
		}
	}

	private static Object dispatchDaemonMethod(String method, Map<String, Object> params) throws Exception {
		switch (method) {
			case "list-devices": {
				List<DeviceInfo> devices = SERVICE.listDevices();
				return DeviceExplorerJsonWriter.toDevicesDto(devices);
			}
			case "list-users": {
				String serial = getRequiredParam(params, "serial");
				List<AndroidUser> users = SERVICE.listUsers(serial);
				return DeviceExplorerJsonWriter.toUsersDto(users);
			}
			case "list-packages": {
				String serial = getRequiredParam(params, "serial");
				int userId = getIntParam(params, "userId", 0);
				String filter = getStringParam(params, "filter", "all");
				List<AndroidPackage> packages = SERVICE.listPackages(serial, userId, filter);
				return DeviceExplorerJsonWriter.toPackagesDto(packages, userId);
			}
			case "paths": {
				String serial = getRequiredParam(params, "serial");
				String packageName = getRequiredParam(params, "packageName");
				List<ApkPath> paths = SERVICE.resolveApkPaths(serial, packageName, 0);
				return DeviceExplorerJsonWriter.toApkPathsDto(paths);
			}
			case "pull": {
				String serial = getRequiredParam(params, "serial");
				String packageName = getRequiredParam(params, "packageName");
				String outDir = getRequiredParam(params, "outDir");
				int userId = getIntParam(params, "userId", 0);
				return executePull(serial, packageName, outDir, userId, OutputFormat.JSON);
			}
			case "pull-and-decompile": {
				String serial = getRequiredParam(params, "serial");
				String packageName = getRequiredParam(params, "packageName");
				String outDir = getRequiredParam(params, "outDir");
				int userId = getIntParam(params, "userId", 0);
				return executePullAndDecompile(serial, packageName, outDir, userId, OutputFormat.JSON);
			}
			default:
				throw new IllegalArgumentException("Unknown daemon method: " + method);
		}
	}

	private static String getRequiredParam(Map<String, Object> params, String name) {
		if (params == null || !params.containsKey(name)) {
			throw new IllegalArgumentException("Missing required parameter: " + name);
		}
		Object val = params.get(name);
		if (val == null) {
			throw new IllegalArgumentException("Parameter " + name + " cannot be null");
		}
		return val.toString();
	}

	private static String getStringParam(Map<String, Object> params, String name, String defaultValue) {
		if (params == null || !params.containsKey(name) || params.get(name) == null) {
			return defaultValue;
		}
		return params.get(name).toString();
	}

	private static int getIntParam(Map<String, Object> params, String name, int defaultValue) {
		if (params == null || !params.containsKey(name) || params.get(name) == null) {
			return defaultValue;
		}
		Object val = params.get(name);
		if (val instanceof Number) {
			return ((Number) val).intValue();
		}
		try {
			return Integer.parseInt(val.toString());
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
}
