# DexForge Engine Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [dev] - 2026-06

### Added
- DexForge rebranding: Primary CLI/GUI binaries (`dexforge`, `dexforge-gui`)
- Compatibility aliases: `jadx` and `jadx-gui` for backward compatibility
- Clean Architecture layer in `dexforge-core/`:
  - Domain models: Project, Device, SourceFile, SearchQuery, CodeAnalysis
  - Application use cases: OpenProjectUseCase, CloseProjectUseCase, SearchCodeUseCase, etc.
  - Infrastructure adapters: JadxProjectRepositoryAdapter, SimpleEventPublisher
- IDE Integration Quick Start guide (`docs/IDE_INTEGRATION.md`)
- Hero banner and logo images in README

### Fixed
- SwingNotificationAdapter: Added missing `notifyInfo()` and `notifyProgress()` methods
- ExportReportUseCase: Added missing ProjectRepository import
- PullAndDecompileUseCase: Removed undefined PullException reference

### Changed
- Window title displays "DexForge GUI" instead of "jadx-gui"
- About dialog shows "DexForge GUI, powered by JADX"
- CLI help banner displays "DexForge CLI, powered by JADX"
- Desktop entry uses DexForge GUI branding

### Tests
- Added unit tests for core use cases:
  - OpenProjectUseCaseTest
  - CloseProjectUseCaseTest
  - SearchCodeUseCaseTest
  - ListPackagesUseCaseTest
  - ListDevicesUseCaseTest

---

## Future Releases

### Phase 5 - IDE Extensions (In Progress)
- VS Code extension marketplace listing
- IntelliJ/Android Studio plugin integration
- Binary PATH installation scripts

### Phase 6 - Public Release (Planned)
- Windows installer (.msi)
- Linux packages (.deb, .rpm)
- macOS app bundle (.dmg)
- API stability guarantees
- Migration guide from JADX