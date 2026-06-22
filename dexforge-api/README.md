# DexForge API

`dexforge-api` is the public DexForge-facing API layer that replaces direct use of `jadx.api` for new consumers.

The first implementation is an adapter over the existing JADX runtime. This keeps internal Gradle modules and Java packages stable for upstream merges while allowing CLI, GUI, integrations, and future plugins to migrate toward DexForge-owned types.

## Migration Rules

- New public code should import `dexforge.api.*`.
- Do not expose `jadx.api.*` from new DexForge public contracts.
- Deprecated `unwrap()` methods and JADX bridge constructors exist only for gradual migration.
- Existing JADX plugins can keep using the compatibility plugin contracts.
- DexForge plugins and integrations should prefer DexForge API types.

## Architecture Rules

- Public API types are DexForge-owned contracts; adapter details remain package-private.
- Concrete API value/wrapper objects are final unless they are explicit extension points.
- Public methods must not expose `jadx.*` types unless they are deprecated compatibility bridges.
- Settings and DTOs stay small and focused; orchestration belongs in `DexForgeDecompiler`.
- Wrapping/mapping logic is centralized to avoid duplication as the API surface grows.

## Current Replacement Surface

- `DexForgeDecompiler`
- `DexForgeSettings`
- `DexForgeClass`
- `DexForgeMethod`
- `DexForgeField`
- `DexForgePackage`
- `DexForgeNode`
- `DexForgeCodeInfo`
- `DexForgeResourceFile`
- `DexForgeDiagnostic`
- `DexForgePlugin`
- `DexForgePluginLoader`
- `DexForgePluginRegistry`
- DexForge-owned enums for comments, decompilation mode, and resources
