#!/usr/bin/env python3
"""Post a chat line (Chatr)."""
from __future__ import annotations

import html as _html
import sys
from pathlib import Path

_HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(_HERE))
sys.path.insert(0, str(_HERE.parents[2]))
from _cgiutil import echo, flush_headers, get, html_start  # noqa: E402
import config as cfg  # noqa: E402

user = get("u")
text = get("t")
flush_headers()
html_start()
echo(text)
type_ = 0
post_ok = 0
header_txt = " "
if user == str(cfg.admin_id):
    type_ = 1
    post_ok = 1
else:
    users_path = _HERE / cfg.users_file
    users = users_path.read_text(encoding="utf-8", errors="replace") if users_path.exists() else ""
    lines = users.splitlines()
    if text.find("/me") in (0, 1):
        type_ = 2
    for line in lines:
        data = line.split(",")
        if len(data) > 1 and user == data[0]:
            post_ok = 1
            if type_ == 0:
                header_txt = data[1] + ": "
            elif type_ == 2:
                header_txt = data[1] + " "
                text = text[4:]
text = _html.escape(text)
if post_ok:
    css = {0: "usermsg", 1: "systemmsg", 2: "actionmsg"}.get(type_, "usermsg")
    entry_type = f"<li class='{css}'>"
    entry = header_txt + text + "</li>\n"
    buf = _HERE / cfg.buffer_file
    prev = buf.read_text(encoding="utf-8", errors="replace") if buf.exists() else ""
    buf.write_text(entry_type + "\n" + entry + prev, encoding="utf-8")
