#!/usr/bin/env python3
"""List other online users for the sidebar."""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from _cgiutil import (  # noqa: E402
    echo,
    flush_headers,
    html_start,
    mysql_fetch_row,
    mysql_num_rows,
    mysql_query,
    session_destroy,
    session_get,
    session_isset,
    session_save,
    session_start,
)

# ensure config connection
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "config"))
import config as _config  # noqa: F401,E402

session_start()
flush_headers()
if session_isset("username"):
    html_start()
    echo("""<html>
	<head>

<h3>Who is Online</h3>

	</head>
<p>

""")
    currentuser = session_get("username")
    result = mysql_query(
        f"select username,datetime from onlineuser where username != '{currentuser}'"
    )
    if mysql_num_rows(result) > 0:
        while True:
            row = mysql_fetch_row(result)
            if not row:
                break
            echo("""
<img src="../images/onlineicon.gif ">


""")
            echo(f"<label id='onlinelbl' name='onlinelbl' value='{row[0]}'>{row[0]}</label>")
            echo("<br>")
    echo("""
</p>
</html>
""")
else:
    session_destroy()
    html_start()
    echo("""      <a href="javascript:ajax('../login/index.py','login')"> </a>

""")
session_save()
