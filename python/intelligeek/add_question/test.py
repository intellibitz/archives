#!/usr/bin/env python3
"""Legacy post-a-question test page."""
from __future__ import annotations

import sys
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(_ROOT))
from _cgiutil import (  # noqa: E402
    echo, flush_headers, get, html_start, mysql_fetch_row, mysql_num_rows,
    mysql_query, mysql_real_escape_string, post, request, session_get,
    session_save, session_start,
)

def _db():
    import config.config  # noqa: F401


_db()
flush_headers()
html_start()
echo("<html><head><title>Post a Question</title></head><body>")
echo("<h3>Post a Question (test)</h3>")
result = mysql_query("select * from question limit 20")
while True:
    row = mysql_fetch_row(result)
    if not row:
        break
    echo(f"<pre>{row}</pre>")
echo("</body></html>")
