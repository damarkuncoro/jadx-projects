# DexForge Rebranding Roadmap

## Ringkasan

DexForge adalah brand utama untuk fork ini, sementara JADX tetap menjadi basis upstream engine dan compatibility layer.

Strategi rebranding yang disarankan:

```text
Public brand: DexForge
Technical lineage: powered by JADX
Compatibility: keep jadx module/package names until safe to migrate
```

Tujuan utamanya adalah membuat pengguna melihat produk ini sebagai **DexForge**, tanpa merusak kompatibilitas dengan ekosistem JADX, Gradle project, plugin API, upstream merge, dan script yang sudah ada.

## Positioning Produk

Kalimat positioning utama:

```text
DexForge Engine, powered by JADX
```

Alternatif untuk README, website, dan release notes:

```text
DexForge is an Android reverse engineering workbench built on JADX.
```

```text
DexForge extends JADX with Android device workflows, Frida integration, UI layout preview, and IDE-friendly automation.
```

Gunakan istilah:

- **DexForge Engine** untuk repo ini.
- **DexForge GUI** untuk aplikasi desktop Swing.
- **DexForge CLI** untuk binary command line.
- **DexForge Device Explorer** untuk fitur ADB package pull/decompile.
- **DexForge Layout Viewer** untuk Android XML layout preview.
- **JADX** sebagai upstream decompiler engine/foundation.

## Prinsip Rebranding

## 1. Brand Publik Harus DexForge

Semua permukaan yang dilihat user baru sebaiknya memakai DexForge:

- README title.
- Window title.
- About dialog.
- Launcher name.
- Desktop file.
- Distribution zip.
- Release notes.
- Screenshots.
- Docs.
- CLI alias utama.
- IDE extension name.

Contoh:

```text
DexForge GUI
DexForge Engine
DexForge Device Explorer
DexForge Layout Viewer
```

## 2. Jangan Rename Internal JADX Terlalu Cepat

Hal-hal berikut sebaiknya tetap `jadx` untuk sementara:

- Gradle module name seperti `jadx-core`, `jadx-cli`, `jadx-gui`.
- Java package internal `jadx.*`.
- Upstream plugin packages.
- Jadx API names yang dipakai plugin/external tools.
- Cache/config compatibility yang masih bergantung pada JADX behavior.

Alasan:

- Mengurangi konflik saat merge upstream.
- Menghindari breakage plugin.
- Menghindari rename masif yang tidak memberi nilai user langsung.
- Menjaga tool/script existing tetap berjalan.

## 3. DexForge Sebagai Distribution Layer

Untuk jangka pendek dan menengah, DexForge sebaiknya menjadi:

```text
Brand layer + distribution layer + enhanced feature layer
```

Bukan rename total seluruh codebase.

Dengan strategi ini:

- Binary utama bisa `dexforge`.
- GUI launcher bisa `dexforge-gui`.
- Zip release bisa `dexforge-engine-<version>.zip`.
- Internal engine tetap memakai struktur JADX.

## Current State

Yang sudah ada di repo ini:

- [x] README title uses DexForge Engine.
- [x] CLI help uses DexForge branding ("DexForge CLI, powered by JADX").
- [x] Device Explorer CLI uses DexForge naming (`dexforge device-explorer`).
- [x] All use cases have unit tests in dexforge-core.
- [x] Distribution bundle `dexforge-engine-{version}.zip` generated.
- [x] Compatibility aliases present (`jadx`, `jadx-gui`).
- [x] Window title uses DexForge GUI.
- [x] About dialog shows "DexForge GUI, powered by JADX".
- [x] Desktop entry uses DexForge GUI branding.

## Target Brand Architecture

## DexForge Engine

Repo:

```text
damarkuncoro/jadx-projects
```

Role:

- Core decompiler engine.
- DexForge application/domain facade through `dexforge-core`.
- CLI.
- GUI desktop.
- Device Explorer.
- Frida integration.
- Layout Viewer.
- Release artifacts.
- JSON contracts for IDE integrations.

Suggested public name:

```text
DexForge Engine
```

Internal compatibility name:

```text
jadx
```

## DexForge GUI

Role:

- Desktop reverse engineering UI.
- Main entry for APK/class/resource inspection.
- Owns Device Explorer UI, Frida panel, Layout Viewer panel.

Suggested public name:

```text
DexForge GUI
```

Compatibility:

```text
jadx-gui
```

## DexForge CLI

Role:

- Command line decompile.
- Device Explorer commands.
- LSP/daemon mode.
- Automation-friendly JSON output.

Suggested public binary:

```bash
dexforge
```

Compatibility binary:

```bash
jadx
```

## DexForge IDE Extensions

Repos:

