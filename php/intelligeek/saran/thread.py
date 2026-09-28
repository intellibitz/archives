#!/usr/bin/env python3
"""Fetch saran chat thread."""
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
    result = mysql_query(
        "SELECT Chat_Users.UserName, Chat_Messages.Message, Chat_Messages.Posted "
        "FROM Chat_Messages JOIN Chat_Users ON Chat_Users.UserID=Chat_Messages.UserID "
        "ORDER BY MessageID DESC LIMIT 50"
    )
    while True:
        row = mysql_fetch_row(result)
        if not row:
            break
        echo(f"{row[0]}: {row[1]} ({row[2]})<br>")
session_save()
