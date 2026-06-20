# DexForge JADX Dependency Zones

## Purpose

This document defines where direct dependencies on `jadx.api.*`, `jadx.core.*`, and concrete JADX types are allowed while `dexforge-core` grows into the primary DexForge engine boundary.

The goal is not to remove JADX immediately. The goal is to prevent new DexForge-owned layers from leaking JADX internals into public APIs, domain models, or IDE-facing contracts.

## Allowed Zones

## `dexforge.core.infrastructure.jadx`

Status: **Allowed**

This package is the main JADX backend adapter for `dexforge-core`.

Allowed dependencies:

```text
jadx.api.JadxArgs
jadx.api.JadxDecompiler
jadx.api.JavaClass
jadx.api.JavaMethod
jadx.core.dex.nodes.*
jadx.core.utils.*
```

Examples:

```text
JadxBackedDexForgeEngine
JadxDecompilerEngine
JadxDecompilerSession
JadxSingleClassDecompileAction
JadxMethodSignatureMapper
```

Rule:

JADX-specific conversions should live here instead of in `dexforge.domain` or public `dexforge.engine` APIs.

## `dexforge.infrastructure.adapter.jadx`

Status: **Allowed**

This package may adapt repository or application ports to current JADX-backed storage/runtime behavior.

Rule:

Keep adapter types package-private where possible. Public method signatures should prefer DexForge domain/application types.

## Compatibility and Legacy CLI Zones

Status: **Temporarily allowed**

Some CLI paths still depend directly on JADX because they parse existing command-line options or implement compatibility utilities.

Current examples:

```text
dexforge-cli/src/main/java/dexforge/cli/DexforgeCLIArgs.java
dexforge-cli/src/main/java/dexforge/cli/DexforgeAppCommon.java
dexforge-cli/src/main/java/dexforge/cli/JCommanderWrapper.java
dexforge-cli/src/main/java/dexforge/cli/daemon
dexforge-cli/src/main/java/dexforge/cli/tools
dexforge-cli/src/main/java/dexforge/cli/clst
```

Rule:

These usages should shrink over time. New CLI workflows should prefer `dexforge.engine.*` and `dexforge-core` use cases.

## Disallowed Zones

## `dexforge.domain`

Status: **Disallowed**

Domain models must not import `jadx.api.*` or `jadx.core.*`.

Reason:

Domain models are DexForge-owned concepts and should remain stable if the backend changes.

## `dexforge.application`

Status: **Disallowed by default**

Application use cases should depend on DexForge ports and domain models.

Exception:

Temporary migration code may be allowed only when there is no adapter boundary yet, and it should be tracked for removal.

## `dexforge.engine`

Status: **Disallowed**

Public engine APIs must not expose `JadxArgs`, `JadxDecompiler`, `ClassNode`, `RootNode`, or other JADX types.

Reason:

This package is the public DexForge boundary for CLI, GUI, IDE integrations, and automation.

## Audit Command

Use this command to inspect direct JADX dependencies:

```bash
rg "jadx\\.api|jadx\\.core|JadxDecompiler|JadxArgs|ClassNode|RootNode" dexforge-core/src/main/java dexforge-cli/src/main/java
```

## Current Audit Notes

The first cleanup removed direct `jadx.api.JavaClass` and `jadx.api.JavaMethod` imports from:

```text
dexforge.domain.model.source.MethodSignature
```

The JADX-specific factory was moved to:

```text
dexforge.core.infrastructure.jadx.JadxMethodSignatureMapper
```

Remaining direct JADX dependencies in `dexforge-core` should be limited to adapter/infrastructure packages.

## Next Enforcement Options

Possible enforcement mechanisms:

- Checkstyle custom import rule.
- ArchUnit-style dependency tests.
- Gradle source-set boundaries.
- Review checklist for new DexForge-owned features.

Recommended first enforcement:

```text
Fail the build if dexforge.domain or dexforge.engine imports jadx.api.* or jadx.core.*.
```
