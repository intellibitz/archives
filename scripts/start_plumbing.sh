#!/usr/bin/env bash
# ==============================================================================
# LiteLLM Proxy Gateway Service Launcher (Plumbing Layer)
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(dirname "$SCRIPT_DIR")"
CONFIG_FILE="${WORKSPACE_ROOT}/orchestrator/litellm_config.yaml"
PORT="${LITELLM_PROXY_PORT:-4000}"
HOST="${LITELLM_PROXY_HOST:-127.0.0.1}"

# Load .env if present
if [ -f "${WORKSPACE_ROOT}/.env" ]; then
    echo "Loading environment from ${WORKSPACE_ROOT}/.env..."
    set -a
    # shellcheck disable=SC1091
    source "${WORKSPACE_ROOT}/.env"
    set +a
fi

echo "======================================================================"
echo " Starting LiteLLM Multi-Model Orchestration Proxy Gateway"
echo " Host: http://${HOST}:${PORT}"
echo " Config: ${CONFIG_FILE}"
echo "======================================================================"

# Check if litellm is installed
if ! command -v litellm &> /dev/null; then
    echo "❌ litellm command not found! Ensure pipx or pip install litellm is installed."
    exit 1
fi

exec litellm \
    --config "${CONFIG_FILE}" \
    --host "${HOST}" \
    --port "${PORT}" \
    --telemetry False
