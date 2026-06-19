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

include("jadx-plugins:dexforge-input-api")
include("jadx-plugins:dexforge-dex-input")
include("jadx-plugins:dexforge-java-input")
include("jadx-plugins:dexforge-raung-input")
include("jadx-plugins:dexforge-smali-input")
include("jadx-plugins:dexforge-java-convert")
include("jadx-plugins:dexforge-rename-mappings")
include("jadx-plugins:dexforge-kotlin-metadata")
include("jadx-plugins:dexforge-kotlin-source-debug-extension")
include("jadx-plugins:dexforge-xapk-input")
include("jadx-plugins:dexforge-aab-input")
include("jadx-plugins:dexforge-apkm-input")
include("jadx-plugins:dexforge-apks-input")
include("jadx-plugins:dexforge-ad-detector")

// Frida Integration Module
include("jadx-plugins:dexforge-frida-integration")
