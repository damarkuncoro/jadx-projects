plugins {
	id("jadx-library")
}

dependencies {
	api(project(":jadx-core"))

	api("net.fabricmc:mapping-io:0.8.0") {
		exclude("org.ow2.asm:asm")
		exclude("net.fabricmc:tiny-remapper")
	}

	testRuntimeOnly(project(":jadx-plugins:dexforge-dex-input"))
	testRuntimeOnly(project(":jadx-plugins:dexforge-smali-input"))
}
