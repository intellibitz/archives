#!/usr/bin/env python3
"""Authenticate user and mark online."""
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
flush_headers()
html_start()
if post("username") and post("password"):
    username = mysql_real_escape_string(post("username"))
    pswd = mysql_real_escape_string(post("password"))
    sql = f"SELECT * FROM user WHERE username = '{username}' AND password = PASSWORD('{pswd}')"
    result = mysql_query(sql)
    if mysql_num_rows(result) == 1:
        session_set("db_is_logged_in", True)
        session_set("auth", 1)
        session_set("username", username)
        try:
            mysql_query(f"insert into onlineuser values('','{username}',current_timestamp())")
        except Exception:
            echo("you are already loggedin")
        session_save()
        redirect("../login/loggedin.py")
        raise SystemExit()
    else:
        echo("Improper Login Please Login Again!")
session_save()
