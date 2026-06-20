#!/bin/bash
# Generate checksums for all dexforge-engine zip files in build/jadx directory
# Usage: ./scripts/generate-checksums.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BUILD_DIR="$PROJECT_ROOT/build/jadx"

if [[ ! -d "$BUILD_DIR" ]]; then
	echo "Error: Build directory not found. Run './gradlew dist' first."
	exit 1
fi

cd "$BUILD_DIR"

for zipfile in dexforge-engine-*.zip; do
	if [[ -f "$zipfile" ]]; then
		echo "Generating checksum for $zipfile"
		sha256sum "$zipfile" > "${zipfile}.sha256"
	fi
done

echo "Checksums generated successfully."