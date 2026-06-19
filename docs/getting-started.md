---
layout: default
title: Getting Started
nav_order: 2
---

# Getting Started

This guide will help you install and set up DexForge Engine for Android reverse engineering.

## Prerequisites

- **Java 11+** runtime (Java 21 recommended for building)
- **ADB** (Android Debug Bridge) for device operations
- **Frida** (optional) for dynamic instrumentation

## Installation

### Option 1: Download Pre-built Release

1. Go to [GitHub Releases](https://github.com/damarkuncoro/jadx-projects/releases/latest)
2. Download the appropriate package for your OS:
   - `dexforge-engine-<version>.zip` - Linux/macOS
   - `dexforge-gui-<version>-win.zip` - Windows
   - `dexforge-gui-<version>-with-jre-win.zip` - Windows with JRE
3. Extract the archive
4. Run `bin/dexforge` or `bin/dexforge-gui`

### Option 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/damarkuncoro/jadx-projects.git
cd jadx-projects

# Copy environment configuration
cp .env.example .env

# Build the project
./gradlew build

# Run CLI
./gradlew :dexforge-cli:run --args="--help"

# Run GUI
./gradlew :jadx-gui:run
```

### Option 3: Docker

```bash
# Build Docker image
docker build -t dexforge:latest -f docker/Dockerfile .

# Run CLI
docker run --rm -v "$(pwd):/workspace" dexforge:latest --help

# Run LSP daemon
docker run --rm -p 8080:8080 -v "$(pwd):/workspace" dexforge:latest lsp
```

## Quick Start

### Decompile an APK

```bash
# Basic decompilation
dexforge app.apk -d output/

# With resources
dexforge app.apk -d output/ --no-res

# Single class decompilation
dexforge --single-class com.example.MainActivity app.apk
```

### Use Device Explorer

```bash
# List connected devices
dexforge device-explorer list-devices

# List packages on a device
dexforge device-explorer list-packages SERIAL 0 user

# Pull and decompile an APK
dexforge device-explorer pull-and-decompile SERIAL com.example.app ./workspace 0
```

### Start LSP Daemon for IDE Integration

```bash
# Start daemon on default port
dexforge lsp

# Start on custom port
dexforge lsp --port 9090
```

## Configuration

### Environment Variables

| Variable | Description |
|----------|-------------|
| `JADX_CONFIG_DIR` | Custom config directory |
| `JADX_CACHE_DIR` | Custom cache directory |
| `JADX_TMP_DIR` | Custom temp directory |
| `JADX_BUILD_JAVA_VERSION` | Java version for building |
| `JADX_BUILD_CHECKS_MODE` | ErrorProne checks level (off/warn/error) |

### CLI Options

See [CLI Usage](CLI_USAGE.md) for complete reference.

## Next Steps

- [CLI Usage](CLI_USAGE.md) - Learn all CLI commands and options
- [LSP Daemon API](LSP_DAEMON_API.md) - Integrate with your IDE
- [Frida Integration](FRIDA_INTEGRATION.md) - Set up dynamic instrumentation
- [Device Explorer](DEVICE_EXPLORER_ROADMAP.md) - Work with Android devices
- [Security](SECURITY.md) - Understand legal and ethical boundaries

## Troubleshooting

### Common Issues

**"dexforge: command not found"**
- Make sure the `bin/` directory is in your PATH
- Or use the full path: `/path/to/dexforge-engine/bin/dexforge`

**"Failed to connect to LSP daemon"**
- Start the daemon first: `dexforge lsp`
- Check if port 8080 is available

**"ADB not found"**
- Install Android SDK Platform Tools
- Add `platform-tools/` to your PATH

### Getting Help

- [GitHub Issues](https://github.com/damarkuncoro/jadx-projects/issues)
- [GitHub Discussions](https://github.com/damarkuncoro/jadx-projects/discussions)
