#!/usr/bin/env python3
"""Start quiz play session."""
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


session_start()
_db()
flush_headers()
html_start()
topic = get("topic_is") or session_get("topic_is") or ""
from _cgiutil import session_set
if topic:
    session_set("topic_is", topic)
echo(f"<h3>Playing topic: {session_get('topic_is')}</h3>")
# Original fetched questions by subject/level — simplified list
result = mysql_query("select q,a,b,c,d from question limit 10")
while True:
    row = mysql_fetch_row(result)
    if not row:
        break
    echo(f"<p>{row}</p>")
session_save()
