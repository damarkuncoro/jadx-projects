# DexForge Engine Release Template

## DexForge Engine {version}

**Powered by JADX**

### Highlights

- [ ] Primary `dexforge` CLI command
- [ ] Primary `dexforge-gui` GUI launcher  
- [ ] {any breaking changes or new features}

### Installation

```bash
# Download and extract
curl -L https://github.com/damarkuncoro/jadx-projects/releases/download/{version}/dexforge-engine-{version}.zip -o dexforge-engine.zip
unzip dexforge-engine.zip

# Optional: Install to PATH
cd dexforge-engine-{version}
./scripts/install-to-path.sh  # Linux/macOS
scripts\install-to-path.bat   # Windows
```

### Compatibility

This release includes both DexForge and JADX compatibility aliases:
- `dexforge` - Primary CLI (recommended)
- `jadx` - Legacy compatibility alias
- `dexforge-gui` - Primary GUI launcher
- `jadx-gui` - Legacy compatibility GUI

Existing JADX workflows continue to work.

### CLI Changes

```bash
# Primary commands
dexforge --help          # Show help
dexforge device-explorer # Device operations
dexforge lsp             # JSON-RPC daemon mode

# Legacy aliases (still supported)
jadx --help
jadx-gui
```

### API Stability

- [ ] JSON-RPC daemon API stable
- [ ] Device Explorer contract stable
- [ ] Decompile output format stable

### Resources

- [Documentation](https://github.com/damarkuncoro/jadx-projects#readme)
- [IDE Integration](docs/IDE_INTEGRATION.md)
- [LSP Daemon API](docs/LSP_DAEMON_API.md)

---

*Licensed under Apache 2.0*