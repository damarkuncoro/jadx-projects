---
layout: default
title: DexForge Documentation
nav_order: 1
---

# DexForge Documentation

Welcome to the official documentation for **DexForge Engine** - an Android reverse engineering workbench powered by JADX.

## What is DexForge?

DexForge extends JADX with modern workflows for APK analysis, device extraction, Frida scripting, binary/resource inspection, IDE automation, and Android layout preview.

## Quick Links

- [Getting Started](getting-started.md) - Installation and setup guide
- [CLI Usage](CLI_USAGE.md) - Command-line reference
- [LSP Daemon API](LSP_DAEMON_API.md) - IDE integration protocol
- [Frida Integration](FRIDA_INTEGRATION.md) - Dynamic instrumentation guide
- [Device Explorer](DEVICE_EXPLORER_ROADMAP.md) - ADB device workflows
- [Security](SECURITY.md) - Security guidelines

## Features

- **Decompile and Inspect** - Convert APK, DEX, AAB, AAR, JAR, class, smali, XAPK, APKM, and APKS inputs to Java or JSON
- **DexForge Device Explorer** - Pull base and split APKs directly from connected Android devices
- **DexForge Layout Viewer** - Preview Android XML layout structures with visual previews
- **Frida Integration** - Generate Frida hooks directly from GUI decompiler context
- **Automation Ready** - Expose high-performance JSON-RPC decompiler daemon mode for IDE integrations

## Installation

### Download

Download packaged releases from [GitHub Releases](https://github.com/damarkuncoro/jadx-projects/releases/latest).

### Quick Start

```bash
# Extract the distribution zip
unzip dexforge-engine-<version>.zip
cd dexforge-engine-<version>

# Run CLI
./bin/dexforge --help

# Run GUI
./bin/dexforge-gui
```

## Project Status

> **Alpha / Development Preview**
> DexForge is actively developed. Some features, commands, or APIs are experimental and subject to change.

## License

Apache 2.0 - See [LICENSE](https://github.com/damarkuncoro/jadx-projects/blob/main/LICENSE)
