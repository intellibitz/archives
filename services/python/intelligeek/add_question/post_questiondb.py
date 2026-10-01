#!/usr/bin/env python3
"""Insert question into DB."""
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
sub_id = mysql_real_escape_string(post("sub_id"))
level = mysql_real_escape_string(post("level"))
q = mysql_real_escape_string(post("q"))
a = mysql_real_escape_string(post("a"))
b = mysql_real_escape_string(post("b"))
c = mysql_real_escape_string(post("c"))
d = mysql_real_escape_string(post("d"))
ans = mysql_real_escape_string(post("ans"))
mysql_query(
    f"insert into question values('','{sub_id}','{level}','{q}','{a}','{b}','{c}','{d}','{ans}')"
)
echo("Question posted")
