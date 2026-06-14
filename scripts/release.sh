#!/usr/bin/env bash
set -euo pipefail

# Simple release helper for jadx GitHub binary assets
# Usage:
#   ./scripts/release.sh v1.0.0
#   ./scripts/release.sh v1.0.0 --notes-file release-notes.md

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <tag> [--notes-file <file>] [--artifact <file>]"
  exit 1
fi

TAG="$1"
shift
NOTES_FILE=""
ARTIFACT=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --notes-file)
      shift
      NOTES_FILE="${1:-}"
      ;;
    --artifact)
      shift
      ARTIFACT="${1:-}"
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
  shift
done

if [[ -z "$ARTIFACT" ]]; then
  ARTIFACT="build/jadx/jadx-$TAG.zip"
fi

if [[ ! -f "$ARTIFACT" ]]; then
  echo "Artifact not found: $ARTIFACT"
  echo "Run './gradlew dist' first or pass --artifact <file>"
  exit 2
fi

if [[ -n "$NOTES_FILE" && ! -f "$NOTES_FILE" ]]; then
  echo "Notes file not found: $NOTES_FILE"
  exit 3
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI 'gh' is required for release upload"
  exit 4
fi

if ! git diff --quiet --ignore-submodules --; then
  echo "Working tree is dirty. Commit or stash changes before releasing."
  exit 5
fi

if ! git rev-parse --verify "$TAG" >/dev/null 2>&1; then
  echo "Creating annotated tag $TAG"
  git tag -a "$TAG" -m "Release $TAG"
  git push origin "$TAG"
else
  echo "Tag $TAG already exists"
fi

if [[ -n "$NOTES_FILE" ]]; then
  gh release create "$TAG" "$ARTIFACT" --title "$TAG" --notes-file "$NOTES_FILE"
else
  gh release create "$TAG" "$ARTIFACT" --title "$TAG" --notes "Binary-only release for end users."
fi

printf "Release %s created with artifact %s\n" "$TAG" "$ARTIFACT"