```text
damarkuncoro/dexforge-vscode
damarkuncoro/dexforge-intellij
```

Role:

- IDE integrations.
- Call `dexforge` binary.
- Consume stable JSON output.

Rule:

IDE integrations must call machine-readable contracts, not parse human terminal output.

## Naming Rules

## User-Facing Names

Use DexForge:

```text
DexForge
DexForge Engine
DexForge GUI
DexForge CLI
DexForge Device Explorer
DexForge Layout Viewer
DexForge Frida Integration
```

Avoid new user-facing names that start with `JADX`, except when explaining upstream compatibility.

## Technical Compatibility Names

Keep JADX where changing it is risky:

```text
jadx-core
jadx-cli
jadx-gui
jadx.api.*
jadx.core.*
jadx.gui.*
```

Do not rename these without a dedicated migration milestone.

## Feature Package Names

New DexForge-owned features may use:

```text
com.dexforge.*
```

Example:

```text
com.dexforge.layoutviewer
```

Existing JADX-owned code should stay:

```text
jadx.*
```

## CLI Naming

Primary:

```bash
dexforge
dexforge-gui
```

Compatibility:

```bash
jadx
jadx-gui
```

CLI help should prefer DexForge wording but mention JADX compatibility.

Example:

```text
DexForge CLI, powered by JADX
```

## Artifact Naming

Preferred:

```text
dexforge-engine-<version>.zip
dexforge-gui-<version>-win.zip
dexforge-gui-<version>-with-jre-win.zip
```

Avoid publishing new public artifacts under `jadx-*`, except if needed for compatibility.

## Config and Cache Naming

Current environment variables:

```text
JADX_CONFIG_DIR
JADX_CACHE_DIR
JADX_TMP_DIR
```

Short-term:

- Keep them for compatibility.
- Document them as inherited JADX environment variables.

Future optional aliases:

```text
DEXFORGE_CONFIG_DIR
DEXFORGE_CACHE_DIR
DEXFORGE_TMP_DIR
```

Rule:

Do not remove `JADX_*` variables until a major version boundary and migration plan exists.

## Documentation Plan

## README

README should start with DexForge:

```md
# DexForge Engine

DexForge Engine is an Android reverse engineering workbench powered by JADX.
```

Then explain:

```md
This project is based on upstream skylot/jadx and keeps JADX compatibility aliases.
```

README should clearly list:

- `dexforge`
- `dexforge-gui`
- `jadx` compatibility alias
- `jadx-gui` compatibility alias

## Docs

Docs that should be updated:

```text
docs/DEVICE_EXPLORER_ROADMAP.md
docs/PROJECT_SUGGESTIONS_ID.md
docs/DEXFORGE_REPOSITORIES.md
docs/ANDROID_XML_LAYOUT_VIEWER_ROADMAP.md
```

Preferred naming:

- Replace “JADX Device Explorer” with “DexForge Device Explorer”.
- Keep “JADX” only when referring to upstream engine or compatibility.

## Release Notes

Release notes should use this structure:

```md
# DexForge Engine <version>

Powered by JADX.

## Highlights
...

## Compatibility
- Includes `jadx` and `jadx-gui` aliases for existing workflows.
```

## UI Rebranding Plan

## Window Title

Preferred:

```text
DexForge GUI
```

Optional with project:

```text
DexForge GUI - <project>
```

## About Dialog

Should show:

```text
DexForge GUI
Powered by JADX
Based on skylot/jadx
```

Include links:

```text
DexForge repo: https://github.com/damarkuncoro/jadx-projects
Upstream JADX: https://github.com/skylot/jadx
```

## Desktop File

Preferred:

```text
Name=DexForge GUI
Comment=Android reverse engineering workbench powered by JADX
Exec=dexforge-gui
```

## Icons and Logos

Current README still references upstream JADX logo.

Rebranding target:

- Add DexForge logo assets.
- Keep upstream attribution in docs/About.
- Do not reuse upstream logo as primary DexForge identity.

Suggested asset paths:

```text
jadx-gui/src/main/resources/logos/dexforge-logo.png
jadx-gui/src/main/resources/logos/dexforge-logo.ico
jadx-gui/src/main/resources/logos/dexforge-logo.svg
```

## CLI Rebranding Plan

## Help Banner

Preferred:

```text
DexForge CLI, powered by JADX
```

Compatibility note:

```text
The `jadx` command is kept as a compatibility alias.
```

## Commands

Device Explorer command should use:

```bash
dexforge device-explorer ...
```

Docs may mention:

```bash
jadx device-explorer ...
```

only as compatibility.

## JSON Contracts

JSON output should not include unstable branding unless necessary.

Good:

