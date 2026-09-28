#!/usr/bin/env python3
"""Store uploaded photo blob."""
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


session_start()
_db()
user = session_get("username")
flush_headers()
html_start()
f = files("userfile")
if f is not None:
    # cgi FieldStorage-like
    fileName = getattr(f, "filename", None) or ""
    tmpName = getattr(f, "file", None)
    fileType = getattr(f, "type", "") or ""
    if tmpName is not None:
        content = tmpName.read()
        fileSize = len(content)
    else:
        content = b""
        fileSize = 0
    # escape for SQL — prefer parameterized; legacy used addslashes
    from _cgiutil import mysql_real_escape_string as esc
    # store as binary escape
    content_s = esc(content.decode("latin1", errors="replace") if isinstance(content, bytes) else content)
    fileName_s = esc(fileName)
    select = mysql_query(f"select * from upload where username='{user}'")
    if mysql_num_rows(select) != 0:
        q = mysql_query(
            f"update upload SET name= '{fileName_s}' , size='{fileSize}' , type='{fileType}', content='{content_s}' where username='{user}'"
        )
        echo(f"File {fileName} updated" if q is not None else "Image Cannot be Updated")
    else:
        mysql_query(
            "INSERT INTO upload (name, size, type, content, username ) "
            f"VALUES ('{fileName_s}', '{fileSize}', '{fileType}', '{content_s}', '{user}')"
        )
        echo(f"File {fileName} uploaded")
session_save()
