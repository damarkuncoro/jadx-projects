plugins {
	id("jadx-library")
	id("jadx-kotlin")
}

dependencies {
	api(project(":jadx-core"))

	implementation(project(":jadx-plugins:dexforge-dex-input"))
}
