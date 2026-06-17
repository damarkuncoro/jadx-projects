package jadx.gui.device.cli;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jadx.gui.device.api.AndroidPackage;
import jadx.gui.device.api.AndroidUser;
import jadx.gui.device.api.ApkPath;
import jadx.gui.device.api.DeviceInfo;
import jadx.gui.device.cli.dto.ApkPathDto;
import jadx.gui.device.cli.dto.DeviceDto;
import jadx.gui.device.cli.dto.ErrorDto;
import jadx.gui.device.cli.dto.PackageDto;
import jadx.gui.device.cli.dto.PullResultDto;
import jadx.gui.device.cli.dto.UserDto;
import jadx.gui.device.api.DeviceExplorerException;
import jadx.gui.device.workspace.DexForgeWorkspaceLayout;

public final class DeviceExplorerJsonWriter {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private DeviceExplorerJsonWriter() {
	}

	public static void print(Object value) {
		System.out.println(toJson(value));
	}

	public static String toJson(Object value) {
		return GSON.toJson(value);
	}

	public static void write(File file, Object value) throws IOException {
		File parent = file.getParentFile();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		try (FileWriter writer = new FileWriter(file)) {
			GSON.toJson(value, writer);
		}
	}

	public static List<DeviceDto> toDevicesDto(List<DeviceInfo> devices) {
		List<DeviceDto> result = new ArrayList<>();
		for (DeviceInfo device : devices) {
			result.add(new DeviceDto(
					device.getSerial(),
					device.getState(),
					device.getModel(),
					device.getState() // or default version
			));
		}
		return result;
	}

	public static List<UserDto> toUsersDto(List<AndroidUser> users) {
		List<UserDto> result = new ArrayList<>();
		for (AndroidUser user : users) {
			result.add(new UserDto(user.getId(), user.getName()));
		}
		return result;
	}

	public static List<PackageDto> toPackagesDto(List<AndroidPackage> packages, int userId) {
		List<PackageDto> result = new ArrayList<>();
		for (AndroidPackage pkg : packages) {
			result.add(toPackageDto(pkg, userId));
		}
		return result;
	}

	public static PackageDto toPackageDto(AndroidPackage pkg, int userId) {
		return new PackageDto(
				pkg.getPackageName(),
				pkg.getPackageName(),
				userId,
				pkg.isSystem() ? "system" : "user",
				pkg.getPath()
		);
	}

	public static List<ApkPathDto> toApkPathsDto(List<ApkPath> paths) {
		List<ApkPathDto> result = new ArrayList<>();
		for (ApkPath path : paths) {
			result.add(new ApkPathDto(path.getType(), path.getRemotePath(), path.getLocalName()));
		}
		return result;
	}

	public static PullResultDto createPullSummary(String packageName, String serial, int userId,
			DexForgeWorkspaceLayout layout, List<ApkPath> paths) {
		List<String> apks = new ArrayList<>();
		for (ApkPath path : paths) {
			apks.add(path.getLocalName());
		}

		Map<String, String> reports = new LinkedHashMap<>();
		reports.put("pull", layout.getPullReportPath());

		return new PullResultDto(
				packageName,
				serial,
				userId,
				layout.getRootDir().getAbsolutePath(),
				apks,
				toApkPathsDto(paths),
				reports
		);
	}

	public static ErrorDto createError(String command, Exception e) {
		String code = "INTERNAL_ERROR";
		String msg = e.getMessage();
		
		Throwable target = e;
		while (target != null && !(target instanceof DeviceExplorerException)) {
			target = target.getCause();
		}
		
		if (target instanceof DeviceExplorerException) {
			DeviceExplorerException.DeviceExplorerErrorCode err = ((DeviceExplorerException) target).getErrorCode();
			if (err != DeviceExplorerException.DeviceExplorerErrorCode.INTERNAL_ERROR) {
				code = err.name();
			} else if (msg != null && msg.toLowerCase().contains("not found")) {
				code = "ADB_NOT_FOUND";
			}
		} else if (msg != null && msg.toLowerCase().contains("not found")) {
			code = "ADB_NOT_FOUND";
		}
		
		return new ErrorDto("ERROR", command, code, msg, new LinkedHashMap<>());
	}
}
