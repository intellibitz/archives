#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export DISPLAY=:0
nohup /usr/bin/python3 -u "$SCRIPT_DIR/auto_click_decisions.py" >> /tmp/auto_click_decisions.log 2>&1 &
echo "auto_click_decisions.py running in background (log: /tmp/auto_click_decisions.log)."
