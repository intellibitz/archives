#!/usr/bin/env python3
"""Update Chatr user ping timestamp."""
from __future__ import annotations

import sys
import time
from pathlib import Path

_HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(_HERE))
sys.path.insert(0, str(_HERE.parents[2]))
from _cgiutil import echo, flush_headers, get, html_start  # noqa: E402
import config as cfg  # noqa: E402

userid = get("u")
flush_headers()
html_start()
try:
    uid_i = int(userid)
except Exception:
    echo("invalid user id")
    raise SystemExit()
if uid_i < 100000000 or uid_i > 1000000000:
    echo("invalid user id")
    raise SystemExit()
pings_path = _HERE / cfg.pings_file
lines = pings_path.read_text(encoding="utf-8", errors="replace").splitlines() if pings_path.exists() else []
now = str(int(time.time()))
found = False
for i, line in enumerate(lines):
    data = line.split(",")
    if data and userid in data[0]:
        data[1] = now
        lines[i] = ",".join(data)
        found = True
        break
if not found:
    lines.insert(0, f"{userid},{now}")
pings_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
import timeout  # noqa: F401
echo("ok")
