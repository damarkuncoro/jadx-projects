plugins {
	id("jadx-library")
}

dependencies {
	// Public API must not leak JADX types
	implementation(project(":dexforge-core"))

	testImplementation(project(":jadx-core"))
}

tasks.named("check") {
	dependsOn("test")
}
