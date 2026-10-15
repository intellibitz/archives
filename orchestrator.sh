#!/usr/bin/env bash
# ==============================================================================
# Agent Orchestrator Quick-Start Launcher
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export PYTHONPATH="${SCRIPT_DIR}:${PYTHONPATH:-}"

# Load persistent AI API keys if present
if [ -f "/mnt/data/var/AI/env_api_keys.sh" ]; then
    set -a
    # shellcheck disable=SC1091
    source "/mnt/data/var/AI/env_api_keys.sh"
    set +a
fi

PYTHON_BIN="python3"
if [ -x "${SCRIPT_DIR}/.venv-orchestrator/bin/python" ]; then
    PYTHON_BIN="${SCRIPT_DIR}/.venv-orchestrator/bin/python"
fi

exec "${PYTHON_BIN}" -m orchestrator.cli "$@"
