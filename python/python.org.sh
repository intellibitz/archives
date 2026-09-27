#!/usr/bin/env bash
# https://docs.python.org/3/using/index.html
# https://docs.python.org/3/installing/index.html

set -euo pipefail

# Standard package installation using pip
python3 -m pip install --upgrade pip

# Key standards:
# - venv is the standard tool for creating isolated environments:
#     python3 -m venv .venv
#     source .venv/bin/activate
#
# - In modern development, 'uv' is recommended for 10-100x faster package resolution:
#     curl -LsSf https://astral.sh/uv/install.sh | sh
#     uv venv
#
# Ensure pip is installed:
python3 -m ensurepip --default-pip
