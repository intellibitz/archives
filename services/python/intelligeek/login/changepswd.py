#!/usr/bin/env python3
"""Change-password form."""
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
html_start()
if session_isset("username"):
    u = session_get("username")
    echo(f"""<html>
<head><div align="left"><u>{u}</u> | <a href="javascript:ajax('../login/loggedin.py','login')">Home</a> | <a href="../login/logout.py">Logout</a></div>

</head>
<body>


<form name="chg" id="chg" method="post" action="javascript:getchangepswd(document.getElementById('chg'));" onsubmit="return validatechange();">
<table id="cpswd"  class="cpswd" >
<tr align="left"><th>Change Password</th></tr>
<tr></tr>
<tr><td></td><td><font color="red"><blink><label id="faq" ></label></blink></font></td></tr>
<tr><td>
&nbsp;&nbsp;&nbsp;&nbsp;Old Password<font color="red">*</font></td><td><input type="password" id="oldpwsd" name="oldpwsd" ></td></tr>
<tr><td>
&nbsp;&nbsp;&nbsp;&nbsp;New Password<font color="red">*</font></td><td><input type= "password" id="newpwsd" name="newpwsd" ></td></tr>
<tr align="center"><td align="right"><input type="submit" name="submit" value="SUBMIT"></td><td align="left"><input type="reset" name="reset" value="CLEAR"></td></tr>
</table>
</form>
</body>
</html>
""")
else:
    session_destroy()
    echo(" <i>Hello Guest! Your are not logged in</i>")
    echo("""      <a href="javascript:ajax('../login/index.py','login')">Login </a>  Here

""")
session_save()
