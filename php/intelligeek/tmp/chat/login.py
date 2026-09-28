#!/usr/bin/env python3
"""Chatr login."""
from __future__ import annotations

import random
import sys
from pathlib import Path

_HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(_HERE))
sys.path.insert(0, str(_HERE.parents[2]))
from _cgiutil import echo, flush_headers, html_start, post  # noqa: E402
import config as cfg  # noqa: E402

userid = post("u")
password = post("p")
flush_headers()
uid = str(random.randint(100000001, 999999999))
if userid == cfg.admin_nick and password == cfg.admin_password:
    uid = str(cfg.admin_id)
users_path = _HERE / cfg.users_file
line = f"{uid},{userid}\n"
prev = users_path.read_text(encoding="utf-8", errors="replace") if users_path.exists() else ""
users_path.write_text(line + prev, encoding="utf-8")
html_start()
echo(f"<script>location.href='chat.py?u={uid}'</script>")
