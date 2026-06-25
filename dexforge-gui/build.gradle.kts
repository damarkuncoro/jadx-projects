plugins {
	id("jadx-java")
	id("application")
	id("jadx-library")
}

dependencies {
	implementation(project(":dexforge-api"))
	implementation(project(":dexforge-core"))
	implementation(project(":dexforge-engine-jadx"))
	implementation(project(":dexforge-commons:dexforge-app-commons"))

	// UI Framework: Swing + FlatLaf (Modern Look)
	implementation("com.formdev:flatlaf:3.5.4")
	implementation("com.formdev:flatlaf-extras:3.5.4")
	implementation("com.fifesoft:rsyntaxtextarea:3.5.2")

	// Logging
	implementation("ch.qos.logback:logback-classic:1.5.21")
	implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
}

application {
	mainClass.set("dexforge.gui.DexForgeGuiApp")
	applicationName = "dexforge-gui"
}
