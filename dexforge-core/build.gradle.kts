plugins {
	id("jadx-java")
	id("jadx-library")
}

dependencies {
	implementation(project(":jadx-core"))

	// Logging
	implementation("ch.qos.logback:logback-classic:1.5.21")
	implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")

	// Utilities
	implementation("org.apache.commons:commons-lang3:3.20.0")
	implementation("commons-io:commons-io:2.21.0")

	testImplementation("org.mockito:mockito-core:5.2.0")
	testImplementation("org.assertj:assertj-core:3.24.1")
}

val checkDexForgeBoundaryImports by tasks.registering {
	group = "verification"
	description = "Ensures public DexForge domain and engine APIs do not import JADX internals."

	val checkedSources =
		files(
			fileTree("src/main/java/dexforge/domain") {
				include("**/*.java")
			},
			fileTree("src/main/java/dexforge/engine") {
				include("**/*.java")
			},
			fileTree("src/main/java/dexforge/application") {
				include("**/*.java")
			},
			fileTree("src/main/java/dexforge/core/application") {
				include("**/*.java")
			},
			fileTree("src/main/java/dexforge/core/ports") {
				include("**/*.java")
			},
		)
	inputs.files(checkedSources)

	doLast {
		val forbiddenTokens =
			listOf(
				"import jadx.",
				"jadx.api",
				"jadx.core",
				"JadxArgs",
				"JadxDecompiler",
				"ClassNode",
				"RootNode",
			)
		val violations =
			checkedSources.files
				.filter { it.isFile }
				.flatMap { file ->
					val text = file.readText()
					forbiddenTokens
						.filter { token -> text.contains(token) }
						.map { token -> "${file.relativeTo(projectDir)} contains forbidden JADX dependency token: $token" }
				}

		if (violations.isNotEmpty()) {
			throw GradleException(violations.joinToString(separator = System.lineSeparator()))
		}
	}
}

tasks.named("check") {
	dependsOn(checkDexForgeBoundaryImports)
}
