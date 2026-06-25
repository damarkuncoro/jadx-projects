plugins {
	id("jadx-library")
}

dependencies {
	// API is the base module, it doesn't depend on Core or Engines.
	// Implementation details are injected via SPI or Registry.
}

tasks.named("check") {
	dependsOn("test")
}
