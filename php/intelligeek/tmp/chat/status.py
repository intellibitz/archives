#!/usr/bin/env python3
"""Online count link for Chatr."""
from __future__ import annotations

import sys
from pathlib import Path

_HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(_HERE))
sys.path.insert(0, str(_HERE.parents[2]))
from _cgiutil import echo, flush_headers, html_start  # noqa: E402
import config as cfg  # noqa: E402
import timeout as _timeout  # noqa: F401,E402

flush_headers()
html_start()
path = _HERE / cfg.users_file
num = len(path.read_text(encoding="utf-8", errors="replace").splitlines()) if path.exists() else 0
echo(f"<a class='chatcount' href='{cfg.path_to_chat}'>Chatr ({num})</a>")
