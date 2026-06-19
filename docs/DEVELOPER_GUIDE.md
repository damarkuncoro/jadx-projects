# DexForge Developer Guide

This guide is for developers who want to contribute to DexForge or extend it with custom plugins and integrations.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Setting Up Development Environment](#setting-up-development-environment)
3. [Building and Testing](#building-and-testing)
4. [Plugin Development](#plugin-development)
5. [Extending the LSP Daemon](#extending-the-lsp-daemon)
6. [Contributing Guidelines](#contributing-guidelines)

## Architecture Overview

DexForge is built on a modular architecture:

```
┌─────────────────────────────────────────────────────────────────┐
│                        User Interfaces                          │
├─────────────────────────────────────────────────────────────────┤
│  jadx-gui (Swing)  │  dexforge-cli  │  dexforge-vscode        │
└─────────────────────┴───────────────┴───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Application Layer                            │
├─────────────────────────────────────────────────────────────────┤
│  DaemonService │ DeviceExplorer │ FridaPanel │ LayoutViewer     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Plugin System                                │
├─────────────────────────────────────────────────────────────────┤
│  dexforge-plugins-tools (loader, registry, resolvers)           │
│  dexforge-plugins/* (input/output plugins)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Core Engine (JADX)                           │
├─────────────────────────────────────────────────────────────────┤
│  jadx-core (decompiler, codegen, dex parser)                    │
│  jadx-commons (shared utilities)                                │
└─────────────────────────────────────────────────────────────────┘
```

## Setting Up Development Environment

### Prerequisites

- **JDK 21+** (Temurin recommended)
- **Gradle 8+** (wrapper included)
- **Git**
- **Docker** (optional, for containerized builds)
- **Node.js 20+** (for VS Code extension development)
- **Frida** (for Frida integration development)

### Clone and Build

```bash
# Clone the repository
git clone https://github.com/damarkuncoro/jadx-projects.git
cd jadx-projects

# Copy environment configuration
cp .env.example .env

# Build the project
./gradlew build

# Run tests
./gradlew test

# Launch GUI
./gradlew :jadx-gui:run

# Run CLI
./gradlew :dexforge-cli:run --args="--help"
```

### IDE Setup

#### IntelliJ IDEA / Android Studio

1. Open the project root directory
2. Import as Gradle project
3. Ensure JDK 21+ is configured
4. Enable annotation processing

#### VS Code

1. Open the project root directory
2. Install recommended extensions:
   - Extension Pack for Java
   - Gradle Extension Pack
   - ESLint (for VS Code extension)

## Building and Testing

### Build Commands

```bash
# Full build with tests
./gradlew clean build

# Build without tests (faster)
./gradlew build -x test

# Build specific module
./gradlew :jadx-core:build

# Build distribution packages
./gradlew dist          # Linux/macOS
./gradlew distWin       # Windows

# Build all distributions
./gradlew dist distWin
```

### Test Commands

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :jadx-core:test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew :jadx-core:test --tests "jadx.tests.*"
```

### Code Quality

```bash
# Format code
./gradlew spotlessApply

# Check formatting
./gradlew spotlessCheck

# Check for dependency updates
./gradlew dependencyUpdates

# Update dependencies
./gradlew useLatestVersions
```

## Plugin Development

DexForge supports a plugin system for extending functionality.

### Plugin Structure

A DexForge plugin is a Gradle module that:

1. Depends on `dexforge-plugins-tools`
2. Implements plugin interfaces
3. Registers components via service loader

### Example Plugin

```java
// build.gradle.kts
plugins {
    id("jadx-java")
    id("jadx-library")
}

dependencies {
    implementation(project(":dexforge-plugins-tools"))
    implementation(project(":jadx-core"))
}

// src/main/resources/META-INF/services/jadx.plugins.tools.IPlugin
com.example.MyPlugin

// src/main/java/com/example/MyPlugin.java
package com.example;

import jadx.plugins.tools.IPlugin;
import jadx.plugins.tools.PluginContext;

public class MyPlugin implements IPlugin {
    @Override
    public void init(PluginContext context) {
        context.getLogger().info("MyPlugin initialized!");
        // Register custom components
    }

    @Override
    public String getName() {
        return "MyPlugin";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
```

### Plugin Registration

Plugins are auto-discovered via Java ServiceLoader:

```
src/main/resources/META-INF/services/
├── jadx.plugins.tools.IPlugin
└── jadx.plugins.tools.IPluginResolver
```

### Available Extension Points

- `IPlugin` - Base plugin interface
- `IPluginResolver` - Custom plugin source resolver
- `IPluginDownloader` - Custom download mechanism
- `IPluginMetadataLoader` - Custom metadata source
- `IPluginUnzipper` - Custom extraction logic

## Extending the LSP Daemon

The LSP daemon can be extended with custom methods.

### Adding a Custom Method

```java
// In LspService.java
public DaemonResponse customMethod(int requestId, Map<String, Object> params) {
    // Validate parameters
    String param = (String) params.get("param");
    if (param == null) {
        return DaemonResponse.error(requestId, "Missing 'param'");
    }

    // Process request
    String result = processCustomRequest(param);

    // Return response
    Map<String, Object> response = new HashMap<>();
    response.put("result", result);
    return DaemonResponse.success(requestId, response);
}
```

### Registering the Method

```java
// In DaemonCommandRouter.java
case "custom/method":
    return lspService.customMethod(requestId, params);
```

## Contributing Guidelines

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed contribution guidelines.

### Quick Checklist

- [ ] Code follows project style (Spotless)
- [ ] Tests added for new functionality
- [ ] Documentation updated
- [ ] CHANGELOG.md updated
- [ ] No breaking changes without discussion

## Resources

- [JADX Documentation](https://github.com/skylot/jadx/wiki)
- [Frida Documentation](https://frida.re/docs/)
- [LSP Specification](https://microsoft.github.io/language-server-protocol/)
- [Gradle Documentation](https://docs.gradle.org/)
