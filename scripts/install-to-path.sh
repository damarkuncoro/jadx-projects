#!/bin/bash
# DexForge PATH Installer
# Adds dexforge executable to system PATH

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
INSTALL_DIR="${DEXFORGE_INSTALL_DIR:-/usr/local/bin}"

# Find the latest version zip or use current directory
if [[ -d "$SCRIPT_DIR/lib" ]]; then
	JADX_DIR="$SCRIPT_DIR"
else
	echo "Error: Could not find lib/ directory. Run this from the extracted distribution."
	exit 1
fi

# Create symlink or copy scripts
if [[ -w "$INSTALL_DIR" ]]; then
	ln -sf "$JADX_DIR/bin/dexforge" "$INSTALL_DIR/dexforge"
	ln -sf "$JADX_DIR/bin/dexforge-gui" "$INSTALL_DIR/dexforge-gui"
	echo "DexForge installed to $INSTALL_DIR"
	echo "You can now run 'dexforge' and 'dexforge-gui' from any terminal."
else
	echo "Need sudo to install to $INSTALL_DIR"
	sudo ln -sf "$JADX_DIR/bin/dexforge" "$INSTALL_DIR/dexforge"
	sudo ln -sf "$JADX_DIR/bin/dexforge-gui" "$INSTALL_DIR/dexforge-gui"
	echo "DexForge installed to $INSTALL_DIR (with sudo)"
fi

# Add to shell profile if not already present
SHELL_PROFILE=""
if [[ -n "$ZSH_VERSION" ]]; then
	SHELL_PROFILE="$HOME/.zshrc"
elif [[ -n "$BASH_VERSION" ]]; then
	SHELL_PROFILE="$HOME/.bashrc"
fi

if [[ -n "$SHELL_PROFILE" && -f "$SHELL_PROFILE" ]]; then
	if ! grep -q "dexforge" "$SHELL_PROFILE" 2>/dev/null; then
		echo "" >> "$SHELL_PROFILE"
		echo "# DexForge Engine" >> "$SHELL_PROFILE"
		echo "export DEXFORGE_HOME=\"$JADX_DIR\"" >> "$SHELL_PROFILE"
		echo 'export PATH="$DEXFORGE_HOME/bin:$PATH"' >> "$SHELL_PROFILE"
		echo "Added DEXFORGE_HOME to $SHELL_PROFILE"
		echo "Run 'source $SHELL_PROFILE' or restart your terminal"
	fi
fi