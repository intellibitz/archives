#!/usr/bin/env python3
"""About page."""
from __future__ import annotations

import sys
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(_ROOT))
from _cgiutil import (  # noqa: E402
    echo, end_headers, flush_headers, get, header, html_start,
    mysql_connect, mysql_fetch_row, mysql_num_rows, mysql_query,
    mysql_real_escape_string, mysql_select_db, post, request,
    session_destroy, session_get, session_isset, session_save,
    session_set, session_start, session_unset,
)


flush_headers()
html_start()
echo("""<html>
<head>
<title>About Us</title>
<meta http-equiv="Content-Type" content="text/html; charset=windows-874">
</head>

<body topmargin="0" leftmargin="0">
<table width="300" height="200" border="1" cellpadding="0" cellspacing="0" bordercolor="#1F5989" bgcolor="#2B7BBD">
  <tr>
    <td align="center" valign="middle">
        <table width="280" height="180" border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td valign="top"><p><font size="2" face="MS Sans Serif, Tahoma, sans-serif"><strong><font color="#FFFFFF" size="1">About
                      Us</font></strong></font></p>
              <p align="justify"><font color="#FFFFFF" size="1" face="MS Sans Serif, Tahoma, sans-serif">MessChat
                   is a Property of Intellibitz.com. You
                  can upgrade or change this chat to your style but do not remove
                  our
                  brand and links to my website for our next version of MessChat <br>
                  <font color="#FFCCCD"><br>
                  </font></font><font color="#FFCCCD" size="1" face="MS Sans Serif, Tahoma, sans-serif">
                  <font color="#FFFFFF" size="1" face="MS Sans Serif, Tahoma, sans-serif"><strong><br>
                    <br>
                    Creator<br>
</strong>Mr.Theerasak Phuetthanyakij <strong>              <br>
              </strong></font></p>
            </td>
          </tr>
        </table>
</td>
  </tr>
</table>
</body>
</html>
""")
