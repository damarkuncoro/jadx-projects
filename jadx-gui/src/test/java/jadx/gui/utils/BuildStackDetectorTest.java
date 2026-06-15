package jadx.gui.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jadx.gui.utils.BuildStackDetector.BuildStackInfo;
import jadx.gui.utils.BuildStackDetector.FrameworkDetection;

import static org.assertj.core.api.Assertions.assertThat;

class BuildStackDetectorTest {

	@TempDir
	Path tempDir;

	@Test
	void detectBuildStackFromExportedProject() throws IOException {
		Path outputDir = tempDir.resolve("com.example.app");
		Path resourcesDir = outputDir.resolve("resources");
		Path sourcesDir = outputDir.resolve("sources");
		Files.createDirectories(resourcesDir.resolve("META-INF/proguard"));
		Files.createDirectories(resourcesDir.resolve("assets/www"));
		Files.createDirectories(sourcesDir.resolve("okhttp3"));
		Files.createDirectories(sourcesDir.resolve("retrofit2"));
		Files.createDirectories(sourcesDir.resolve("dagger"));

		write(resourcesDir.resolve("AndroidManifest.xml"), "<manifest package=\"com.example.app\" "
				+ "android:compileSdkVersion=\"35\"><uses-sdk android:minSdkVersion=\"26\" "
				+ "android:targetSdkVersion=\"35\"/><application android:name=\"com.example.App\"/></manifest>");
		write(resourcesDir.resolve("kotlin-tooling-metadata.json"), "{"
				+ "\"buildSystem\":\"Gradle\","
				+ "\"buildSystemVersion\":\"8.9\","
				+ "\"buildPlugin\":\"org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper\","
				+ "\"buildPluginVersion\":\"1.9.0\","
				+ "\"projectTargets\":[{\"target\":\"org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget\","
				+ "\"platformType\":\"androidJvm\",\"extras\":{\"android\":{\"sourceCompatibility\":\"17\","
				+ "\"targetCompatibility\":\"17\"}}}]}");
		write(resourcesDir.resolve("google-services.json"), "{}");
		write(resourcesDir.resolve("assets/www/index.html"), "<html></html>");
		write(resourcesDir.resolve("META-INF/androidx.core_core-ktx.version"), "1.13.1");
		write(resourcesDir.resolve("META-INF/androidx.compose.ui_ui.version"), "1.6.0");
		write(resourcesDir.resolve("META-INF/androidx.room_room-runtime.version"), "2.6.1");
		write(resourcesDir.resolve("META-INF/com.google.firebase_firebase-analytics.version"), "22.0.0");
		write(resourcesDir.resolve("META-INF/com.squareup.retrofit2_retrofit.version"), "2.11.0");
		write(resourcesDir.resolve("META-INF/com.squareup.okhttp3_okhttp.version"), "4.12.0");
		write(resourcesDir.resolve("META-INF/com.google.dagger_hilt-android.version"), "2.51.1");
		write(resourcesDir.resolve("META-INF/proguard/coroutines.pro"), "-keep class kotlinx.coroutines.**");
		write(sourcesDir.resolve("okhttp3/OkHttpClient.java"), "package okhttp3; public class OkHttpClient {}");
		write(sourcesDir.resolve("retrofit2/Retrofit.java"), "package retrofit2; public class Retrofit {}");
		write(sourcesDir.resolve("dagger/Component.java"), "package dagger; public @interface Component {}");

		BuildStackInfo info = BuildStackDetector.analyzeExportedProject(outputDir.toFile());
		Map<String, FrameworkDetection> frameworks = info.getFrameworks().stream()
				.collect(Collectors.toMap(FrameworkDetection::getName, framework -> framework));

		assertThat(info.getSummary())
				.contains("Native Android", "Gradle", "KotlinAndroidPluginWrapper", "compileSdk 35", "targetSdk 35");
		assertDetected(frameworks, "Native Android");
		assertDetected(frameworks, "Kotlin runtime");
		assertDetected(frameworks, "AndroidX / Jetpack");
		assertDetected(frameworks, "Jetpack Compose");
		assertDetected(frameworks, "Room");
		assertDetected(frameworks, "Firebase");
		assertDetected(frameworks, "Retrofit");
		assertDetected(frameworks, "OkHttp");
		assertDetected(frameworks, "Dagger / Hilt");
		assertDetected(frameworks, "WebView / Hybrid");
		assertDetected(frameworks, "R8 / ProGuard");
		assertNotDetected(frameworks, "Flutter");
		assertThat(info.getBuildMetadata()).containsEntry("buildSystemVersion", "8.9");
		assertThat(info.getManifest()).containsEntry("package", "com.example.app");
		assertThat(info.getLibraryVersions()).containsEntry("androidx.room_room-runtime", "2.6.1");
		assertThat(info.toMap().get("evidence").toString())
				.contains("resources/google-services.json", "resources/META-INF/proguard/coroutines.pro");
	}

	private static void assertDetected(Map<String, FrameworkDetection> frameworks, String name) {
		assertThat(frameworks).containsKey(name);
		assertThat(frameworks.get(name).getStatus()).isEqualTo("DETECTED");
		assertThat(frameworks.get(name).getEvidence()).isNotEmpty();
	}

	private static void assertNotDetected(Map<String, FrameworkDetection> frameworks, String name) {
		assertThat(frameworks).containsKey(name);
		assertThat(frameworks.get(name).getStatus()).isEqualTo("NOT_DETECTED");
		assertThat(frameworks.get(name).getEvidence()).isEmpty();
	}

	private static void write(Path file, String content) throws IOException {
		File parent = file.toFile().getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
		Files.writeString(file, content);
	}
}
