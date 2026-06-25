# DexForge Core Replacement Plan

## Ringkasan

Dokumen ini menjelaskan rencana kerja jika `dexforge-core` ingin menjadi core engine utama yang menggantikan peran `jadx-core` secara bertahap.

Target ini harus diperlakukan sebagai **major architecture migration**, bukan sekadar rename module.

Strategi yang disarankan:

```text
Short term:
CLI / GUI / IDE
    -> dexforge-core API
        -> engine ports
            -> jadx-core adapter

Long term:
CLI / GUI / IDE
    -> dexforge-core
        -> DexForge-native engine internals
```

Tujuan awal bukan menghapus `jadx-core`, tetapi membuat semua produk DexForge memakai `dexforge-core` sebagai boundary utama. `jadx-core` tetap dapat menjadi backend compatibility sampai kontrak DexForge stabil dan feature-complete.

## Prinsip Migrasi

## 1. Jangan Hapus `jadx-core` Terlalu Cepat

`jadx-core` adalah engine decompiler utama, bukan dependency kecil. Banyak fitur CLI, GUI, plugin, dan upstream code kemungkinan masih bergantung ke tipe internal seperti:

```text
JadxArgs
JadxDecompiler
ClassNode
RootNode
```

Penghapusan langsung akan berisiko:

- Merusak upstream merge.
- Merusak plugin compatibility.
- Merusak workflow CLI/GUI yang sudah stabil.
- Memaksa rewrite besar tanpa acceptance criteria yang jelas.

## 2. Jadikan `dexforge-core` Boundary Publik

Semua product surface DexForge sebaiknya bergantung ke API DexForge:

```text
dexforge-cli
jadx-gui / DexForge GUI
VS Code extension
IntelliJ extension
automation / JSON consumers
```

Tidak boleh langsung bergantung ke `JadxDecompiler` untuk workflow baru.

## 3. Batasi JADX ke Adapter Layer

Import `jadx.api.*` dan `jadx.core.*` sebaiknya hanya berada di area yang memang menjadi adapter atau compatibility layer.

Contoh area yang diperbolehkan:

```text
dexforge.core.infrastructure.jadx
dexforge.infrastructure.adapter.jadx
jadx compatibility modules
upstream-derived code
```

Contoh area yang sebaiknya tidak expose JADX:

```text
dexforge.core.application
dexforge.application.usecase
dexforge.domain
dexforge.engine.api
IDE JSON contracts
```

## Target Architecture

## Current Transitional Shape

```text
dexforge-cli
    -> dexforge-core
        -> DecompileApplicationService
        -> DecompilerEngine port
        -> JadxDecompilerEngine
            -> jadx-core
```

## Target Medium-Term Shape

```text
dexforge-cli
dexforge-gui
dexforge-vscode
dexforge-intellij
    -> dexforge-core public API
        -> application use cases
        -> domain model
        -> engine ports
        -> jadx-core adapter
```

## Target Long-Term Shape

```text
dexforge-cli
dexforge-gui
dexforge-vscode
dexforge-intellij
    -> dexforge-core
        -> DexForge-native engine internals
        -> optional jadx compatibility backend
```

Possible final modules:

```text
dexforge-core              - public engine API, domain, application services
dexforge-engine-jadx       - adapter/backend to upstream JADX
dexforge-cli               - CLI thin client
dexforge-gui               - GUI thin client
dexforge-plugin-api        - DexForge plugin API
jadx-compat                - optional JADX compatibility facade
```

## Phase 0 - Stabilize Current Module

Status: **Implemented initial stabilization**

Goal:

Make `dexforge-core` compile, test, and behave as a reliable module before expanding its ownership.

Tasks:

- Fix stale tests in `dexforge-core`.
- Ensure `:dexforge-core:test` passes.
- Ensure `dexforge-core` source and tests agree on use case contracts.
- Use typed exceptions for the current `OpenProjectUseCase` contract.
- Remove or update obsolete DTOs if they are no longer used.
- Keep `dexforge-core` build output out of source control.

Acceptance criteria:

- `./gradlew :dexforge-core:test` passes.
- `dexforge-core` has no stale tests expecting old signatures.
- Existing CLI decompile flow still works.

## Phase 1 - Define DexForge Engine Contracts

Status: **Started**

Goal:

Create stable DexForge-facing contracts that CLI, GUI, IDE extensions, and automation can use without knowing JADX internals.

Candidate API concepts:

