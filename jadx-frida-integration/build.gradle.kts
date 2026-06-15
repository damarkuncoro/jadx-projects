plugins {
	id("jadx-library")
}

dependencies {
	api(project(":jadx-core"))
	api(project(":jadx-plugins:jadx-input-api"))
}
