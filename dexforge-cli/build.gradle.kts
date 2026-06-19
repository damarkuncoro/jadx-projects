plugins {
	id("jadx-java")
	id("jadx-library")
	id("application")

	// use shadow only for application scripts, jar will be copied from jadx-gui
	id("com.gradleup.shadow") version "8.3.8"
}

dependencies {
	implementation(project(":jadx-core"))
	implementation(project(":dexforge-plugins-tools"))
	implementation(project(":dexforge-commons:dexforge-app-commons"))

	runtimeOnly(project(":jadx-plugins:dexforge-dex-input"))
	runtimeOnly(project(":jadx-plugins:dexforge-java-input"))
	runtimeOnly(project(":jadx-plugins:dexforge-java-convert"))
	runtimeOnly(project(":jadx-plugins:dexforge-smali-input"))
	runtimeOnly(project(":jadx-plugins:dexforge-rename-mappings"))
	runtimeOnly(project(":jadx-plugins:dexforge-kotlin-metadata"))
	runtimeOnly(project(":jadx-plugins:dexforge-kotlin-source-debug-extension"))
	runtimeOnly(project(":jadx-plugins:dexforge-xapk-input"))
	runtimeOnly(project(":jadx-plugins:dexforge-aab-input"))
	runtimeOnly(project(":jadx-plugins:dexforge-apkm-input"))
	runtimeOnly(project(":jadx-plugins:dexforge-apks-input"))

	implementation("org.jcommander:jcommander:2.0")
	implementation("ch.qos.logback:logback-classic:1.5.32")
	implementation("com.google.code.gson:gson:2.13.2")
}

application {
	applicationName = "jadx"
	mainClass.set("dexforge.cli.DexforgeCLI")
	applicationDefaultJvmArgs =
		listOf(
			"-XX:+IgnoreUnrecognizedVMOptions",
			"-Xms256M",
			"-XX:MaxRAMPercentage=70.0",
			"-XX:ParallelGCThreads=3",
			// disable zip checks (#1962)
			"-Djdk.util.zip.disableZip64ExtraFieldValidation=true",
			// Foreign API access for 'directories' library (Windows only)
			"--enable-native-access=ALL-UNNAMED",
		)
	applicationDistribution.from("$rootDir") {
		include("README.md")
		include("NOTICE")
		include("LICENSE")
	}
}

tasks.shadowJar {
	// shadow jar not needed
	configurations = listOf()
}
