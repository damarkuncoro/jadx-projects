# DexForge VS Code Extension (Alpha)

This is the official VS Code integration for the **DexForge Android Reverse Engineering Workbench**. It connects VS Code to the background `dexforge lsp` daemon, providing Java/smali decompiler support and ADB package querying.

## Features

- **Automatic Decompiler Daemon Startup**: Spawns `dexforge lsp` on startup to keep a single JVM decompiler process active, ensuring sub-millisecond query responses.
- **Language Server Protocol (LSP)**: Integrates hover signatures, go-to-definition, reference tracking, and workspace symbol search directly into VS Code for decompiled source outputs.
- **Android Device Explorer**: Run command `DexForge: Open Device Explorer` to detect connected physical or virtual Android devices via ADB, list installed packages, and select targets.

## Setup & Running

### 1. Build the DexForge Engine
Ensure you have built the engine in the main repository:
```bash
./gradlew dist
```
Add the `build/jadx/bin/` folder to your system `PATH` so that the `dexforge` executable command is globally accessible.

### 2. Load the Extension in VS Code
1. Open VS Code.
2. Select **File -> Open Folder...** and choose the `dexforge-vscode` directory.
3. Press `F5` to open a new **Extension Development Host** window with the extension loaded.

### 3. Usage
- Open any `.java` or `.smali` file. The language client will boot the decompiler server.
- Open the Command Palette (`Ctrl+Shift+P` or `Cmd+Shift+P` on macOS) and run:
  - `DexForge: Start Decompiler Daemon`
  - `DexForge: Open Device Explorer`
