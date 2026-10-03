#!/usr/bin/env python3
"""Write Chatr config.py from install form."""
from __future__ import annotations

import random
import sys
from pathlib import Path

_HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(_HERE))
sys.path.insert(0, str(_HERE.parents[2]))
from _cgiutil import echo, flush_headers, get, html_start, post, redirect  # noqa: E402

flush_headers()
if (_HERE / "config.py").exists() and get("force") != "1":
    redirect("install.py?alert=1")
    raise SystemExit()
number = random.randint(1000, 10000)
admin_num = number * 100000 + 1
path_to_chat = post("path") or "./"
admin_nick = post("admin_nick") or "admin"
admin_pwd = post("password") or "admin"
b, u, p = f"b{number}.txt", f"u{number}.txt", f"p{number}.txt"
cfg_txt = (
    f"path_to_chat = {path_to_chat!r}\n"
    f"admin_id = {str(admin_num)!r}\n"
    f"admin_nick = {admin_nick!r}\n"
    f"admin_password = {admin_pwd!r}\n"
    f"buffer_file = {b!r}\n"
    f"users_file = {u!r}\n"
    f"pings_file = {p!r}\n"
    "timeout = 62\n"
    "maxlines = 20\n"
)
(_HERE / "config.py").write_text(cfg_txt, encoding="utf-8")
for f in (b, u, p):
    (_HERE / f).write_text("", encoding="utf-8")
html_start()
echo("Install complete. <a href='index.py'>Login</a>")
