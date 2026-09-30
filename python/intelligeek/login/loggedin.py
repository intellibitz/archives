#!/usr/bin/env python3
"""Logged-in user home / photo panel."""
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
    user = session_get("username")
    if get("id"):
        _db()
        echo(user)
        query = f"SELECT name, type, size, content FROM upload WHERE username = '{user}'"
        result = mysql_query(query)
        row = mysql_fetch_array(result)
        if row:
            name, type_, size, content = row[0], row[1], row[2], row[3]
            header(f"Content-Disposition: attachment; filename={name}")
            header(f"Content-length: {size}")
            header(f"Content-type: {type_}")
            end_headers()
            echo(content if not isinstance(content, bytes) else content.decode("latin1", errors="replace"))
        raise SystemExit()
    html_start()
    echo(f"""<html>
<head><div align="left"><u>{user}</u> | <a href="javascript:ajax('../login/changepswd.py','login')">changepswd</a> | <a href="../login/logout.py">Logout</a></div><br>

</head>
<body>
<form name="photofrm" method="post" action="" onsubmit="">
	<div align="left">
<table id="photo"   class="" width="150" >
<tr><td>""")
    _db()
    result = mysql_query(f"SELECT id, name FROM upload where username='{user}'")
    if mysql_num_rows(result) == 0:
        echo("<table  width='100px 'height='100px' border='1'>")
        echo("</table>")
    else:
        while True:
            row = mysql_fetch_array(result)
            if not row:
                break
            id_ = row[0]
            echo(f'''<img src="../login/checkimage.py?id={id_}" width="100px" height="100px"><br>
''')
    echo(f"""</td></tr>
</table>

<table id="photo1"  class="photo1" width="150">
<tr><td>&nbsp;&nbsp;&nbsp;<a href="javascript:ajax('../login/upload.py','login')">Upload Photo</a></td></tr>
 <input type="hidden" id="username" name="username" value="{user}">
</table>
<label id="msg" class="msg" />
</div>
</body>
</html>
""")
else:
    session_destroy()
    html_start()
    echo(" <i>Hello Guest! Your are not logged in</i>")
    echo("""      <a href="javascript:ajax('../login/index.py','login')">Login </a>  Here

""")
session_save()
