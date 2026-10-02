#!/usr/bin/env python3
"""Forgot-password form."""
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


flush_headers()
html_start()
echo("""

<html>
<head><div align="right"> <a href=""></a> </div>
<script type="text/javascript" src="../js/fpswd.js"/>
</head>
<body>
<form id="fpswd" name="fpswd" method="post" action="javascript:getforgotpswd(document.getElementById('fpswd'));" onsubmit="return validatefpswd()">
<table>
<tr><td>Email:</td><td><input type="text" id="email" name="email"></td></tr>
<tr><td colspan="2"><input type="submit" value="SUBMIT"></td></tr>
</table>
</form>
</body>
</html>
""")
