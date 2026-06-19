# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Docker support for DexForge CLI with multi-stage builds
- GitHub Actions workflow for automated Docker image builds and pushes
- GitHub Actions workflow for automated APK analysis with security pattern scanning
- GitHub Actions workflow for VS Code extension CI/CD
- Comprehensive LSP Daemon API documentation
- Enhanced Frida Integration documentation
- Security guidelines for Frida usage
- Docker Compose configuration for easier container management
- Pre-commit hooks configuration for code quality
- `.env.example` template for build configuration
- `justfile` for development workflow automation
- VS Code extension marketplace README

### Changed
- Updated CONTRIBUTING.md with good first issue guidance and development setup instructions

### Planned
- Phase 5: IDE Extensions (VS Code and IntelliJ plugins)
- Phase 6: Public Release with stabilized APIs

## [1.0.0-alpha] - 2024-XX-XX

### Added
- Initial DexForge rebranding from JADX
- DexForge CLI with LSP daemon mode
- DexForge GUI with Swing desktop application
- Device Explorer for ADB-driven APK extraction
- Android XML Layout Viewer with visual preview
- Frida Integration for dynamic instrumentation
- Build Stack Detection (Flutter, React Native, Unity, etc.)
- Plugin management system
- VS Code extension (dexforge-vscode)

### Changed
- Distribution artifacts renamed to `dexforge-engine-<version>.zip`
- Binary aliases: `dexforge`, `dexforge-gui`
- Legacy compatibility aliases: `jadx`, `jadx-gui`

---

## Versioning

We use [Semantic Versioning](https://semver.org/). Given a version number `MAJOR.MINOR.PATCH`:

- **MAJOR** version for incompatible API changes
- **MINOR** version for new functionality in a backwards compatible manner
- **PATCH** version for backwards compatible bug fixes

## Categories

Each version section may include:

- **Added** for new features
- **Changed** for changes in existing functionality
- **Deprecated** for soon-to-be removed features
- **Removed** for now removed features
- **Fixed** for any bug fixes
- **Security** in case of vulnerabilities

## Contributing

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute.