```text
DexForgeEngine
DexForgeSession
DexForgeDecompileRequest
DexForgeDecompileResult
DexForgeProgressReporter
DexForgeProject
DexForgeError
DexForgeDiagnostic
```

Rules:

- Public DexForge contracts must not expose `JadxArgs`, `JadxDecompiler`, `ClassNode`, or `RootNode`.
- Error and result models should be stable enough for JSON output.
- API changes should be tracked in changelog or migration notes.

Acceptance criteria:

- CLI can create a DexForge request without exposing `JadxDecompiler`.
- IDE integrations can consume documented JSON contracts.
- Public model names consistently use DexForge terminology.

## Phase 2 - Complete the JADX Adapter Backend

Goal:

Make `jadx-core` an implementation detail behind DexForge ports.

Current foundation:

```text
dexforge.core.ports.decompile.DecompilerEngine
dexforge.core.ports.decompile.DecompilerSession
dexforge.core.infrastructure.jadx.JadxDecompilerEngine
dexforge.core.infrastructure.jadx.JadxDecompilerSession
dexforge.core.application.decompile.DecompileApplicationService
```

Expand adapter coverage for:

- Load APK, DEX, JAR, AAB, and source inputs.
- List classes.
- Resolve class metadata.
- Get decompiled source.
- Decode resources.
- Search code.
- Export decompiled project.
- Decompile single class.
- Manage project/session lifecycle and state persistence.
- Report diagnostics and decompile errors.
- Expose progress lifecycle.
- Support plugin loading.
- Expose source map or code offset metadata where needed.

Acceptance criteria:

- DexForge application services do not instantiate `JadxDecompiler` directly.
- All direct JADX calls for decompile workflow live in adapter packages.
- CLI behavior remains backward compatible.

## Phase 3 - Move Workflow Logic from CLI and GUI into DexForge Core

Status: **Started for daemon load/list/decompile and LSP operations**

Goal:

Make CLI and GUI thin clients over `dexforge-core`.

Move these workflows into `dexforge-core`:

- Open project.
- Close project.
- Decompile project.
- Pull APK and decompile.
- Search code.
- Export reports.
- Generate JSON output.
- Single-class decompile.
- Track progress and diagnostics.
- Manage project/session lifecycle.

Target client shape:

```text
CLI:
parse args -> build request -> call dexforge-core -> render output

GUI:
user action -> call use case -> update view model
```

Acceptance criteria:

- CLI contains argument parsing and output rendering, not decompile business logic.
- GUI actions call DexForge use cases for new DexForge-owned workflows.
- Core workflows have unit or integration coverage.

## Phase 4 - Dependency Audit and Enforcement

Status: **Started**

Goal:

Know exactly where `jadx-core` is still used and prevent new leakage into DexForge-owned layers.

Audit command:

```bash
rg "jadx\\.api|jadx\\.core|JadxDecompiler|JadxArgs|ClassNode|RootNode"
```

Categorize every usage:

- Allowed adapter usage.
- Allowed upstream-derived usage.
- Temporary compatibility usage.
- Should migrate to DexForge API.
- Blocked until deeper engine work exists.

Possible enforcement:

- Checkstyle custom rule.
- ArchUnit-style dependency tests.
- Gradle source-set boundaries.
- Review checklist for new DexForge features.

Acceptance criteria:

- A dependency map exists in `docs/DEXFORGE_JADX_DEPENDENCY_ZONES.md`.
- Allowed JADX dependency zones are documented.
- New DexForge application/domain code cannot casually import JADX internals.

## Phase 5 - Public DexForge API and JSON Contract Freeze

Goal:

Make DexForge contracts stable enough for IDE extensions and external automation.

Tasks:

- Document JSON CLI output.
- Document JSON-RPC/LSP daemon API.
- Version schemas explicitly.
- Add compatibility tests for JSON output.
- Add sample consumers for VS Code and IntelliJ.
- Add migration notes from `jadx` command usage to `dexforge`.

Good JSON shape:

```json
{
  "tool": "dexforge",
  "schemaVersion": 1,
  "status": "success"
}
```

Acceptance criteria:

- IDE extensions call `dexforge` by default.
- IDE extensions do not parse human terminal output.
- Schema-breaking changes require a version bump.

## Phase 6 - DexForge-Native Internals

Goal:

Begin replacing selected `jadx-core` responsibilities with DexForge-owned internals.

Start with isolated areas:

1. Project/session lifecycle and persistence.
2. Progress and diagnostic reporting.
3. Source/resource indexing.
4. Search model.
5. Report generation.
6. JSON/LSP contract generation.
7. Plugin facade.

