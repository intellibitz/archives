#!/usr/bin/env python3
"""IntelliChat main room (simplified faithful port)."""
from __future__ import annotations

import os
import sys
import time
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(_ROOT))
sys.path.insert(0, str(Path(__file__).resolve().parent))
from _cgiutil import (  # noqa: E402
    echo, flush_headers, html_start, mysql_connect, mysql_fetch_array,
    mysql_num_rows, mysql_query, mysql_real_escape_string, mysql_select_db,
    post, session_save, session_start,
)
from lib import config_inc as cfg  # noqa: E402

session_start()
flush_headers()
mysql_connect(cfg.host, cfg.user, cfg.pass_)
mysql_select_db(cfg.database)

def _cookie(name: str) -> str:
    raw = os.environ.get("HTTP_COOKIE", "")
    for part in raw.split(";"):
        part = part.strip()
        if part.startswith(name + "="):
            return part.split("=", 1)[1]
    return ""

login = _cookie("login")
if not login:
    html_start()
    echo("<script>location.href='login.py'</script>")
    raise SystemExit()

if post("text"):
    text = mysql_real_escape_string(post("text"))
    color = mysql_real_escape_string(post("color") or "#000000")
    tstr = time.strftime("%H:%M:%S")
    ts = int(time.time())
    name_e = mysql_real_escape_string(login)
    mysql_query(
        f"INSERT INTO chat_text (name,text,color,time,timestamp) "
        f"VALUES ('{name_e}','{text}','{color}','{tstr}',{ts})"
    )

html_start()
echo(f"""<html><head><title>{cfg.title}</title>
<meta http-equiv="refresh" content="15">
<link href="style.css" rel="stylesheet" type="text/css">
</head><body>
<p>Logged in as <strong>{login}</strong> | <a href="history.py">History</a> | <a href="about.py">About</a></p>
<div id="messages">
""")
result = mysql_query("SELECT name,text,color,time FROM chat_text ORDER BY no DESC LIMIT 50")
n = mysql_num_rows(result)
for _ in range(n):
    row = mysql_fetch_array(result)
    if not row:
        break
    if isinstance(row, dict):
        echo(f"[{row.get('time')}] <b>{row.get('name')}</b>: "
             f"<font color='{row.get('color')}'>{row.get('text')}</font><br>")
    else:
        echo(f"{row}<br>")
echo("""</div>
<form method="post">
<input name="text" size="60">
<input type="color" name="color" value="#000000">
<input type="submit" value="Send">
</form>
</body></html>""")
session_save()
