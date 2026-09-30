#!/usr/bin/env python3
"""Change password for logged-in user."""
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
oldpass = post("oldpwsd")
newpass = post("newpwsd")
select = mysql_query(f"select * from user where password=PASSWORD('{oldpass}')")
if mysql_num_rows(select) != 0:
    mysql_query(f"update user SET password=PASSWORD('{newpass}') where username='{user}'")
    echo("Password Changed sucessfully")
else:
    echo("Enter The Old Password Correctly!")
session_save()
