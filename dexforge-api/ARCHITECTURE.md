# DexForge API Architecture

This module is the DexForge-owned public API boundary. It replaces direct consumer usage of `jadx.api` while keeping the current runtime implementation as an adapter over JADX.

## Layers

- Public API: `dexforge.api.*` contracts consumed by CLI, GUI, integrations, and future DexForge plugins.
- Adapter bridge: package-private wrappers and deprecated bridge methods that translate to the current JADX backend.
- Backend runtime: existing `jadx-core` implementation, unchanged to preserve upstream mergeability and plugin compatibility.

## Principles

- Clean Architecture: dependencies point inward toward DexForge contracts from consumers; only this adapter module knows about JADX backend types.
- DDD: public names describe DexForge domain concepts such as decompiler, class, method, package, resource, diagnostic, settings, and plugin registry.
- SOLID/SRP: API objects expose one domain concept each; orchestration stays in `DexForgeDecompiler`; wrapping logic stays centralized.
- DRY: `DexForgeNodeFactory` owns node wrapping so class, method, field, and package APIs do not duplicate conversion rules.
- Scalability: compatibility bridges are deprecated and isolated, allowing consumer migration without changing internal JADX package layout.

## Enforced Rules

`DexForgeApiArchitectureTest` verifies that:

- Public API methods and constructors do not expose `jadx.*` types unless they are deprecated bridges.
- Concrete public API objects remain final by default.
- Adapter factory internals remain package-private.

Run:

```bash
./gradlew :dexforge-api:test
```
