plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
    id("org.jetbrains.kotlin.jvm") version "1.9.22" apply false
}

group = "com.dexforge"
version = "0.1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://www.jetbrains.com/intellij-repository/releases") }
    maven { url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies") }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("junit:junit:4.13.2")
}

intellij {
    version.set("2024.1")
    type.set("IC") // IntelliJ IDEA Community Edition
    plugins.set(listOf("java", "Kotlin"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("252.*")
        changeNotes.set("""
            <h2>DexForge IntelliJ Plugin - Initial Release</h2>
            <ul>
                <li>Decompile APK/DEX/JAR files directly in IntelliJ IDEA</li>
                <li>LSP daemon integration for fast decompilation</li>
                <li>Device Explorer for ADB-connected Android devices</li>
                <li>Frida script generation from decompiled code</li>
            </ul>
        """.trimIndent())
    }

    buildSearchableOptions {
        enabled.set(false)
    }
}
