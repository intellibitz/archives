#!/usr/bin/env python3
"""Chat buffer poll (Chatr)."""
from __future__ import annotations

import sys
from pathlib import Path

_HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(_HERE))
sys.path.insert(0, str(_HERE.parents[2]))
from _cgiutil import echo, flush_headers, get, html_start  # noqa: E402
import config as cfg  # noqa: E402

userid = get("u")
d = get("d")
flush_headers()
html_start()
users_path = _HERE / cfg.users_file
users = users_path.read_text(encoding="utf-8", errors="replace") if users_path.exists() else ""
if userid not in users:
    echo(
        userid
        + "timeout<br><br><br><br><br><br><br><br><br><center><li class='servermsg'>"
        + "You have timed out, please <a href='"
        + cfg.path_to_chat
        + "' target='_top'>login</a> again</li></center>"
    )
    raise SystemExit()
buf_path = _HERE / cfg.buffer_file
file_txt = buf_path.read_text(encoding="utf-8", errors="replace") if buf_path.exists() else ""
lines = file_txt.split("\n")
num = max(0, len(lines) - 1)
maxlines = int(getattr(cfg, "maxlines", 20))
start = 0 if (num < maxlines * 2 or d == "1") else num - (maxlines * 2)
if d == "1":
    echo(
        "<html><head><title>Chatr Archive</title>"
        "<link rel='stylesheet' href='style.css' type='text/css' ></head>"
        "<body style='width:620px'><div id='chatpane'><ul id='chatbuffer'>"
    )
echo("<div width='50%'>")
i = start
while i < num:
    msg = lines[i + 1] if i + 1 < len(lines) else ""
    echo(lines[i] + msg)
    i += 2
echo("</div>")
if d == "1":
    echo("</ul></div></body></html>")
