#!/usr/bin/env python3
"""Serve uploaded image by id."""
from __future__ import annotations

import os
import sys
import smtplib
import random
import string
from email.mime.text import MIMEText
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(_ROOT))
from _cgiutil import (  # noqa: E402
    echo, end_headers, files, flush_headers, get, header, html_start,
    mysql_fetch_array, mysql_fetch_row, mysql_num_rows, mysql_query,
    mysql_real_escape_string, post, redirect, request,
    session_destroy, session_get, session_isset, session_save,
    session_set, session_start, session_unset,
)

def _db():
    import config.config  # noqa: F401

def _mail(to, subject, message, frm="admin@intelligame.com"):
    try:
        msg = MIMEText(message, "html")
        msg["Subject"] = subject
        msg["From"] = frm
        msg["To"] = to
        smtplib.SMTP("localhost").send_message(msg)
        return True
    except Exception:
        return False


_db()
if get("id"):
    id_ = get("id")
    result = mysql_query(f"SELECT name, type, size, content FROM upload WHERE id = '{id_}'")
    row = mysql_fetch_array(result)
    if row:
        name, type_, size, content = row[0], row[1], row[2], row[3]
        flush_headers()
        header(f"Content-Disposition: attachment; filename={name}")
        header(f"Content-length: {size}")
        header(f"Content-type: {type_}")
        end_headers()
        echo(content if not isinstance(content, bytes) else content.decode("latin1", errors="replace"))
        raise SystemExit()
flush_headers()
html_start()
echo("""<html>
<head>


</head>

<body>
""")
result = mysql_query("SELECT id, name FROM upload")
if mysql_num_rows(result) == 0:
    echo("Database is empty <br>")
else:
    while True:
        row = mysql_fetch_array(result)
        if not row:
            break
        id_, name = row[0], row[1]
        echo("	Here is your picture:<br>")
        echo(f'<img src="../login/checkimage.py?id={id_}"><br>')
echo("""
</body>
</html>
""")
