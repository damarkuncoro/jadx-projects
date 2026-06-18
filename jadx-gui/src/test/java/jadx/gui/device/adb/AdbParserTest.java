package jadx.gui.device.adb;

import java.util.List;

import org.junit.jupiter.api.Test;

import jadx.gui.device.adb.parsers.AdbDevicesParser;
import jadx.gui.device.adb.parsers.AdbUserParser;
import jadx.gui.device.adb.parsers.AndroidPropsParser;
import jadx.gui.device.adb.parsers.ApkPathParser;
import jadx.gui.device.adb.parsers.PackageListParser;

import static org.assertj.core.api.Assertions.assertThat;

class AdbParserTest {

	@Test
	void testDeviceParsingDirect() {
		String output =
				"1027525383003054       device usb:20-3.1 product:X6833B-GL model:Infinix_X6833B device:Infinix-X6833B transport_id:1\n";
		List<ADBDevice> devices = AdbDevicesParser.parse(output, "localhost", 5037);
		assertThat(devices).hasSize(1);
		ADBDeviceInfo info = devices.get(0).getDeviceInfo();
		assertThat(info.getSerial()).isEqualTo("1027525383003054");
		assertThat(info.getModel()).isEqualTo("Infinix_X6833B");
		assertThat(info.getState()).isEqualTo("device");
		assertThat(info.isOnline()).isTrue();
	}

	@Test
	void testUserParsingDirect() {
		String output = "Users:\n"
				+ "    UserInfo{0:Owner:13} running\n"
				+ "    UserInfo{999:Clone:30} running\n";

		List<AdbService.AdbUser> users = AdbUserParser.parse(output);

		assertThat(users).hasSize(2);
		assertThat(users.get(0).getId()).isEqualTo(0);
		assertThat(users.get(0).getName()).isEqualTo("Owner");
		assertThat(users.get(1).getId()).isEqualTo(999);
		assertThat(users.get(1).getName()).isEqualTo("Clone");
	}

	@Test
	void testUserParsingFallbackDirect() {
		String output = "No users listed\n";
		List<AdbService.AdbUser> users = AdbUserParser.parse(output);

		assertThat(users).hasSize(1);
		assertThat(users.get(0).getId()).isEqualTo(0);
		assertThat(users.get(0).getName()).isEqualTo("Owner");
	}

	@Test
	void testPackageParsingDirect() {
		String output = "package:/system/app/Glance/Glance.apk=com.android.glance\n"
				+ "package:/data/app/~~xyz123/com.whatsapp-abc/base.apk=com.whatsapp\n";

		// All
		List<AdbPackage> all = PackageListParser.parse(output, "all");
		assertThat(all).hasSize(2);
		assertThat(all.get(0).getPackageName()).isEqualTo("com.android.glance");
		assertThat(all.get(0).isSystem()).isTrue();
		assertThat(all.get(1).getPackageName()).isEqualTo("com.whatsapp");
		assertThat(all.get(1).isSystem()).isFalse();

		// System only
		List<AdbPackage> system = PackageListParser.parse(output, "system");
		assertThat(system).hasSize(1);
		assertThat(system.get(0).getPackageName()).isEqualTo("com.android.glance");

		// User only
		List<AdbPackage> user = PackageListParser.parse(output, "user");
		assertThat(user).hasSize(1);
		assertThat(user.get(0).getPackageName()).isEqualTo("com.whatsapp");
	}

	@Test
	void testApkPathClassification() {
		ApkPath base = new ApkPath("/data/app/com.whatsapp/base.apk");
		assertThat(base.getLocalName()).isEqualTo("base.apk");
		assertThat(base.getType()).isEqualTo("base");

		ApkPath abi = new ApkPath("/data/app/com.whatsapp/split_config.arm64_v8a.apk");
		assertThat(abi.getLocalName()).isEqualTo("split_config.arm64_v8a.apk");
		assertThat(abi.getType()).isEqualTo("abi");

		ApkPath density = new ApkPath("/data/app/com.whatsapp/split_config.xxhdpi.apk");
		assertThat(density.getLocalName()).isEqualTo("split_config.xxhdpi.apk");
		assertThat(density.getType()).isEqualTo("density");

		ApkPath lang = new ApkPath("/data/app/com.whatsapp/split_config.id.apk");
		assertThat(lang.getLocalName()).isEqualTo("split_config.id.apk");
		assertThat(lang.getType()).isEqualTo("lang");

		ApkPath legacyIndonesianLang = new ApkPath("/data/app/com.whatsapp/split_config.in.apk");
		assertThat(legacyIndonesianLang.getLocalName()).isEqualTo("split_config.in.apk");
		assertThat(legacyIndonesianLang.getType()).isEqualTo("lang");

		ApkPath regionalLang = new ApkPath("/data/app/com.whatsapp/split_config.pt-rBR.apk");
		assertThat(regionalLang.getLocalName()).isEqualTo("split_config.pt-rBR.apk");
		assertThat(regionalLang.getType()).isEqualTo("lang");

		ApkPath unknown = new ApkPath("/data/app/com.whatsapp/something_random.apk");
		assertThat(unknown.getLocalName()).isEqualTo("something_random.apk");
		assertThat(unknown.getType()).isEqualTo("unknown");
	}

	@Test
	void testApkPathParsingDirect() {
		String output = "package:/data/app/com.whatsapp/base.apk\n"
				+ "package:/data/app/com.whatsapp/split_config.arm64_v8a.apk\n";

		List<ApkPath> paths = ApkPathParser.parse(output);

		assertThat(paths).hasSize(2);
		assertThat(paths.get(0).getRemotePath()).isEqualTo("/data/app/com.whatsapp/base.apk");
		assertThat(paths.get(1).getRemotePath()).isEqualTo("/data/app/com.whatsapp/split_config.arm64_v8a.apk");
	}

	@Test
	void testPropsParsingDirect() {
		String output = "line1\nline2\n";
		List<String> props = AndroidPropsParser.parse(output);
		assertThat(props).containsExactly("line1", "line2");
	}

	@Test
	void testServiceDelegation() {
		// Verify AdbService delegation still works identically
		String userOutput = "Users:\n    UserInfo{0:Owner:13} running\n";
		assertThat(AdbService.parseUsers(userOutput)).hasSize(1);

		String packageOutput = "package:/system/app/Glance/Glance.apk=com.android.glance\n";
		assertThat(AdbService.parsePackages(packageOutput, "all")).hasSize(1);

		String pathsOutput = "package:/data/app/com.whatsapp/base.apk\n";
		assertThat(AdbService.parseApkPaths(pathsOutput)).hasSize(1);
	}
}
