#!/usr/bin/env python3
"""Login form or logged-in panel."""
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
flush_headers()
if session_isset("username"):
    # Original required loggedin.php
    import runpy
    runpy.run_path(str(Path(__file__).resolve().parent / "loggedin.py"), run_name="__main__")
else:
    html_start()
    echo("""<html>
<head>
	<h3>LOGIN</h3>

	<div align="left"> <a href=""></a> </div>

</head>

<body>
<div id="faq" class="faq">
<form name="rec" id= "rec" method="post" action="javascript:getlog(document.getElementById('regform'));" onsubmit="return validatelogin()">

<table  border="0" align="center" class="box">
	<tr>
<td></td></tr>
<tr><td><font color="red"><blink><label id="loginmsg"></label></blink></font></td></tr>
<tr>
<td>
USERNAME:
<input type="text" id="username" name="username" ></td>&nbsp;
</tr>
<tr>
<td>
PASSWORD:
<input type="password" id="password" name="password" ></td>
</tr></table>
<input type="submit" name="submit" value="SUBMIT">
&nbsp;&nbsp;&nbsp;<input type="reset" value="CLEAR"><br><a href="javascript:ajax('../login/forgotpswd.py','login')">ForgotPassword!</a>&nbsp;New User&nbsp;<a href="javascript:ajax('../register/index.py','content')">Sign Up</a>

</form>
</div>
</body>

</html>
""")
session_save()
