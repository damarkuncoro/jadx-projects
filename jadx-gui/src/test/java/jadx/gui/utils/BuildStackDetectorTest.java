package jadx.gui.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jadx.gui.buildstack.BuildStackDetector;
import jadx.gui.buildstack.BuildStackInfo;
import jadx.gui.buildstack.FrameworkDetection;

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
		Files.createDirectories(resourcesDir.resolve("assets"));
		Files.createDirectories(sourcesDir.resolve("okhttp3"));
		Files.createDirectories(sourcesDir.resolve("retrofit2"));
		Files.createDirectories(sourcesDir.resolve("dagger"));
		Files.createDirectories(sourcesDir.resolve("org/cocos2dx"));
		Files.createDirectories(sourcesDir.resolve("com/epicgames/ue4"));
		Files.createDirectories(sourcesDir.resolve("org/koin"));
		Files.createDirectories(sourcesDir.resolve("io/reactivex"));
		Files.createDirectories(sourcesDir.resolve("com/bumptech/glide"));
		Files.createDirectories(sourcesDir.resolve("com/airbnb/lottie"));

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
		write(resourcesDir.resolve("assets/tauri.conf.json"), "{}");
		write(resourcesDir.resolve("libtauri.so"), "");
		write(resourcesDir.resolve("libcocos2d.so"), "");
		write(resourcesDir.resolve("libUE4.so"), "");
		write(resourcesDir.resolve("META-INF/androidx.core_core-ktx.version"), "1.13.1");
		write(resourcesDir.resolve("META-INF/androidx.compose.ui_ui.version"), "1.6.0");
		write(resourcesDir.resolve("META-INF/androidx.room_room-runtime.version"), "2.6.1");
		write(resourcesDir.resolve("META-INF/com.google.firebase_firebase-analytics.version"), "22.0.0");
		write(resourcesDir.resolve("META-INF/com.squareup.retrofit2_retrofit.version"), "2.11.0");
		write(resourcesDir.resolve("META-INF/com.squareup.okhttp3_okhttp.version"), "4.12.0");
		write(resourcesDir.resolve("META-INF/com.google.dagger_hilt-android.version"), "2.51.1");
		write(resourcesDir.resolve("META-INF/org.insert-koin_koin-core.version"), "3.5.0");
		write(resourcesDir.resolve("META-INF/io.reactivex.rxjava3_rxjava.version"), "3.1.8");
		write(resourcesDir.resolve("META-INF/com.github.bumptech.glide_glide.version"), "4.16.0");
		write(resourcesDir.resolve("META-INF/com.airbnb.android_lottie.version"), "6.4.0");
		write(resourcesDir.resolve("META-INF/proguard/coroutines.pro"), "-keep class kotlinx.coroutines.**");
		write(sourcesDir.resolve("okhttp3/OkHttpClient.java"), "package okhttp3; public class OkHttpClient {}");
		write(sourcesDir.resolve("retrofit2/Retrofit.java"), "package retrofit2; public class Retrofit {}");
		write(sourcesDir.resolve("dagger/Component.java"), "package dagger; public @interface Component {}");
		write(sourcesDir.resolve("org/cocos2dx/Cocos2dxActivity.java"), "package org.cocos2dx; public class Cocos2dxActivity {}");
		write(sourcesDir.resolve("com/epicgames/ue4/UE4Activity.java"), "package com.epicgames.ue4; public class UE4Activity {}");
		write(sourcesDir.resolve("org/koin/Core.java"), "package org.koin; public class Core {}");
		write(sourcesDir.resolve("io/reactivex/Observable.java"), "package io.reactivex; public class Observable {}");
		write(sourcesDir.resolve("com/bumptech/glide/Glide.java"), "package com.bumptech.glide; public class Glide {}");
		write(sourcesDir.resolve("com/airbnb/lottie/LottieAnimationView.java"),
				"package com.airbnb.lottie; public class LottieAnimationView {}");

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
		assertDetected(frameworks, "Tauri");
		assertDetected(frameworks, "Cocos2d");
		assertDetected(frameworks, "Unreal Engine");
		assertDetected(frameworks, "Koin");
		assertDetected(frameworks, "RxJava");
		assertDetected(frameworks, "Glide");
		assertDetected(frameworks, "Lottie");
		assertDetected(frameworks, "WebView / Hybrid");
		assertDetected(frameworks, "R8 / ProGuard");
		assertNotDetected(frameworks, "Flutter");
		assertThat(info.getBuildMetadata()).containsEntry("buildSystemVersion", "8.9");
		assertThat(info.getManifest()).containsEntry("package", "com.example.app");
		assertThat(info.getLibraryVersions()).containsEntry("androidx.room_room-runtime", "2.6.1");
		assertThat(info.getLibraryVersions()).containsEntry("org.insert-koin_koin-core", "3.5.0");
		assertThat(info.getLibraryVersions()).containsEntry("io.reactivex.rxjava3_rxjava", "3.1.8");
		assertThat(info.getLibraryVersions()).containsEntry("com.github.bumptech.glide_glide", "4.16.0");
		assertThat(info.getLibraryVersions()).containsEntry("com.airbnb.android_lottie", "6.4.0");
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
