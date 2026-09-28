#!/usr/bin/env python3
"""Message history (last hour)."""
from __future__ import annotations

import sys
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(_ROOT))
sys.path.insert(0, str(Path(__file__).resolve().parent))
from _cgiutil import (  # noqa: E402
    echo, flush_headers, html_start, mysql_connect, mysql_fetch_array,
    mysql_num_rows, mysql_query, mysql_select_db, session_start, session_save,
)
from lib import config_inc as cfg  # noqa: E402

session_start()
flush_headers()
html_start()
mysql_connect(cfg.host, cfg.user, cfg.pass_)
mysql_select_db(cfg.database)
mysql_query("DELETE FROM chat_text WHERE unix_timestamp()-timestamp > 3600")
result = mysql_query("SELECT * FROM chat_text ORDER BY no ASC")
echo(f"""<html>
<head>
<title>{cfg.title}</title>
<meta http-equiv="Content-Type" content="text/html; charset=windows-874">
<link href="style.css" rel="stylesheet" type="text/css">
</head>
<body bgcolor="#C7DFF3" topmargin="0" leftmargin="0">
<center><font size="2"><u><strong>Message History ( Last 1 Hour  )</strong></u></font></center><br>
<table width="590" border="0" cellspacing="0" cellpadding="0">
""")
i = 0
# drain rows
n = mysql_num_rows(result)
for _ in range(n):
    arr = mysql_fetch_array(result)
    if not arr:
        break
    # support tuple rows: approximate columns by index if needed
    if isinstance(arr, dict):
        sendto = arr.get("sendto") or ""
        togroup = arr.get("togroup") or ""
        t = arr.get("time", "")
        name = arr.get("name", "")
        color = arr.get("color", "#000")
        text = arr.get("text", "")
    else:
        # unknown order — print raw
        sendto = togroup = ""
        t = name = color = text = str(arr)
    if sendto == "" and togroup == "":
        i += 1
        bg = "#ADD0ED" if i % 2 == 0 else "#C7DFF3"
        echo(f'<tr><td width="590" height="15" bgcolor="{bg}"><font size="1">')
        echo(f"[{t}] <strong>{name}</strong> : <font color='{color}'>{text}</font></font></td></tr>")
echo("</table></body></html>")
session_save()
