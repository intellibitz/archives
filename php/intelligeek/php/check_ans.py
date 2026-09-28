#!/usr/bin/env python3
"""Check quiz answers from query string."""
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


flush_headers()
html_start()
no_of_questions = int(get("no_of_questions") or 0)
ans = {}
for i in range(1, no_of_questions + 1):
    ans[i] = get(f"ans.{i}")
echo(str(ans))