Avoid starting with:

- Core decompiler algorithms.
- Deep bytecode IR.
- Type inference.
- Plugin internals tightly coupled to JADX.

Reason:

Those areas are expensive, high-risk, and hard to validate without extensive compatibility tests.

Acceptance criteria:

- DexForge-owned workflows can run without importing `jadx-core` outside adapter boundaries.
- `jadx-core` becomes one backend instead of the architectural center.
- Feature parity is measured before replacing each subsystem.

## Phase 7 - Compatibility Layer

Goal:

Preserve existing workflows while DexForge becomes the primary engine API.

Compatibility targets:

```text
jadx command
jadx-gui command
jadx.api facade where feasible
existing plugin API where feasible
old config/cache environment variables
```

Rules:

- Keep `jadx` and `jadx-gui` as aliases until a major version boundary.
- Keep `JADX_CONFIG_DIR`, `JADX_CACHE_DIR`, and `JADX_TMP_DIR` until a migration plan exists.
- Do not promise full compatibility for internal `jadx.core.*` APIs.

Acceptance criteria:

- Existing users can still run old commands.
- Compatibility behavior is documented.
- Breaking changes are limited to a clearly announced major release.

## Phase 8 - Major Version Cutover

Goal:

Switch public ownership from JADX compatibility to DexForge-native core.

This phase should only happen after:

- `dexforge-core` API is stable.
- CLI and GUI use DexForge APIs.
- IDE extensions use DexForge JSON contracts.
- Adapter boundaries are enforced.
- Public packaging is stable.
- Compatibility story is documented.
- Test coverage covers core workflows.

Possible actions:

- Rename or split `jadx-core` into `dexforge-engine-jadx`.
- Introduce `dexforge-plugin-api`.
- Keep `jadx-compat` as an optional compatibility module.
- Mark old `jadx.api.*` compatibility APIs as deprecated where appropriate.

Acceptance criteria:

- DexForge can ship as the primary product without exposing JADX as the main API.
- Existing compatibility aliases still work or have documented replacements.
- Upstream attribution remains visible.

## Risk Register

## Risk: Breaking Upstream Merge

Cause:

- Renaming or rewriting upstream-derived internals too early.

Mitigation:

- Keep upstream code isolated.
- Prefer adapter/facade layers before internal rewrite.
- Avoid mass package rename.

## Risk: Breaking Plugins

Cause:

- Removing or changing `jadx.api.*` and plugin-facing types.

Mitigation:

- Preserve plugin API until a major migration.
- Introduce DexForge plugin facade first.
- Provide compatibility shims where feasible.

## Risk: Half-Migrated Architecture

Cause:

- Some workflows call DexForge APIs while others still directly call JADX internals.

Mitigation:

- Create dependency map.
- Enforce allowed import zones.
- Move workflow by workflow, with tests.

## Risk: Public API Too Early

Cause:

- Freezing DexForge contracts before understanding all CLI, GUI, and IDE needs.

Mitigation:

- Keep early APIs internal or experimental.
- Version JSON schemas.
- Document preview status until stable.

## Risk: Rewriting the Decompiler Core Prematurely

Cause:

- Treating replacement as a branding exercise instead of a compiler/decompiler engineering project.

Mitigation:

- Replace orchestration, contracts, indexing, diagnostics, and reporting first.
- Replace deep decompiler internals only after feature parity tests exist.

## Recommended First Milestone

Best first milestone:

```text
All DexForge product surfaces use dexforge-core as the primary API,
while jadx-core remains a backend adapter.
```

This gives immediate value:

- DexForge owns the public architecture.
- CLI, GUI, and IDE integrations use consistent contracts.
- JADX remains available for compatibility.
- Upstream merge risk stays manageable.

## Immediate Next Actions

1. Expand build enforcement beyond `dexforge.domain` and `dexforge.engine` once adapter boundaries are complete.
2. Start moving GUI workflows behind DexForge project/session APIs.
3. Promote `docs/DEXFORGE_DAEMON_LSP_CONTRACT.md` from preview to stable once downstream IDE clients validate it.
4. Add end-to-end IDE client smoke tests around the documented daemon/LSP contract.

## Decision Summary

Recommended policy:

```text
dexforge-core should become the primary public engine boundary.
jadx-core should remain as the first backend adapter.
CLI, GUI, and IDE integrations should call DexForge contracts.
JADX internals should be isolated to adapter and compatibility zones.
Deep decompiler replacement should happen only after contracts, tests,
and workflow migration are stable.
```
