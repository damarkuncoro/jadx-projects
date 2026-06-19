# DexForge Development Justfile
# Install just: https://github.com/casey/just
# Usage: just <recipe>

set shell := ["bash", "-lc"]

# Show available recipes
default:
    @just --list

# Build the entire project
build:
    ./gradlew build -x test

# Run all tests
test:
    ./gradlew test

# Run tests with coverage report
test-coverage:
    ./gradlew test jacocoTestReport

# Build distribution packages (Linux/macOS)
dist:
    ./gradlew dist

# Build Windows distribution
dist-win:
    ./gradlew distWin

# Build all distributions
dist-all:
    ./gradlew dist distWin

# Build Docker image
docker-build:
    docker build -t dexforge:latest -f docker/Dockerfile .

# Run DexForge CLI in Docker
docker-run *args:
    docker run --rm -v "$(pwd):/workspace" dexforge:latest {{args}}

# Start LSP daemon in Docker
docker-lsp:
    docker run --rm -p 8080:8080 -v "$(pwd):/workspace" dexforge:latest lsp

# Clean build artifacts
clean:
    ./gradlew clean

# Format code with Spotless
fmt:
    ./gradlew spotlessApply

# Check code formatting
fmt-check:
    ./gradlew spotlessCheck

# Update dependencies
deps-update:
    ./gradlew useLatestVersions

# Check for dependency updates
deps-check:
    ./gradlew dependencyUpdates

# Run DexForge CLI
run *args:
    ./gradlew :dexforge-cli:run --args="{{args}}"

# Run DexForge GUI
gui:
    ./gradlew :jadx-gui:run

# Build shadow JAR for CLI
shadow:
    ./gradlew :dexforge-cli:shadowJar

# Verify all checks (format, test, build)
check:
    ./gradlew spotlessCheck test build

# Quick development cycle: format + build + test
dev-check:
    just fmt
    just build
    just test

# Generate LSP daemon documentation
docs-lsp:
    @echo "LSP Daemon API documentation is in docs/LSP_DAEMON_API.md"

# Generate Frida integration documentation
docs-frida:
    @echo "Frida Integration documentation is in docs/FRIDA_INTEGRATION.md"

# Show project info
info:
    @echo "DexForge Engine - Android Reverse Engineering Workbench"
    @echo "Powered by JADX"
    @echo ""
    @echo "Quick commands:"
    @echo "  just build      - Build the project"
    @echo "  just test       - Run tests"
    @echo "  just gui        - Launch GUI"
    @echo "  just run <args> - Run CLI with arguments"
    @echo "  just docker-build - Build Docker image"
    @echo "  just fmt        - Format code"
    @echo "  just check      - Run all checks"
