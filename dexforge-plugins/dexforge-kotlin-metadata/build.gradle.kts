plugins {
	id("jadx-library")
	id("jadx-kotlin")
}

dependencies {
	api(project(":jadx-core"))

	implementation("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.10")

	testImplementation(
		project
			.project(":jadx-core")
			.sourceSets
			.getByName("test")
			.output,
	)
	testImplementation("org.apache.commons:commons-lang3:3.20.0")

	testRuntimeOnly(project(":dexforge-plugins:dexforge-smali-input"))
	testRuntimeOnly(project(":dexforge-plugins:dexforge-java-input"))
}
