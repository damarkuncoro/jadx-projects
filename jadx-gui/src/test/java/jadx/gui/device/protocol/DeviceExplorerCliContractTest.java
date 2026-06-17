package jadx.gui.device.protocol;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceExplorerCliContractTest {

	@TempDir
	Path tempDir;

	@Test
	void testPackageJsonContract() {
		AdbPackage pkg = new AdbPackage("id.net.cakramedia.attendance",
				"/data/app/~~abc/id.net.cakramedia.attendance/base.apk");

		Map<String, Object> json = DeviceExplorerCLI.toPackageJson(pkg, 0);

		assertThat(json)
				containsEntry("packageName", "id.net.cakramedia.attendance")
				.containsEntry("label", "id.net.cakramedia.attendance")
				.containsEntry("userId", 0)
				.containsEntry("type", "user")
				.containsEntry("path", "/data/app/~~abc/id.net.cakramedia.attendance/base.apk");
	}

	@Test
	void testPullSummaryJsonContract() {
		List<ApkPath> paths = List.of(
				new ApkPath("/data/app/id.net.cakramedia.attendance/base.apk"),
				new ApkPath("/data/app/id.net.cakramedia.attendance/split_config.arm64_v8a.apk"),
				new ApkPath("/data/app/id.net.cakramedia.attendance/split_config.xxhdpi.apk"));
		File workspace = tempDir.resolve("workspace/id.net.cakramedia.attendance").toFile();
		File pullReport = new File(workspace, "reports/pull-report.json");

		Map<String, Object> json = DeviceExplorerCLI.createPullSummary(
				"id.net.cakramedia.attendance",
				"R58Nxxxx",
				0,
				workspace.getAbsolutePath(),
				paths,
				pullReport);

		assertThat(json)
				.containsEntry("packageName", "id.net.cakramedia.attendance")
				.containsEntry("deviceSerial", "R58Nxxxx")
				.containsEntry("androidUser", 0)
				.containsEntry("workspace", workspace.getAbsolutePath());
		assertThat(json.get("apks"))
				.asList()
				.containsExactly("base.apk", "split_config.arm64_v8a.apk", "split_config.xxhdpi.apk");

		@SuppressWarnings("unchecked")
		Map<String, Object> reports = (Map<String, Object>) json.get("reports");
		assertThat(reports)
				.containsEntry("pull", "reports/pull-report.json")
				.doesNotContainKeys("manifest", "security");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> apkPaths = (List<Map<String, Object>>) json.get("apkPaths");
		assertThat(apkPaths).hasSize(3);
		assertThat(apkPaths.get(0))
				.containsEntry("type", "base")
				.containsEntry("localName", "base.apk")
				.containsEntry("remotePath", "/data/app/id.net.cakramedia.attendance/base.apk");
	}

	@Test
	void testErrorJsonContract() {
		Map<String, Object> json = DeviceExplorerCLI.createError("list-devices", new IllegalStateException("ADB failed"));

		assertThat(json)
				.containsEntry("status", "ERROR")
				.containsEntry("command", "list-devices")
				.containsEntry("message", "ADB failed");
	}

	@Test
	void testManifestReportContract() throws Exception {
		File decompiledDir = tempDir.resolve("decompiled").toFile();
		File resourcesDir = new File(decompiledDir, "resources");
		assertThat(resourcesDir.mkdirs()).isTrue();
		File manifest = new File(resourcesDir, "AndroidManifest.xml");
		assertThat(manifest.createNewFile()).isTrue();

		File reportFile = tempDir.resolve("reports/manifest.json").toFile();
		DeviceExplorerCLI.writeManifestReport(decompiledDir, reportFile);

		assertThat(reportFile).exists();
		try (FileReader reader = new FileReader(reportFile)) {
			JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
			assertThat(json.get("status").getAsString()).isEqualTo("FOUND");
			assertThat(json.get("path").getAsString()).isEqualTo("resources/AndroidManifest.xml");
			assertThat(json.get("absolutePath").getAsString()).isEqualTo(manifest.getAbsolutePath());
		}
	}
}
