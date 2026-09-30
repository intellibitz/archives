#!/usr/bin/env python3
"""Insert FAQ question."""
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
question = mysql_real_escape_string(post("question"))
emailid = mysql_real_escape_string(post("emailid"))
mysql_query(
    f"insert into faq values('','{question}','','{emailid}','',current_timestamp())"
)
echo("Question submitted")
