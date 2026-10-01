#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export DISPLAY=:0
nohup /usr/bin/python3 -u "$SCRIPT_DIR/auto_keep_all.py" >> /tmp/auto_keep_all.log 2>&1 &
echo "auto_keep_all.py running in background (log: /tmp/auto_keep_all.log)."
