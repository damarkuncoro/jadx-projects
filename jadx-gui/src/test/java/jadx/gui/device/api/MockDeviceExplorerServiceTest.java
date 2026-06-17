package jadx.gui.device.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MockDeviceExplorerServiceTest {

	private MockDeviceExplorerService service;

	@BeforeEach
	void setUp() {
		service = new MockDeviceExplorerService();
	}

	@Test
	void testListDevices() throws IOException {
		List<DeviceInfo> devices = service.listDevices();
		assertThat(devices).hasSize(3);
		assertThat(devices.get(0).getSerial()).isEqualTo("mock-device-1");
		assertThat(devices.get(0).isOnline()).isTrue();
		assertThat(devices.get(1).getSerial()).isEqualTo("mock-device-2");
		assertThat(devices.get(1).isOnline()).isFalse();
	}

	@Test
	void testListUsersOnline() throws Exception {
		List<AndroidUser> users = service.listUsers("mock-device-1");
		assertThat(users).hasSize(2);
		assertThat(users.get(0).getName()).isEqualTo("Owner");
	}

	@Test
	void testListUsersOffline() {
		assertThatThrownBy(() -> service.listUsers("mock-device-2"))
				.isInstanceOf(DeviceExplorerException.class)
				.hasMessageContaining("offline")
				.satisfies(e -> {
					assertThat(((DeviceExplorerException) e).getErrorCode())
							.isEqualTo(DeviceExplorerException.DeviceExplorerErrorCode.DEVICE_OFFLINE);
				});
	}

	@Test
	void testListUsersUnauthorized() {
		assertThatThrownBy(() -> service.listUsers("mock-device-3"))
				.isInstanceOf(DeviceExplorerException.class)
				.hasMessageContaining("unauthorized")
				.satisfies(e -> {
					assertThat(((DeviceExplorerException) e).getErrorCode())
							.isEqualTo(DeviceExplorerException.DeviceExplorerErrorCode.UNAUTHORIZED);
				});
	}

	@Test
	void testListPackages() throws Exception {
		List<AndroidPackage> all = service.listPackages("mock-device-1", 0, "all");
		assertThat(all).hasSize(3);

		List<AndroidPackage> system = service.listPackages("mock-device-1", 0, "system");
		assertThat(system).hasSize(1);
		assertThat(system.get(0).getPackageName()).isEqualTo("com.android.settings");

		List<AndroidPackage> user = service.listPackages("mock-device-1", 0, "user");
		assertThat(user).hasSize(2);
	}

	@Test
	void testResolveApkPaths() throws Exception {
		List<ApkPath> paths = service.resolveApkPaths("mock-device-1", "com.mock.app1", 0);
		assertThat(paths).hasSize(2);
		assertThat(paths.get(0).getLocalName()).isEqualTo("base.apk");
		assertThat(paths.get(1).getLocalName()).isEqualTo("split_config.arm64_v8a.apk");
	}

	@Test
	void testResolveApkPathsNotFound() {
		assertThatThrownBy(() -> service.resolveApkPaths("mock-device-1", "com.nonexistent", 0))
				.isInstanceOf(DeviceExplorerException.class)
				.satisfies(e -> {
					assertThat(((DeviceExplorerException) e).getErrorCode())
							.isEqualTo(DeviceExplorerException.DeviceExplorerErrorCode.PACKAGE_NOT_FOUND);
				});
	}

	@Test
	void testPullApk(@TempDir Path tempDir) throws Exception {
		File outDir = tempDir.resolve("pull-out").toFile();
		PullResult result = service.pullApk("mock-device-1", "com.mock.app1", outDir.getAbsolutePath(), 0);
		
		assertThat(result.getPaths()).hasSize(2);
		assertThat(new File(outDir, "base.apk")).exists();
		assertThat(new File(outDir, "split_config.arm64_v8a.apk")).exists();
	}
}
