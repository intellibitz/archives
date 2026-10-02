#!/usr/bin/env python3
"""Create user account."""
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
username = mysql_real_escape_string(post("username1"))
password = mysql_real_escape_string(post("password1"))
email = mysql_real_escape_string(post("email"))
mailquiz = mysql_real_escape_string(post("quizmail"))
mobile = mysql_real_escape_string(post("mobile"))
smsquiz = mysql_real_escape_string(post("quizsms"))
gender = mysql_real_escape_string(post("gender"))
dob = mysql_real_escape_string(post("dob"))
try:
    result = mysql_query(
        f"insert into user values('','{username}',PASSWORD('{password}'),'{email}','{mailquiz}','{mobile}','{smsquiz}','{gender}','{dob}')"
    )
    ok = result is not None
except Exception:
    ok = False
    echo("email id already exists")
if not ok:
    message = " SORRY ! REGISTRATION FAILED"
else:
    message = "Thank you for registering with us."
echo(f"<div id='message' class='message'><p align='center'>{message}</p></div>")
echo("""<html>
	<head>
	</head>
	</body>
</html>
""")
