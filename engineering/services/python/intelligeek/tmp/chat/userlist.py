#!/usr/bin/env python3
"""List online Chatr users."""
from __future__ import annotations

import sys
from pathlib import Path

_HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(_HERE))
sys.path.insert(0, str(_HERE.parents[2]))
from _cgiutil import echo, flush_headers, html_start  # noqa: E402
import config as cfg  # noqa: E402

flush_headers()
html_start()
path = _HERE / cfg.users_file
text = path.read_text(encoding="utf-8", errors="replace") if path.exists() else ""
for line in text.splitlines():
    data = line.split(",")
    if len(data) > 1:
        echo(f"<li>{data[1]}</li>")
