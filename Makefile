# DexForge Development Makefile
# Alternative to justfile for developers who prefer make
# Usage: make <target>

.PHONY: help build test clean fmt fmt-check dist docker-build docker-run docker-lsp run gui check dev-check info

# Default target
help:
	@echo "DexForge Development Commands"
	@echo ""
	@echo "Usage: make <target>"
	@echo ""
	@echo "Targets:"
	@echo "  help           Show this help message"
	@echo "  build          Build the entire project"
	@echo "  test           Run all tests"
	@echo "  clean          Clean build artifacts"
	@echo "  fmt            Format code with Spotless"
	@echo "  fmt-check      Check code formatting"
	@echo "  dist           Build distribution packages"
	@echo "  dist-win       Build Windows distribution"
	@echo "  docker-build   Build Docker image"
	@echo "  docker-run     Run DexForge CLI in Docker"
	@echo "  docker-lsp     Start LSP daemon in Docker"
	@echo "  run            Run DexForge CLI"
	@echo "  gui            Run DexForge GUI"
	@echo "  check          Run all checks (format, test, build)"
	@echo "  dev-check      Quick development cycle"
	@echo "  info           Show project info"

# Build the entire project
build:
	./gradlew build -x test

# Run all tests
test:
	./gradlew test

# Clean build artifacts
clean:
	./gradlew clean

# Format code with Spotless
fmt:
	./gradlew spotlessApply

# Check code formatting
fmt-check:
	./gradlew spotlessCheck

# Build distribution packages (Linux/macOS)
dist:
	./gradlew dist

# Build Windows distribution
dist-win:
	./gradlew distWin

# Build Docker image
docker-build:
	docker build -t dexforge:latest -f docker/Dockerfile .

# Run DexForge CLI in Docker
docker-run:
	docker run --rm -v $$(pwd):/workspace dexforge:latest

# Start LSP daemon in Docker
docker-lsp:
	docker run --rm -p 8080:8080 -v $$(pwd):/workspace dexforge:latest lsp

# Run DexForge CLI
run:
	./gradlew :dexforge-cli:run

# Run DexForge GUI
gui:
	./gradlew :jadx-gui:run

# Verify all checks
check:
	./gradlew spotlessCheck test build

# Quick development cycle
dev-check: fmt build test

# Show project info
info:
	@echo "DexForge Engine - Android Reverse Engineering Workbench"
	@echo "Powered by JADX"
	@echo ""
	@echo "Quick commands:"
	@echo "  make build      - Build the project"
	@echo "  make test       - Run tests"
	@echo "  make gui        - Launch GUI"
	@echo "  make run        - Run CLI"
	@echo "  make docker-build - Build Docker image"
	@echo "  make fmt        - Format code"
	@echo "  make check      - Run all checks"
