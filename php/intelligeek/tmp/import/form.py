#!/usr/bin/env python3
"""Import contacts form."""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[2]))
from _cgiutil import echo, flush_headers, html_start, post  # noqa: E402

flush_headers()
html_start()
username = post("username") or ""
echo(
    "<html><body><form method='post' action='import.py'>"
    f"Username: <input name='username' value='{username}'>"
    "Password: <input type='password' name='password'>"
    "<input type='submit' value='Import'></form></body></html>"
)
