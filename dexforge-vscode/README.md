# DexForge - VS Code Extension

[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![VS Code](https://img.shields.io/badge/VS%20Code-1.85%2B-blue)](https://code.visualstudio.com/)

**DexForge** brings Android reverse engineering capabilities directly into VS Code. Decompile APK, DEX, and AAB files, navigate code symbols, and integrate with the DexForge LSP daemon for a seamless reverse engineering workflow.

## Features

### 🔍 Decompile & Navigate

- **Open APK/DEX/AAB files** directly in VS Code
- **Decompile to Java** with syntax highlighting
- **Go to Definition** - Navigate to class/method definitions
- **Find References** - See all usages of a symbol
- **Hover Information** - View method signatures and documentation
- **Workspace Symbol Search** - Quick search across all classes

### 🤖 LSP Daemon Integration

- Connects to local DexForge LSP daemon for high-performance decompilation
- JSON-RPC protocol for IDE automation
- Persistent decompiler instance for fast repeated queries

### 📱 Android Workflow

- **Device Explorer** - Browse connected Android devices
- **Pull APKs** - Extract APKs directly from devices
- **Quick Decompile** - One-click decompilation of device APKs

### 🎯 Smart Features

- **Symbol Resolution** - Accurate cross-reference navigation
- **Method Overload Support** - Correctly handles overloaded methods
- **Package Explorer** - Browse decompiled packages in tree view
- **Search Integration** - Search across all decompiled code

## Installation

### From VS Code Marketplace

1. Open VS Code
2. Go to Extensions (`Ctrl+Shift+X` / `Cmd+Shift+X`)
3. Search for **"DexForge"**
4. Click **Install**

### From VSIX

1. Download the `.vsix` file from [Releases](https://github.com/damarkuncoro/jadx-projects/releases)
2. Open VS Code
3. Go to Extensions
4. Click `...` → **Install from VSIX...**
5. Select the downloaded file

## Prerequisites

- **VS Code** 1.85 or later
- **DexForge CLI** installed and available in PATH
- **Java 11+** runtime

### Installing DexForge CLI

```bash
# Download from GitHub Releases
# Or build from source:
git clone https://github.com/damarkuncoro/jadx-projects.git
cd jadx-projects
./gradlew :dexforge-cli:installShadowDist

# Add to PATH
export PATH="$PATH:/path/to/jadx-projects/dexforge-cli/build/install/dexforge-cli/bin"
```

## Quick Start

### 1. Open an APK

```bash
# From command palette (Ctrl+Shift+P / Cmd+Shift+P)
> DexForge: Open APK

# Or drag and drop an APK file into VS Code
```

### 2. Start LSP Daemon

```bash
# Start the daemon (runs in background)
dexforge lsp

# Or use the VS Code command:
> DexForge: Start LSP Daemon
```

### 3. Navigate Decompiled Code

- Click on class names to navigate
- Right-click for context menu options
- Use `F12` to go to definition
- Use `Shift+F12` to find references

## Commands

| Command | Description |
|---------|-------------|
| `DexForge: Open APK` | Open and decompile an APK/DEX/AAB file |
| `DexForge: Start LSP Daemon` | Start the DexForge LSP daemon |
| `DexForge: Stop LSP Daemon` | Stop the running daemon |
| `DexForge: Decompile Current File` | Decompile the active file |
| `DexForge: Pull APK from Device` | Pull APK from connected Android device |
| `DexForge: Show Device Explorer` | Open device explorer panel |
| `DexForge: Generate Frida Hook` | Generate Frida hook for selected method |

## Configuration

Add to your `settings.json`:

```json
{
  "dexforge.cliPath": "/usr/local/bin/dexforge",
  "dexforge.lspPort": 8080,
  "dexforge.autoStartDaemon": true,
  "dexforge.decompileOnOpen": true,
  "dexforge.showDeviceExplorer": true,
  "dexforge.theme": "default"
}
```

### Settings Reference

| Setting | Default | Description |
|---------|---------|-------------|
| `dexforge.cliPath` | `"dexforge"` | Path to DexForge CLI executable |
| `dexforge.lspPort` | `8080` | Port for LSP daemon |
| `dexforge.autoStartDaemon` | `true` | Auto-start daemon when needed |
| `dexforge.decompileOnOpen` | `true` | Auto-decompile when opening APK |
| `dexforge.showDeviceExplorer` | `true` | Show device explorer in sidebar |
| `dexforge.theme` | `"default"` | Color theme for decompiled code |

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        VS Code                               │
├─────────────────────────────────────────────────────────────┤
│  DexForge Extension                                         │
│           │                                                 │
│           ├──► Content Provider (APK/DEX reader)            │
│           ├──► LSP Client (JSON-RPC over stdio/TCP)         │
│           ├──► Device Explorer (ADB integration)            │
│           └──► Command Handler (CLI wrapper)                │
│           │                                                 │
│           ▼                                                 │
│  DexForge CLI ──► LSP Daemon ──► jadx-core                  │
└─────────────────────────────────────────────────────────────┘
```

## Development

### Building from Source

```bash
# Clone the repository
git clone https://github.com/damarkuncoro/jadx-projects.git
cd jadx-projects/dexforge-vscode

# Install dependencies
npm install

# Compile
npm run compile

# Run in development mode
code --extensionDevelopmentPath=$(pwd)
```

### Project Structure

```
dexforge-vscode/
├── src/
│   ├── extension.js          # Extension entry point
│   ├── domain/               # Domain models
│   │   ├── class.js
│   │   ├── device.js
│   │   ├── package.js
│   │   └── user.js
│   └── presentation/
│       └── contentProvider.js # APK/DEX content provider
├── package.json              # Extension manifest
└── README.md
```

## Troubleshooting

### Daemon Won't Start

```bash
# Check if DexForge CLI is in PATH
which dexforge
dexforge --version

# Check if port is available
lsof -i :8080

# Start with debug logging
dexforge lsp --log-level debug
```

### APK Won't Open

- Ensure the file is a valid APK/DEX/AAB
- Check file permissions
- Try decompiling via CLI first: `dexforge input.apk -o output/`

### Slow Performance

- Ensure LSP daemon is running (not one-off CLI mode)
- Increase JVM heap: `export _JAVA_OPTIONS="-Xmx4g"`
- Use SSD for decompilation output

## Roadmap

- [ ] IntelliJ IDEA plugin
- [ ] Frida script generation from editor
- [ ] Binary diff viewer
- [ ] YARA rule integration
- [ ] Collaborative analysis sessions
- [ ] Cloud decompilation backend

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

## License

Apache 2.0 - See [LICENSE](../LICENSE)

## Related Projects

- [DexForge Engine](https://github.com/damarkuncoro/jadx-projects) - Main repository
- [JADX](https://github.com/skylot/jadx) - Upstream decompiler
- [Frida](https://frida.re/) - Dynamic instrumentation toolkit

## Support

- [GitHub Issues](https://github.com/damarkuncoro/jadx-projects/issues)
- [GitHub Discussions](https://github.com/damarkuncoro/jadx-projects/discussions)
