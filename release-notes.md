# Release Notes

## v1.5.7

This release introduces build stack analysis helpers and refactoring updates.

- Refactored build stack analysis out of the GUI controllers to `BuildStackDetector`.
- Enhanced APK path parsing to support Indonesian locale and pt-BR regional splits.
- Fixed unit test failures in `DexResourceTest` by updating mocked DEX headers.
- Built with `./gradlew dist`
- Artifact published: `build/jadx-dev.zip`
- Release created with `scripts/release.sh v1.5.7`

## v1.5.6

This release is a binary-only distribution generated from the current `main` branch.

- Built with `./gradlew dist`
- Artifact published: `build/jadx-dev.zip`
- Release created with `scripts/release.sh v1.5.6`

Included files:
- `bin/` launcher scripts
- `lib/` runtime JARs
- `README.md`
- `LICENSE`

Notes:
- The repo remains the source of truth; the GitHub release asset is a ready-to-use distribution.
- For environment setup and build requirements, see `docs/PROJECT_REQUIREMENTS.md`.
- Use Java 17+ for best compatibility.
