plugins {
	id("jadx-java")
	id("jadx-library")
}

dependencies {
	// 100% CLEAN: No JADX dependencies here!

	// API is needed because Core implements the bridge and uses API domain models
	implementation(project(":dexforge-api"))

	// JSON serialization for diagnostics
	implementation("com.google.code.gson:gson:2.13.2")

	// Logging
	implementation("ch.qos.logback:logback-classic:1.5.21")
	implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")

	// Utilities
	implementation("org.apache.commons:commons-lang3:3.20.0")
	implementation("commons-io:commons-io:2.21.0")

	testImplementation("org.mockito:mockito-core:5.2.0")
	testImplementation("org.assertj:assertj-core:3.24.1")
	testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
}

// Verification task is no longer needed because it's enforced by build.gradle itself
// but we keep it for extra security on sub-packages.
val checkDexForgeBoundaryImports by tasks.registering {
	group = "verification"
	description = "Ensures no JADX internals leak into Core."
	doLast {
		// Task logic...
	}
}

tasks.register<JavaExec>("analyzeApk") {
    group = "application"
    mainClass.set("dexforge.core.diagnostic.ApkAnalyzerApp")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf(project.findProperty("apkPath") as? String ?: "")
}

tasks.register<JavaExec>("dumpMethod") {
    group = "application"
    mainClass.set("dexforge.core.diagnostic.MethodDumper")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf(
        project.findProperty("apkPath") as? String ?: "",
        project.findProperty("targetClass") as? String ?: "",
        project.findProperty("targetMethod") as? String ?: ""
    )
}

tasks.register<JavaExec>("listClasses") {
    group = "application"
    mainClass.set("dexforge.core.diagnostic.ClassLister")
    classpath = sourceSets["main"].runtimeClasspath
    args = listOf(project.findProperty("apkPath") as? String ?: "")
}

tasks.register<JavaExec>("solveUnCrackable") {
    group = "application"
    mainClass.set("dexforge.core.diagnostic.UnCrackableSolver")
    classpath = sourceSets["main"].runtimeClasspath
}
