# DexForge IDE Integration Quick Start

## Overview

DexForge provides machine-readable JSON contracts for IDE integrations. This guide covers how to integrate DexForge CLI with your IDE or editor.

## Binary Configuration

IDE extensions should use `dexforge` as the primary binary. Support custom paths for user configuration:

```json
{
  "dexforge.enginePath": "/usr/local/bin/dexforge"
}
```

Fallback order:
1. Custom path from settings
2. `dexforge` on PATH
3. `jadx` on PATH (compatibility)

## JSON CLI Contracts

### Device Explorer Contract

The `dexforge device-explorer` command provides a JSON contract for Android device operations.

```bash
dexforge device-explorer list-devices --format json
```

Output:
```json
[
  {
    "model": "Pixel 6",
    "serial": "emulator-5554",
    "status": "device"
  }
]
```

### LSP Daemon Mode

For code navigation and decompilation:

```bash
dexforge lsp --port 8080
```

Full API documented in [LSP_DAEMON_API.md](LSP_DAEMON_API.md).

## Integration Examples

### VS Code

See: https://github.com/damarkuncoro/dexforge-vscode

### IntelliJ IDEA

See: https://github.com/damarkuncoro/dexforge-intellij

## Error Handling

Always check exit codes and parse JSON errors:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "error": {
    "code": -32000,
    "message": "Decompiler not loaded"
  }
}
```