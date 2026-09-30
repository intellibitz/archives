#!/usr/bin/env python3
"""Post saran chat message."""
from __future__ import annotations

import sys
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(_ROOT))
from _cgiutil import (  # noqa: E402
    echo, end_headers, flush_headers, get, header, html_start,
    mysql_connect, mysql_fetch_row, mysql_num_rows, mysql_query,
    mysql_real_escape_string, mysql_select_db, post, request,
    session_destroy, session_get, session_isset, session_save,
    session_set, session_start, session_unset,
)


session_start()
mysql_connect("192.168.1.6", "geek", "geek")
mysql_select_db("geek")
flush_headers()
html_start()
if not session_isset("Chat_UserID"):
    echo("Not logged in")
else:
    msg = mysql_real_escape_string(post("message") or get("message"))
    uid = session_get("Chat_UserID")
    mysql_query(f"INSERT INTO Chat_Messages VALUES ('', {uid}, '{msg}', NOW())")
    echo("posted")
session_save()
