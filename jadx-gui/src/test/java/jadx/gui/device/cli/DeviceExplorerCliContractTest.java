package jadx.gui.device.cli;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jadx.gui.device.api.AndroidPackage;
import jadx.gui.device.api.ApkPath;
import jadx.gui.device.api.DeviceExplorerException;
import jadx.gui.device.cli.dto.ContractDto;
import jadx.gui.device.cli.dto.ErrorDto;
import jadx.gui.device.cli.dto.PackageDto;
import jadx.gui.device.cli.dto.PullResultDto;
import jadx.gui.device.reports.DeviceReportWriter;
import jadx.gui.device.workspace.DexForgeWorkspaceLayout;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceExplorerCliContractTest {

	@TempDir
	Path tempDir;

	@Test
	void testPackageJsonContract() {
		AndroidPackage pkg = new AndroidPackage("id.net.cakramedia.attendance",
				"/data/app/~~abc/id.net.cakramedia.attendance/base.apk");

		PackageDto dto = DeviceExplorerJsonWriter.toPackageDto(pkg, 0);

		assertThat(dto.getPackageName()).isEqualTo("id.net.cakramedia.attendance");
		assertThat(dto.getLabel()).isEqualTo("id.net.cakramedia.attendance");
		assertThat(dto.getUserId()).isEqualTo(0);
		assertThat(dto.getType()).isEqualTo("user");
		assertThat(dto.getPath()).isEqualTo("/data/app/~~abc/id.net.cakramedia.attendance/base.apk");

		String jsonStr = DeviceExplorerJsonWriter.toJson(dto);
		@SuppressWarnings("unchecked")
		Map<String, Object> json = new com.google.gson.Gson().fromJson(jsonStr, Map.class);

		assertThat(json)
				.containsEntry("packageName", "id.net.cakramedia.attendance")
				.containsEntry("label", "id.net.cakramedia.attendance")
				.containsEntry("userId", 0.0)
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
		DexForgeWorkspaceLayout layout = new DexForgeWorkspaceLayout(workspace);

		PullResultDto dto = DeviceExplorerJsonWriter.createPullSummary(
				"id.net.cakramedia.attendance",
				"R58Nxxxx",
				0,
				layout,
				paths);

		assertThat(dto.getPackageName()).isEqualTo("id.net.cakramedia.attendance");
		assertThat(dto.getDeviceSerial()).isEqualTo("R58Nxxxx");
		assertThat(dto.getAndroidUser()).isEqualTo(0);
		assertThat(dto.getWorkspace()).isEqualTo(workspace.getAbsolutePath());
		assertThat(dto.getApks()).containsExactly("base.apk", "split_config.arm64_v8a.apk", "split_config.xxhdpi.apk");
		assertThat(dto.getReports()).containsEntry("pull", "reports/pull-report.json");

		String jsonStr = DeviceExplorerJsonWriter.toJson(dto);
		@SuppressWarnings("unchecked")
		Map<String, Object> json = new com.google.gson.Gson().fromJson(jsonStr, Map.class);

		assertThat(json)
				.containsEntry("packageName", "id.net.cakramedia.attendance")
				.containsEntry("deviceSerial", "R58Nxxxx")
				.containsEntry("androidUser", 0.0)
				.containsEntry("workspace", workspace.getAbsolutePath());
	}

	@Test
	void testErrorJsonContract() {
		ErrorDto dto = DeviceExplorerJsonWriter.createError("list-devices", new IllegalStateException("ADB failed"));

		assertThat(dto.getStatus()).isEqualTo("ERROR");
		assertThat(dto.getCommand()).isEqualTo("list-devices");
		assertThat(dto.getCode()).isEqualTo("INTERNAL_ERROR");
		assertThat(dto.getMessage()).isEqualTo("ADB failed");

		// Test mapping of DeviceExplorerException
		ErrorDto adbDto = DeviceExplorerJsonWriter.createError("list-devices", new DeviceExplorerException("device R58Nxxxx not found"));
		assertThat(adbDto.getCode()).isEqualTo("ADB_NOT_FOUND");
	}

	@Test
	void testContractJsonContract() {
		List<String> commands = List.of("contract", "list-devices");
		ContractDto dto = new ContractDto("1", commands);
		assertThat(dto.getApiVersion()).isEqualTo("1");
		assertThat(dto.getCommands()).containsExactly("contract", "list-devices");

		String jsonStr = DeviceExplorerJsonWriter.toJson(dto);
		@SuppressWarnings("unchecked")
		Map<String, Object> json = new com.google.gson.Gson().fromJson(jsonStr, Map.class);
		assertThat(json).containsEntry("apiVersion", "1");
	}

	@Test
	void testManifestReportContract() throws Exception {
		File decompiledDir = tempDir.resolve("decompiled").toFile();
		File resourcesDir = new File(decompiledDir, "resources");
		assertThat(resourcesDir.mkdirs()).isTrue();
		File manifest = new File(resourcesDir, "AndroidManifest.xml");
		assertThat(manifest.createNewFile()).isTrue();

		File reportFile = tempDir.resolve("reports/manifest.json").toFile();
		DeviceReportWriter.writeManifestReport(decompiledDir, reportFile);

		assertThat(reportFile).exists();
		try (FileReader reader = new FileReader(reportFile)) {
			JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
			assertThat(json.get("status").getAsString()).isEqualTo("FOUND");
			assertThat(json.get("path").getAsString()).isEqualTo("resources/AndroidManifest.xml");
			assertThat(json.get("absolutePath").getAsString()).isEqualTo(manifest.getAbsolutePath());
		}
	}
}
