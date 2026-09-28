#!/usr/bin/env python3
"""Saran chat login/logout."""
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
if get("logout"):
    uid = session_get("Chat_UserID")
    if uid:
        mysql_query(f"DELETE FROM Chat_Users WHERE UserID = {uid}")
    session_unset()
    session_destroy()
    echo("Logged out")
else:
    user = post("username") or get("username")
    if user:
        user = mysql_real_escape_string(user)
        mysql_query(f"INSERT INTO Chat_Users VALUES ('', '{user}', NOW())")
        # fetch id
        result = mysql_query(f"SELECT UserID FROM Chat_Users WHERE UserName='{user}' ORDER BY UserID DESC LIMIT 1")
        row = mysql_fetch_row(result)
        if row:
            session_set("Chat_UserID", row[0])
            session_set("Chat_UserName", user)
        echo("OK")
    else:
        echo("""<form method="post"><input name="username"><input type="submit" value="Login"></form>""")
session_save()
