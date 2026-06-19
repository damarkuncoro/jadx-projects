plugins {
	id("jadx-library")
}

dependencies {
	api(project(":jadx-core"))
	api(project(":jadx-plugins:dexforge-input-api"))
	implementation("org.apache.commons:commons-text:1.15.0")
}
