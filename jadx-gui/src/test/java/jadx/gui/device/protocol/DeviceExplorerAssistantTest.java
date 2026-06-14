package jadx.gui.device.protocol;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceExplorerAssistantTest {

	private Path tempDir;
	private File outputDir;
	private File reportFile;

	@BeforeEach
	void setUp() throws IOException {
		tempDir = Files.createTempDirectory("jadx_assistant_test");
		outputDir = tempDir.resolve("com.example.app").toFile();
		outputDir.mkdirs();

		reportFile = new File(outputDir, "reports/assistant-report.json");
	}

	@AfterEach
	void tearDown() throws IOException {
		if (Files.exists(tempDir)) {
			try (Stream<Path> walk = Files.walk(tempDir)) {
				walk.sorted(Comparator.reverseOrder())
						.map(Path::toFile)
						.forEach(File::delete);
			}
		}
	}

	@Test
	void testSecurityAssistantAnalysis() throws IOException {
		// 1. Create mock Java source files
		File sourcesDir = new File(outputDir, "sources");
		File pkgDir = new File(sourcesDir, "com/bca");
		pkgDir.mkdirs();

		File mainJava = new File(pkgDir, "Main.java");
		String mainContent = "package com.bca;\n"
				+ "import javax.crypto.Cipher;\n"
				+ "public class Main {\n"
				+ "    private static final String API = \"https://api.bca.co.id/v1/auth\";\n"
				+ "    private static final String DATA = \"SGVsbG8gd29ybGQgYmFzZTY0IHN0cmluZyE=\";\n" // valid base64
				+ "    public void doCrypto() throws Exception {\n"
				+ "        // JADX WARNING: Method dump skipped\n"
				+ "        Cipher cipher = Cipher.getInstance(\"AES/CBC/PKCS5Padding\");\n"
				+ "    }\n"
				+ "}\n";
		try (FileWriter fw = new FileWriter(mainJava)) {
			fw.write(mainContent);
		}

		// Obfuscated class
		File obfuscatedPkg = new File(sourcesDir, "a/b");
		obfuscatedPkg.mkdirs();
		File obfuscatedJava = new File(obfuscatedPkg, "c.java");
		try (FileWriter fw = new FileWriter(obfuscatedJava)) {
			fw.write("package a.b; public class c {}");
		}

		// 2. Create mock resources
		File resourcesDir = new File(outputDir, "resources");
		resourcesDir.mkdirs();

		File gServicesJson = new File(resourcesDir, "google-services.json");
		String jsonContent = "{\n"
				+ "  \"project_info\": {\n"
				+ "    \"project_id\": \"my-bca-project\",\n"
				+ "    \"firebase_url\": \"https://my-bca-project.firebaseio.com\",\n"
				+ "    \"storage_bucket\": \"my-bca-project.appspot.com\"\n"
				+ "  },\n"
				+ "  \"client\": [\n"
				+ "    {\n"
				+ "      \"client_info\": {\n"
				+ "        \"mobilesdk_app_id\": \"1:12345:android:abcde\"\n"
				+ "      },\n"
				+ "      \"api_key\": [\n"
				+ "        {\n"
				+ "          \"current_key\": \"AIzaSyFakeKey123\"\n"
				+ "        }\n"
				+ "      ]\n"
				+ "    }\n"
				+ "  ]\n"
				+ "}";
		try (FileWriter fw = new FileWriter(gServicesJson)) {
			fw.write(jsonContent);
		}

		File stringsXml = new File(new File(resourcesDir, "res/values"), "strings.xml");
		stringsXml.getParentFile().mkdirs();
		String xmlContent = "<resources>\n"
				+ "    <string name=\"google_api_key\">AIzaSyStringsXmlKey</string>\n"
				+ "</resources>\n";
		try (FileWriter fw = new FileWriter(stringsXml)) {
			fw.write(xmlContent);
		}

		File manifest = new File(resourcesDir, "AndroidManifest.xml");
		String manifestContent = "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
				+ "    android:versionCode=\"12\"\n"
				+ "    android:versionName=\"1.2.3\"\n"
				+ "    android:compileSdkVersion=\"35\"\n"
				+ "    package=\"com.example.app\"\n"
				+ "    platformBuildVersionName=\"15\">\n"
				+ "    <uses-sdk android:minSdkVersion=\"26\" android:targetSdkVersion=\"35\"/>\n"
				+ "    <application android:name=\"com.example.App\"/>\n"
				+ "</manifest>\n";
		try (FileWriter fw = new FileWriter(manifest)) {
			fw.write(manifestContent);
		}

		File layoutDir = new File(resourcesDir, "res/layout");
		layoutDir.mkdirs();

		File kotlinMetadata = new File(resourcesDir, "kotlin-tooling-metadata.json");
		String kotlinMetadataContent = "{\n"
				+ "  \"schemaVersion\": \"1.1.0\",\n"
				+ "  \"buildSystem\": \"Gradle\",\n"
				+ "  \"buildSystemVersion\": \"8.9\",\n"
				+ "  \"buildPlugin\": \"org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper\",\n"
				+ "  \"buildPluginVersion\": \"1.9.0\",\n"
				+ "  \"projectTargets\": [\n"
				+ "    {\n"
				+ "      \"target\": \"org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget\",\n"
				+ "      \"platformType\": \"androidJvm\",\n"
				+ "      \"extras\": {\n"
				+ "        \"android\": {\n"
				+ "          \"sourceCompatibility\": \"17\",\n"
				+ "          \"targetCompatibility\": \"17\"\n"
				+ "        }\n"
				+ "      }\n"
				+ "    }\n"
				+ "  ]\n"
				+ "}";
		try (FileWriter fw = new FileWriter(kotlinMetadata)) {
			fw.write(kotlinMetadataContent);
		}

		File metaInf = new File(resourcesDir, "META-INF");
		metaInf.mkdirs();
		try (FileWriter fw = new FileWriter(new File(metaInf, "androidx.core_core-ktx.version"))) {
			fw.write("1.9.0");
		}

		// 3. Run analysis
		DeviceExplorerAssistant.runAnalysis(outputDir, reportFile);

		// 4. Verify report file
		assertThat(reportFile).exists();
		try (FileReader fr = new FileReader(reportFile)) {
			JsonObject report = JsonParser.parseReader(fr).getAsJsonObject();

			assertThat(report.get("packageName").getAsString()).isEqualTo("com.example.app");
			assertThat(report.has("generatedAt")).isTrue();

			// Obfuscation Summary (totalClasses = 2: Main.java and c.java. c.java is obfuscated.
			// obfuscationPercentage = 50%)
			JsonObject obf = report.getAsJsonObject("obfuscationSummary");
			assertThat(obf.get("totalClasses").getAsInt()).isEqualTo(2);
			assertThat(obf.get("obfuscatedClasses").getAsInt()).isEqualTo(1);
			assertThat(obf.get("obfuscationPercentage").getAsDouble()).isEqualTo(50.0);

			JsonObject buildStack = report.getAsJsonObject("buildStack");
			assertThat(buildStack.get("summary").getAsString())
					.contains("Native Android", "Gradle", "KotlinAndroidPluginWrapper", "compileSdk 35", "targetSdk 35");
			JsonObject buildMetadata = buildStack.getAsJsonObject("buildMetadata");
			assertThat(buildMetadata.get("buildSystem").getAsString()).isEqualTo("Gradle");
			assertThat(buildMetadata.get("buildSystemVersion").getAsString()).isEqualTo("8.9");
			assertThat(buildMetadata.get("buildPluginVersion").getAsString()).isEqualTo("1.9.0");
			assertThat(buildMetadata.get("platformType").getAsString()).isEqualTo("androidJvm");
			JsonObject manifestReport = buildStack.getAsJsonObject("manifest");
			assertThat(manifestReport.get("package").getAsString()).isEqualTo("com.example.app");
			assertThat(manifestReport.get("versionName").getAsString()).isEqualTo("1.2.3");
			assertThat(manifestReport.get("targetSdkVersion").getAsString()).isEqualTo("35");
			assertThat(buildStack.getAsJsonArray("frameworks").toString()).contains("Native Android", "AndroidX / Jetpack");
			assertThat(buildStack.getAsJsonObject("libraryVersions").get("androidx.core_core-ktx").getAsString())
					.isEqualTo("1.9.0");

			// Endpoints
			assertThat(report.getAsJsonArray("endpoints")).hasSize(1);
			JsonObject endpoint = report.getAsJsonArray("endpoints").get(0).getAsJsonObject();
			assertThat(endpoint.get("url").getAsString()).isEqualTo("https://api.bca.co.id/v1/auth");
			assertThat(endpoint.get("file").getAsString()).contains("Main.java");

			// Firebase Config (google-services.json overrides strings.xml or merges)
			JsonObject fb = report.getAsJsonObject("firebaseConfig");
			assertThat(fb.get("projectId").getAsString()).isEqualTo("my-bca-project");
			assertThat(fb.get("apiKey").getAsString()).isEqualTo("AIzaSyFakeKey123");
			assertThat(fb.get("appId").getAsString()).isEqualTo("1:12345:android:abcde");
			assertThat(fb.get("databaseUrl").getAsString()).isEqualTo("https://my-bca-project.firebaseio.com");

			// Crypto Usage
			assertThat(report.getAsJsonArray("cryptoUsage")).hasSize(1);
			JsonObject crypto = report.getAsJsonArray("cryptoUsage").get(0).getAsJsonObject();
			assertThat(crypto.get("file").getAsString()).contains("Main.java");
			assertThat(crypto.getAsJsonArray("findings").toString()).contains("Cipher", "AES");

			// Base64
			assertThat(report.getAsJsonArray("base64Strings")).hasSize(1);
			JsonObject b64 = report.getAsJsonArray("base64Strings").get(0).getAsJsonObject();
			assertThat(b64.get("string").getAsString()).isEqualTo("SGVsbG8gd29ybGQgYmFzZTY0IHN0cmluZyE=");

			// Failed Methods
			assertThat(report.getAsJsonArray("failedMethods")).hasSize(1);
			JsonObject failed = report.getAsJsonArray("failedMethods").get(0).getAsJsonObject();
			assertThat(failed.get("error").getAsString()).contains("JADX WARNING: Method dump skipped");
			assertThat(failed.get("context").getAsString()).contains("public void doCrypto()");
		}
	}
}