```json
{
  "tool": "dexforge",
  "schemaVersion": 1
}
```

If existing integrations already expect `jadx`, keep backward compatibility.

## Gradle and Module Naming

Short-term:

Keep:

```text
rootProject.name = "jadx"
include("jadx-core")
include("dexforge-core")
include("dexforge-cli")
include("jadx-gui")
```

Reason:

- Lower merge risk.
- Lower plugin breakage.
- Lower build script churn.
- Allows DexForge-owned use cases to grow without mass-renaming upstream code.

Medium-term optional:

Add DexForge facade APIs and distribution tasks with DexForge names only.

Long-term optional:

Only consider Gradle module rename after:

- IDE integrations are stable.
- Release process is stable.
- Upstream merge strategy is clear.
- Plugin API compatibility plan exists.

## Package Naming

Recommended rule:

```text
Existing upstream-derived code: keep jadx.*
New DexForge-owned features and orchestration: allow dexforge.* or com.dexforge.*
```

Good examples:

```text
com.dexforge.layoutviewer
dexforge.core.application.decompile
dexforge.core.infrastructure.jadx
jadx.gui.device
jadx.core
```

Avoid:

```text
Mass rename jadx.* -> com.dexforge.*
```

unless this becomes a hard fork with no upstream merge plan.

## Migration Phases

## Phase 0 - Current State

Status:

- DexForge README branding exists.
- DexForge artifacts exist.
- DexForge binary aliases exist.
- JADX compatibility remains.

Goal:

Document and stabilize current branding strategy.

## Phase 1 - User-Facing Consistency

Tasks:

- Rename UI title to DexForge GUI where safe.
- Update About dialog copy.
- Update desktop template.
- Add DexForge logo assets.
- Update docs wording from “JADX Device Explorer” to “DexForge Device Explorer”.
- Ensure README consistently says DexForge Engine.

Acceptance criteria:

- New user sees DexForge as the product name.
- Upstream JADX attribution remains visible.
- Existing `jadx` commands still work.

## Phase 2 - CLI and Release Polish

Tasks:

- Ensure `dexforge` is documented as primary CLI.
- Ensure `dexforge-gui` is documented as primary GUI launcher.
- Keep `jadx` and `jadx-gui` in release zip as compatibility aliases.
- Standardize release artifact names.
- Update release scripts and notes templates.

Acceptance criteria:

- Release assets all use DexForge names.
- Compatibility aliases are still present.
- Docs do not confuse primary command and legacy alias.

## Phase 3 - Android XML Layout Viewer (Completed)

Tasks:

- Implement embedded Swing-based Android XML layout parser.
- Add layout preview and hierarchy inspector.
- Integrate Layout Viewer panel into DexForge GUI.
- Support resource reference resolving.

Acceptance criteria:

- Users can visually preview Android layouts.
- Resource references are resolved correctly.
- DexForge Layout Viewer is accessible from File menu.

## Phase 4 - Frida Script Generator (Completed)

Tasks:

- Implement right-click context hooks builder.
- Create Frida script generation from decompiled methods.
- Add predefined snippet providers (SSL pinning bypass, root detection bypass, etc.).
- Integrate Frida panel into DexForge GUI.

Acceptance criteria:

- Users can generate Frida hooks directly from GUI.
- Predefined snippets are available and functional.
- Frida panel shows generated and custom scripts.

## Phase 5 - IDE Integration Branding (Current)

Status: **In Progress**

Tasks:

- Finalize VS Code extension DexForge branding in marketplace.
- Finalize IntelliJ extension DexForge branding.
- Document IDE extensions calling `dexforge` binary by default.
- Publish stable JSON-RPC/LSP daemon API contracts.
- Ensure IDE users do not need to understand JADX internals.

Acceptance criteria:

- VS Code extension is published and branded as DexForge.
- IntelliJ extension is published and branded as DexForge.
- IDE users see DexForge as the primary product.
- JSON CLI contracts are stable and backward-compatible.

## Phase 6 - Public Release (Planned)

Status: **Planned**

Tasks:

- Stabilize all public APIs, schema structures, and JSON contracts.
- Finalize public binary packaging (Windows installer, Linux deb/rpm, macOS app bundle).
- Create comprehensive user documentation and tutorials.
- Prepare marketing materials and release announcement.
- Establish community feedback and support channels.

Acceptance criteria:

- Binary packages available on all major platforms.
- Documentation covers setup, usage, and troubleshooting.
- Community feedback loop is operational.
- Version 1.0 ready for general availability.

## Risk Register

## Risk: Breaking Upstream Merge

Cause:

- Renaming modules/packages too early.

Mitigation:

- Keep internal Gradle modules and Java packages unchanged.
- Use DexForge only in public distribution and new feature namespaces.

