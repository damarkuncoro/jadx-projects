plugins {
	id("jadx-library")
}

dependencies {
	api(project(":jadx-core"))
}

tasks.named("check") {
	dependsOn("test")
}
