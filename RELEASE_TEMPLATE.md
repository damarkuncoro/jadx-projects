# DexForge Engine Release Notes

## DexForge Engine v0.6.0-dev

**Powered by JADX**

### Highlights

**New Binary Names:**
- `dexforge` - Primary CLI command (recommended)
- `dexforge-gui` - Primary GUI launcher
- `jadx`, `jadx-gui` - Compatibility aliases still included

**Clean Architecture Layer:**
- New `dexforge-core/` module with DDD structure
- Domain models: Project, Device, SourceFile, SearchQuery
- Application use cases with unit tests
- Infrastructure adapters to jadx-core

**Documentation:**
- IDE Integration Quick Start guide
- Migration Guide for JADX users
- Release notes template for contributors

### Installation

```bash
# Download and extract
curl -L https://github.com/damarkuncoro/jadx-projects/releases/download/v0.6.0-dev/dexforge-engine-0.6.0-dev.zip -o dexforge-engine.zip
unzip dexforge-engine.zip

# Optional: Install to PATH
cd dexforge-engine-0.6.0-dev
./scripts/install-to-path.sh  # Linux/macOS
scripts\install-to-path.bat   # Windows
```

### Compatibility

| Old Command | New Command |
| --- | --- |
| `jadx app.apk` | `dexforge app.apk` |
| `jadx-gui` | `dexforge-gui` |
| `jadx device-explorer` | `dexforge device-explorer` |
| `jadx lsp` | `dexforge lsp` |

Legacy `jadx` commands remain available.

### CLI Commands

```bash
# Decompile APK
dexforge app.apk -d output/

# Device Explorer
dexforge device-explorer list-devices
dexforge device-explorer pull emulator-5554 com.example.app ./output

# LSP Daemon for IDE
dexforge lsp --port 8080
```

### Documentation

- [IDE Integration Quick Start](docs/IDE_INTEGRATION.md)
- [Migration Guide](docs/MIGRATION_GUIDE.md)
- [LSP Daemon API](docs/LSP_DAEMON_API.md)
- [Changelog](CHANGELOG.md)

---

*Licensed under Apache 2.0*

*Licensed under Apache 2.0*