## Risk: Breaking Plugins

Cause:

- Changing `jadx.api.*` or plugin paths.

Mitigation:

- Preserve `jadx.api.*`.
- Add DexForge wrappers instead of replacing APIs.

## Risk: User Confusion

Cause:

- README says DexForge, CLI says JADX, GUI says jadx-gui.

Mitigation:

- Use “DexForge, powered by JADX”.
- Clearly mark `jadx` as compatibility alias.

## Risk: Trademark/Attribution Issues

Cause:

- Hiding upstream relationship.

Mitigation:

- Keep upstream attribution in README/About/license notices.
- Do not imply DexForge created upstream JADX.

## Risk: Overbranding Internal Code

Cause:

- Renaming everything for aesthetic consistency.

Mitigation:

- Separate product branding from code ownership.
- Rename only when it improves user or developer experience.

## Checklist

## Docs

- [x] README title and intro use DexForge Engine.
- [x] README explains “powered by JADX”.
- [x] Device Explorer docs use DexForge naming.
- [x] Release notes template uses DexForge.
- [x] Repository split docs remain current.
- [x] Layout Viewer docs use DexForge naming.

## GUI

- [x] Main window title says DexForge GUI.
- [x] About dialog says DexForge GUI, powered by JADX.
- [x] Desktop template uses DexForge GUI.
- [x] App icon/logo uses DexForge asset.
- [x] Menus and panels use DexForge feature names where relevant.

## CLI

- [x] `dexforge` binary works (primary command).
- [x] `dexforge-gui` binary works (primary GUI launcher).
- [x] `jadx` compatibility alias works (legacy support).
- [x] `jadx-gui` compatibility alias works (legacy support).
- [x] Help output prefers DexForge.
- [x] Compatibility note is visible.

## Release

- [x] Zip names use DexForge.
- [x] Windows launcher uses DexForge.
- [x] Release notes use DexForge.
- [x] Checksums/artifact names are consistent.
- [x] Compatibility aliases are included.

## Code

- [x] New DexForge-owned features may use `com.dexforge.*`.
- [x] New DexForge use cases may grow in `dexforge-core`.
- [x] Upstream-derived code remains `jadx.*`.
- [x] No mass rename without migration plan.
- [x] Public APIs remain backward compatible.

## Phase 5 - IDE Extensions (In Progress)

Status: **In Progress** - VS Code extension in separate repository.

Tasks completed:
- [x] JSON LSP daemon API documented (see LSP_DAEMON_API.md)
- [x] `dexforge` binary is primary CLI command
- [x] `dexforge-gui` binary is primary GUI launcher
- [x] Compatibility aliases maintained (`jadx`, `jadx-gui`)
- [x] Quick-start guide for IDE integration created (see IDE_INTEGRATION.md)

Tasks remaining:
- [ ] VS Code extension uses DexForge branding in marketplace.
- [ ] IntelliJ extension uses DexForge branding.
- [ ] Binary availability in PATH post-install scripts.

## Phase 6 - Public Release (In Progress)

Status: **In Progress**

Tasks in progress:
- [x] Distribution bundle generation (`dexforge-engine-{version}.zip`)
- [x] Release notes template created
- [x] PATH installation scripts (Linux/macOS + Windows)
- [x] Migration guide for JADX users
- [x] Linux packaging workflow (.deb)
- [ ] Windows installer (.msi)
- [ ] macOS app bundle (.dmg)
- [ ] Release announcement and marketing materials

## Recommended Next Actions

**Current Status**: Phase 1-4 complete, Phase 5 in progress.

Best sequence for Phase 5-6:

1. **VS Code Extension**: Finalize DexForge branding in marketplace.
2. **IntelliJ Extension**: Add DexForge naming and `dexforge` binary support.
3. **JSON-RPC Contracts**: Publish stable OpenAPI/Swagger documentation for LSP daemon.
4. **Binary Availability**: Ensure `dexforge` and `dexforge-gui` are in system PATH post-install.
5. **Release Process**: Automate Phase 6 release artifacts (Windows installer, Linux packages, macOS app bundle).
6. **Documentation**: Create quick-start guide for IDE extensions and JSON-RPC API consumers.
7. **Community**: Announce Phase 5 completion and call for feedback on IDE integration.

## Decision Summary

Recommended final policy:

```text
DexForge is the product.
JADX is the upstream engine foundation.
dexforge is the primary command.
jadx remains as compatibility alias.
Internal jadx.* packages remain until a major migration is justified.
New DexForge-owned features may use dexforge.* or com.dexforge.*.
DexForge use cases should be introduced in dexforge-core before being wired into CLI, GUI, or IDE integrations.
```
