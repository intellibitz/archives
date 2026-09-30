#!/usr/bin/env python3
"""Reset password and email it."""
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
flush_headers()
html_start()

def createPassword(length=8):
    chars = string.ascii_letters + string.digits
    return "".join(random.choice(chars) for _ in range(length))

email = mysql_real_escape_string(post("email") or get("email"))
if email:
    result = mysql_query(f"select * from user where email='{email}'")
    rows = []
    # drain via num_rows cache
    n = mysql_num_rows(result)
    if n != 0:
        a = mysql_fetch_row(result)
        # row layout depends on schema; username often index 1
        password = createPassword(8)
        mysql_query(f"update user SET password=PASSWORD('{password}') where email='{email}'")
        uname = a[1] if a and len(a) > 1 else ""
        message = (
            f"Hi! {uname} now you can use this password to login to your account,you may change your password later."
            f"<br>Your username is: {uname}<br>Your password is: {password}<br><br>Thank You"
        )
        if _mail(email, "IntelliGame password", message):
            echo("<p>Message successfully sent!</p>")
        else:
            echo("<p>Message delivery failed...</p>")
        raise SystemExit()
    else:
        echo("Enter The Correct Email-Address!")
