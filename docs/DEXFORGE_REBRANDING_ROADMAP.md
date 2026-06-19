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

Yang sudah ada:

- README sudah memperkenalkan **DexForge Engine**.
- Distribution task menghasilkan artifact seperti:

```text
dexforge-engine-<version>.zip
dexforge-gui-<version>-win.zip
dexforge-gui-<version>-with-jre-win.zip
```

- Binary alias sudah diarahkan:

```text
dexforge
dexforge-gui
```

- Compatibility binary masih tersedia:

```text
jadx
jadx-gui
```

- Fitur baru sudah memakai DexForge naming:

```text
DexForge Device Explorer
DexForge Layout Viewer
DexForge Frida Integration
DexForge CLI
DexForge GUI
com.dexforge.layoutviewer
```

- Repo split sudah dijelaskan di:

```text
docs/DEXFORGE_REPOSITORIES.md
```

- Docker support ditambahkan untuk DexForge CLI.
- GitHub Actions CI/CD untuk Docker, APK analysis, dan VS Code extension.
- Dokumentasi LSP Daemon API dan Frida Integration sudah lengkap.
- Security guidelines dan developer guide sudah ditambahkan.
- VS Code extension marketplace README sudah dibuat.
- Pre-commit hooks, CODEOWNERS, CHANGELOG, dan Renovate/Dependabot sudah dikonfigurasi.

## Target Brand Architecture

## DexForge Engine

Repo:

```text
damarkuncoro/jadx-projects
```

Role:

- Core decompiler engine.
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
include("jadx-cli")
include("jadx-gui")
```

Reason:

- Lower merge risk.
- Lower plugin breakage.
- Lower build script churn.

Medium-term optional:

Add distribution tasks and aliases with DexForge names only.

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
New DexForge-owned features: allow com.dexforge.*
```

Good examples:

```text
com.dexforge.layoutviewer
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

## Phase 3 - IDE Integration Branding

Tasks:

- VS Code extension uses DexForge naming.
- IntelliJ extension uses DexForge naming.
- IDE extensions call `dexforge` binary by default.
- IDE settings allow custom engine binary path.
- JSON CLI contracts remain stable.

Acceptance criteria:

- IDE users do not need to understand JADX internals to use DexForge.
- Advanced users can still point extensions to compatibility binaries.

## Phase 4 - Optional Internal Namespace Review

Only consider after v1-level stability.

Questions:

- Are upstream merges still important?
- Are JADX plugins expected to work unchanged?
- Do external users import `jadx.api.*`?
- Is there a breaking major version planned?

Possible outcomes:

1. Keep internal `jadx.*` forever.
2. Introduce DexForge facade APIs while keeping `jadx.*`.
3. Rename internal packages in a major version.

Recommended default:

```text
Keep internal jadx.* and build DexForge facade APIs only where useful.
```

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

- [ ] README title and intro use DexForge Engine.
- [ ] README explains “powered by JADX”.
- [ ] Device Explorer docs use DexForge naming.
- [ ] Release notes template uses DexForge.
- [ ] Repository split docs remain current.
- [ ] Layout Viewer docs use DexForge naming.

## GUI

- [ ] Main window title says DexForge GUI.
- [ ] About dialog says DexForge GUI, powered by JADX.
- [ ] Desktop template uses DexForge GUI.
- [ ] App icon/logo uses DexForge asset.
- [ ] Menus and panels use DexForge feature names where relevant.

## CLI

- [ ] `dexforge` binary works.
- [ ] `dexforge-gui` binary works.
- [ ] `jadx` compatibility alias works.
- [ ] `jadx-gui` compatibility alias works.
- [ ] Help output prefers DexForge.
- [ ] Compatibility note is visible.

## Release

- [ ] Zip names use DexForge.
- [ ] Windows launcher uses DexForge.
- [ ] Release notes use DexForge.
- [ ] Checksums/artifact names are consistent.
- [ ] Compatibility aliases are included.

## Code

- [ ] New DexForge-owned features may use `com.dexforge.*`.
- [ ] Upstream-derived code remains `jadx.*`.
- [ ] No mass rename without migration plan.
- [ ] Public APIs remain backward compatible.

## Recommended Next Actions

Best immediate sequence:

1. Update user-facing docs terminology.
2. Update GUI title/About/desktop template.
3. Add DexForge logo assets.
4. Standardize CLI help banner.
5. Keep internal modules/packages unchanged.
6. Create release note template for DexForge Engine.

## Decision Summary

Recommended final policy:

```text
DexForge is the product.
JADX is the upstream engine foundation.
dexforge is the primary command.
jadx remains as compatibility alias.
Internal jadx.* packages remain until a major migration is justified.
New DexForge-owned features may use com.dexforge.*.
```
