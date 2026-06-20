# JADX to DexForge Migration Guide

This guide helps existing JADX users transition to DexForge.

## Quick Migration

Most JADX workflows continue to work unchanged in DexForge:

| Old Command | New Command | Notes |
|-------------|-------------|-------|
| `jadx app.apk` | `dexforge app.apk` | CLI renamed to `dexforge` |
| `jadx-gui` | `dexforge-gui` | GUI renamed to `dexforge-gui` |
| `jadx device-explorer` | `dexforge device-explorer` | Device explorer integrated |

## What Changed

### CLI Branding
- Help banner shows "DexForge CLI, powered by JADX"
- Binary names: `dexforge` (primary), `jadx` (compatibility alias)
- Same arguments and options as upstream JADX

### GUI Branding
- Window title: "DexForge GUI"
- About dialog shows "DexForge GUI, powered by JADX"
- Menu: "DexForge Device Explorer" instead of generic names

### New Features
- `dexforge lsp` - JSON-RPC daemon mode for IDE integrations
- Device Explorer CLI: `dexforge device-explorer list-devices`
- Frida panel integration in GUI
- Layout Viewer integration in GUI

## Compatibility Guarantees

### What Remains Unchanged
- Internal package namespaces: `jadx.*`, `jadx.core.*`, `jadx.api.*`
- Java API: `jadx.api.JadxDecompiler`, etc.
- Plugin API: Plugins continue to work unchanged
- Configuration files: `jadx` prefix remains for compatibility

### Deprecation Timeline
- `jadx` and `jadx-gui` binaries: No plans to remove (long-term compatibility)
- Internal `jadx.*` packages: Remain unchanged until major version boundary

## Migration Steps

### 1. Install DexForge
```bash
# Download dexforge-engine-{version}.zip
# Extract to your preferred location

# Optional: Add to PATH
./scripts/install-to-path.sh  # Linux/macOS
scripts\install-to-path.bat   # Windows
```

### 2. Update Scripts (if needed)
Replace `jadx` references in your automation scripts (optional, `jadx` alias still works):

```bash
# Before
jadx app.apk -d output/

# After (recommended)
dexforge app.apk -d output/
```

### 3. IDE Integration (if used)
Update IDE extension settings to point to `dexforge` binary:
- VS Code: `dexforge-vscode` extension
- IntelliJ: `dexforge-intellij` plugin (planned)

## FAQ

### Q: Do my existing JADX plugins still work?
A: Yes. Plugins using `jadx.api.*` continue to work unchanged.

### Q: Will `jadx` command be removed?
A: Not planned. It's kept as a compatibility alias indefinitely.

### Q: Where are config and cache directories?
A: Same locations as JADX:
- `~/.config/jadx/` - Configuration
- `~/.cache/jadx/` - Cache

DexForge can use `DEXFORGE_CONFIG_DIR` as an optional alias.