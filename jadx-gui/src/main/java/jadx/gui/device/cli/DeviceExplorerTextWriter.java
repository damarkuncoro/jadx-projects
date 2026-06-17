package jadx.gui.device.cli;

import java.util.List;
import jadx.gui.device.cli.dto.DeviceDto;
import jadx.gui.device.cli.dto.UserDto;
import jadx.gui.device.cli.dto.PackageDto;
import jadx.gui.device.cli.dto.ApkPathDto;
import jadx.gui.device.cli.dto.PullResultDto;
import jadx.gui.device.cli.dto.ContractDto;

public final class DeviceExplorerTextWriter {
	private DeviceExplorerTextWriter() {}

	public static void printUsage() {
		System.out.println("DexForge Device Explorer CLI Helper Usage:");
		System.out.println("  dexforge device-explorer contract [--format json]");
		System.out.println("  dexforge device-explorer list-devices [--format json]");
		System.out.println("  dexforge device-explorer list-users <serial> [--format json]");
		System.out.println("  dexforge device-explorer list-packages <serial> <user_id> [filter] [--format json] (filters: all, user, system)");
		System.out.println("  dexforge device-explorer paths <serial> <package_name> [--format json]");
		System.out.println("  dexforge device-explorer pull <serial> <package_name> <out_dir> [user_id] [--format json]");
		System.out.println("  dexforge device-explorer pull-and-decompile <serial> <package_name> <out_dir> [user_id] [--format json]");
	}

	public static void printDevices(List<DeviceDto> devices) {
		if (devices.isEmpty()) {
			System.out.println("No devices connected.");
			return;
		}
		for (DeviceDto device : devices) {
			System.out.printf("Device: %s | Serial: %s | State: %s\n", device.getModel(), device.getSerial(), device.getStatus());
		}
	}

	public static void printUsers(List<UserDto> users) {
		for (UserDto user : users) {
			System.out.printf("User ID: %d | Name: %s\n", user.getId(), user.getName());
		}
	}

	public static void printPackages(List<PackageDto> packages) {
		if (packages.isEmpty()) {
			System.out.println("No packages found.");
			return;
		}
		for (PackageDto pkg : packages) {
			System.out.println(pkg.getPackageName() + " (" + pkg.getType() + ")");
		}
	}

	public static void printApkPaths(List<ApkPathDto> paths) {
		if (paths.isEmpty()) {
			System.out.println("No paths resolved.");
			return;
		}
		for (ApkPathDto path : paths) {
			System.out.printf("Type: %-8s | Path: %s\n", path.getType(), path.getRemotePath());
		}
	}

	public static void printPullResult(PullResultDto result) {
		System.out.println("[*] Pull completed successfully.");
		System.out.println("Workspace: " + result.getWorkspace());
		System.out.println("APKs pulled: " + result.getApks());
	}

	public static void printContract(ContractDto contract) {
		System.out.println("API Version: " + contract.getApiVersion());
		System.out.println("Available commands: " + String.join(", ", contract.getCommands()));
	}
}
