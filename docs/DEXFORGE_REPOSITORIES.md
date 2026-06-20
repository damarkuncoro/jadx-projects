# DexForge Repository Split

DexForge is split into a stable engine repository and separate IDE/UI repositories.

## Repositories

- `damarkuncoro/jadx-projects`
  - Role: DexForge Engine
  - Based on upstream JADX
  - Owns CLI, GUI, decompilation, ADB Device Explorer, reports, and release zip artifacts
  - Contains `dexforge-core`, the DexForge application/domain layer that wraps `jadx-core` through ports and adapters

- `damarkuncoro/dexforge-vscode`
  - Role: VS Code extension
  - Calls the `dexforge` binary from DexForge Engine
  - Reads `device-explorer --format json` output

- `damarkuncoro/dexforge-intellij`
  - Role: future IntelliJ plugin
  - Should use the same CLI contract as the VS Code extension

- `damarkuncoro/dexforge-docs`
  - Role: public docs, guides, examples, and screenshots

## Engine Contract

IDE integrations should not parse human-readable terminal text. They should call:

```bash
dexforge device-explorer list-devices --format json
dexforge device-explorer list-packages SERIAL 0 user --format json
dexforge device-explorer pull-and-decompile SERIAL id.net.cakramedia.attendance ./workspace 0 --format json
```

The engine must keep JSON output backward compatible once external IDE repositories depend on it.

## Current Layout

The existing `jadx-projects` fork should stay in place as the engine. Do not move it into a monorepo until the engine release process, CLI contract, and VS Code extension are stable.

Inside the engine repo, `jadx-core` remains the upstream-aligned decompiler foundation. New DexForge use cases should be introduced in `dexforge-core` first, then consumed by `dexforge-cli`, `jadx-gui`, and IDE integrations. This keeps product-level behavior reusable while reducing direct dependencies on `JadxDecompiler` outside adapter code.
