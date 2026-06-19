plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

if (!JavaVersion.current().isJava11Compatible) {
	throw GradleException("Jadx requires at least Java 11 for build (current version is '${JavaVersion.current()}')")
}

rootProject.name = "jadx"

include("jadx-core")
include("dexforge-cli")
include("jadx-gui")

include("dexforge-plugins-tools")

include("dexforge-commons:dexforge-app-commons")
include("dexforge-commons:dexforge-zip")

include("dexforge-plugins:dexforge-input-api")
include("dexforge-plugins:dexforge-dex-input")
include("dexforge-plugins:dexforge-java-input")
include("dexforge-plugins:dexforge-raung-input")
include("dexforge-plugins:dexforge-smali-input")
include("dexforge-plugins:dexforge-java-convert")
include("dexforge-plugins:dexforge-rename-mappings")
include("dexforge-plugins:dexforge-kotlin-metadata")
include("dexforge-plugins:dexforge-kotlin-source-debug-extension")
include("dexforge-plugins:dexforge-xapk-input")
include("dexforge-plugins:dexforge-aab-input")
include("dexforge-plugins:dexforge-apkm-input")
include("dexforge-plugins:dexforge-apks-input")
include("dexforge-plugins:dexforge-ad-detector")

// Frida Integration Module
include("dexforge-plugins:dexforge-frida-integration")
