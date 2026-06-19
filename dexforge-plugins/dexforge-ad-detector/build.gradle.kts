plugins {
	id("jadx-library")
}

dependencies {
	api(project(":jadx-core"))
	implementation(project(":dexforge-commons:dexforge-app-commons"))
	implementation("com.google.code.gson:gson:2.13.2")
}
