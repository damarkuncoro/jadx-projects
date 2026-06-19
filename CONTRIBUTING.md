# Contributing to DexForge

Thank you for your interest in contributing to DexForge! This document provides guidelines and instructions for contributing.

## Code of Conduct

Please note that we have a [Code of Conduct](CODE_OF_CONDUCT.md). Please follow it in all your interactions with the project.

## Quick Start for New Contributors

### Good First Issues

We label issues with [`good first issue`](https://github.com/damarkuncoro/jadx-projects/labels/good%20first%20issue) for newcomers. These issues are:

- Well-scoped and self-contained
- Do not require deep knowledge of the codebase
- Include clear acceptance criteria

**How to find them:**
1. Visit [GitHub Issues](https://github.com/damarkuncoro/jadx-projects/issues)
2. Filter by label: `good first issue`
3. Comment on the issue to claim it
4. Wait for maintainer confirmation before starting

### Development Setup

#### Prerequisites

- **JDK 21+** (Temurin/OpenJDK recommended)
- **Gradle 8+** (wrapper included)
- **Git**
- **Docker** (optional, for containerized builds)

#### Clone and Build

```bash
# Clone the repository
git clone https://github.com/damarkuncoro/jadx-projects.git
cd jadx-projects

# Copy environment configuration
cp .env.example .env

# Build the project
./gradlew build

# Run tests
./gradlew test

# Launch GUI
./gradlew :jadx-gui:run

# Run CLI
./gradlew :dexforge-cli:run --args="--help"
```

#### Using Just (Recommended)

We provide a [`justfile`](justfile) for common tasks:

```bash
# Install just: https://github.com/casey/just

# Show all available commands
just

# Quick development cycle
just dev-check

# Format code
just fmt

# Build and test
just build
just test
```

## Contribution Workflow

### 1. Find or Create an Issue

- Search existing issues to avoid duplicates
- For bugs: provide reproduction steps, stack trace, and sample APK
- For features: describe the use case and proposed solution

### 2. Fork and Branch

```bash
# Fork the repository on GitHub, then:
git clone https://github.com/YOUR_USERNAME/jadx-projects.git
cd jadx-projects

# Create a feature branch
git checkout -b feature/my-new-feature
# or
git checkout -b fix/issue-123
```

### 3. Make Changes

- Follow the existing code style (enforced by Spotless)
- Write tests for new functionality
- Update documentation if needed
- Keep commits focused and atomic

### 4. Code Quality Checks

Before submitting, ensure:

```bash
# Format code
just fmt

# Check formatting
just fmt-check

# Run tests
just test

# Full verification
just check
```

### 5. Commit and Push

```bash
# Stage changes
git add .

# Commit with descriptive message
git commit -m "feat: add new feature X

- Detailed description of changes
- Closes #123"

# Push to your fork
git push origin feature/my-new-feature
```

### 6. Create Pull Request

- Use the [PR template](.github/pull_request_template.md)
- Link to the related issue
- Describe what changed and why
- Include screenshots for UI changes

## Code Style

### Java/Kotlin

- **Java 11+** features only (for compatibility)
- **4 spaces** indentation (no tabs)
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable names
- Add Javadoc for public APIs

### Formatting

We use [Spotless](https://github.com/diffplug/spotless) for code formatting:

```bash
# Auto-format all code
./gradlew spotlessApply

# Check formatting without modifying
./gradlew spotlessCheck
```

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add new decompilation option
fix: resolve crash when loading malformed APK
docs: update LSP daemon API documentation
refactor: simplify Frida script generation
test: add unit tests for DaemonService
chore: update Gradle wrapper to 8.5
```

## Project Structure

```
jadx-projects/
├── jadx-core/              # Core decompiler engine
├── jadx-gui/               # Swing desktop GUI
├── dexforge-cli/           # Command-line interface
├── dexforge-vscode/        # VS Code extension
├── dexforge-plugins-tools/ # Plugin management system
├── dexforge-commons/       # Shared utilities
├── jadx-plugins/           # Input/output plugins
│   ├── dexforge-frida-integration/
│   ├── dexforge-ad-detector/
│   └── ...
├── docs/                   # Documentation
├── docker/                 # Docker configuration
└── .github/workflows/      # CI/CD pipelines
```

## Testing

### Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :jadx-core:test

# With coverage
./gradlew test jacocoTestReport
```

### Writing Tests

- Place unit tests in `src/test/java/`
- Place integration tests in `src/integrationTest/java/`
- Use descriptive test names: `testMethodName_expectedBehavior()`
- Mock external dependencies

## Reporting Bugs

### Bug Report Template

When reporting bugs, include:

1. **Environment:**
   - OS: (e.g., macOS 14.0, Ubuntu 22.04, Windows 11)
   - Java version: `java -version`
   - DexForge version: (from `dexforge --version`)

2. **Steps to Reproduce:**
   - Detailed steps to trigger the bug
   - Sample APK file (rename to `.zip` if needed)

3. **Expected vs Actual Behavior:**
   - What you expected to happen
   - What actually happened

4. **Additional Context:**
   - Stack trace
   - Screenshots (for GUI issues)
   - Logs (enable debug logging if needed)

## Suggesting Features

### Feature Request Template

1. **Problem Statement:**
   - What problem does this solve?
   - Who benefits from this feature?

2. **Proposed Solution:**
   - How should it work?
   - UI/UX mockups if applicable

3. **Alternatives Considered:**
   - What other approaches did you consider?
   - Why is the proposed solution better?

4. **Additional Context:**
   - Links to similar features in other tools
   - Use cases and examples

## Areas Where We Need Help

### High Priority

- **VS Code Extension** - Improve LSP integration, add more features
- **IntelliJ Plugin** - Create plugin for IntelliJ IDEA / Android Studio
- **Documentation** - Translate docs, add tutorials, improve examples
- **Testing** - Add more unit tests, integration tests, E2E tests

### Good for Beginners

- **Bug fixes** - Check issues labeled `bug` and `good first issue`
- **Documentation improvements** - Fix typos, add examples, clarify instructions
- **Test coverage** - Add tests for uncovered code paths
- **UI polish** - Improve icons, layouts, accessibility

### Advanced

- **New decompilation features** - Improve code generation, add new output formats
- **Performance optimization** - Speed up decompilation, reduce memory usage
- **Plugin system** - Add new plugin APIs, improve plugin loading
- **Security hardening** - Audit code, add sandboxing, improve input validation

## Recognition

Contributors will be:

- Listed in the [contributors graph](https://github.com/damarkuncoro/jadx-projects/graphs/contributors)
- Mentioned in release notes for significant contributions
- Invited to join the DexForge maintainers team for sustained contributions

## Questions?

- **General questions:** Use [GitHub Discussions](https://github.com/damarkuncoro/jadx-projects/discussions)
- **Bug reports:** Use [GitHub Issues](https://github.com/damarkuncoro/jadx-projects/issues)
- **Security issues:** See [SECURITY.md](SECURITY.md) for responsible disclosure

Thank you for contributing to DexForge! 🚀
