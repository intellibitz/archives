#!/usr/bin/env bash
# ==============================================================================
# Complete Ubuntu / Debian Package Maintenance & Upgrade Script
# ==============================================================================

set -euo pipefail

echo "=== Updating package index ==="
sudo apt update

echo "=== Upgrading installed packages ==="
sudo apt upgrade -y

echo "=== Performing full distribution upgrade ==="
sudo apt full-upgrade -y

echo "=== Fixing broken dependencies if any ==="
sudo apt --fix-broken install -y

echo "=== Removing unused packages and purge configuration files ==="
sudo apt autoremove --purge -y

echo "=== Cleaning package cache archives ==="
sudo apt autoclean -y
sudo apt clean

echo "=== System upgrade and maintenance complete ==="
