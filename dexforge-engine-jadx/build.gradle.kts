plugins {
	id("jadx-java")
	id("jadx-library")
}

dependencies {
	implementation(project(":jadx-core"))
	implementation(project(":dexforge-core"))
	implementation(project(":dexforge-api"))

	// Logging
	implementation("ch.qos.logback:logback-classic:1.5.21")
	implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")
}
