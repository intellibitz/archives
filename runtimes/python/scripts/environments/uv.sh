#!/usr/bin/env bash
# https://github.com/astral-sh/uv
# Fast Python package installer and resolver

set -euo pipefail

echo "=== Installing Astral uv ==="
curl -LsSf https://astral.sh/uv/install.sh | sh

# Source environment
export PATH="$HOME/.local/bin:$PATH"

echo "=== Verifying uv installation ==="
uv --version

# Optional: Install Python 3.13 and create virtual environment
# uv python install 3.13
# uv venv --python 3.13
