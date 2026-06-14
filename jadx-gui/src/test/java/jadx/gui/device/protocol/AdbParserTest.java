package jadx.gui.device.protocol;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdbParserTest {

	@Test
	void testDeviceParsing() {
		String line =
				"1027525383003054       device usb:20-3.1 product:X6833B-GL model:Infinix_X6833B device:Infinix-X6833B transport_id:1";
		ADBDeviceInfo info = new ADBDeviceInfo(line, "localhost", 5037);

		assertThat(info.getSerial()).isEqualTo("1027525383003054");
		assertThat(info.getModel()).isEqualTo("Infinix_X6833B");
		assertThat(info.getState()).isEqualTo("device");
		assertThat(info.isOnline()).isTrue();
	}

	@Test
	void testUserParsing() {
		String output = "Users:\n"
				+ "    UserInfo{0:Owner:13} running\n"
				+ "    UserInfo{999:Clone:30} running\n";

		List<AdbService.AdbUser> users = AdbService.parseUsers(output);

		assertThat(users).hasSize(2);
		assertThat(users.get(0).getId()).isEqualTo(0);
		assertThat(users.get(0).getName()).isEqualTo("Owner");
		assertThat(users.get(1).getId()).isEqualTo(999);
		assertThat(users.get(1).getName()).isEqualTo("Clone");
	}

	@Test
	void testUserParsingFallback() {
		String output = "No users listed\n";
		List<AdbService.AdbUser> users = AdbService.parseUsers(output);

		assertThat(users).hasSize(1);
		assertThat(users.get(0).getId()).isEqualTo(0);
		assertThat(users.get(0).getName()).isEqualTo("Owner");
	}

	@Test
	void testPackageParsing() {
		String output = "package:/system/app/Glance/Glance.apk=com.android.glance\n"
				+ "package:/data/app/~~xyz123/com.whatsapp-abc/base.apk=com.whatsapp\n";

		// All
		List<AdbPackage> all = AdbService.parsePackages(output, "all");
		assertThat(all).hasSize(2);
		assertThat(all.get(0).getPackageName()).isEqualTo("com.android.glance");
		assertThat(all.get(0).isSystem()).isTrue();
		assertThat(all.get(1).getPackageName()).isEqualTo("com.whatsapp");
		assertThat(all.get(1).isSystem()).isFalse();

		// System only
		List<AdbPackage> system = AdbService.parsePackages(output, "system");
		assertThat(system).hasSize(1);
		assertThat(system.get(0).getPackageName()).isEqualTo("com.android.glance");

		// User only
		List<AdbPackage> user = AdbService.parsePackages(output, "user");
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

		ApkPath unknown = new ApkPath("/data/app/com.whatsapp/something_random.apk");
		assertThat(unknown.getLocalName()).isEqualTo("something_random.apk");
		assertThat(unknown.getType()).isEqualTo("unknown");
	}

	@Test
	void testApkPathParsing() {
		String output = "package:/data/app/com.whatsapp/base.apk\n"
				+ "package:/data/app/com.whatsapp/split_config.arm64_v8a.apk\n";

		List<ApkPath> paths = AdbService.parseApkPaths(output);

		assertThat(paths).hasSize(2);
		assertThat(paths.get(0).getRemotePath()).isEqualTo("/data/app/com.whatsapp/base.apk");
		assertThat(paths.get(1).getRemotePath()).isEqualTo("/data/app/com.whatsapp/split_config.arm64_v8a.apk");
	}
}
