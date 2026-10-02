#!/usr/bin/env python3
"""IntelliChat AJAX data endpoint (condensed faithful port of Data.php)."""
from __future__ import annotations

import os
import sys
import time
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(_ROOT))
sys.path.insert(0, str(Path(__file__).resolve().parent))
from _cgiutil import (  # noqa: E402
    echo, flush_headers, get, html_start, mysql_connect, mysql_fetch_array,
    mysql_num_rows, mysql_query, mysql_real_escape_string, mysql_select_db,
    post, session_start, session_save,
)
from lib import config_inc as cfg  # noqa: E402
from lib import func as _func  # noqa: F401,E402

session_start()
flush_headers()
html_start()
mysql_connect(cfg.host, cfg.user, cfg.pass_)
mysql_select_db(cfg.database)

action = get("action") or post("action") or "messages"
login = ""
raw = os.environ.get("HTTP_COOKIE", "")
for part in raw.split(";"):
    part = part.strip()
    if part.startswith("login="):
        login = part.split("=", 1)[1]

# keepalive
if login:
    mysql_query(
        "UPDATE chat_online SET utimestmp=%d WHERE name='%s'"
        % (int(time.time()), mysql_real_escape_string(login))
    )
    mysql_query("DELETE FROM chat_online WHERE unix_timestamp() - utimestmp > 300")

if action in ("messages", "get", ""):
    result = mysql_query("SELECT name,text,color,time FROM chat_text ORDER BY no DESC LIMIT 30")
    n = mysql_num_rows(result)
    for _ in range(n):
        row = mysql_fetch_array(result)
        if not row:
            break
        if isinstance(row, dict):
            echo(f"[{row.get('time')}] {row.get('name')}: {row.get('text')}<br>")
        else:
            echo(str(row) + "<br>")
elif action == "online":
    result = mysql_query("SELECT name,mystatus FROM chat_online")
    n = mysql_num_rows(result)
    for _ in range(n):
        row = mysql_fetch_array(result)
        if not row:
            break
        echo(str(row) + "<br>")
elif action == "post" and post("text") and login:
    text = mysql_real_escape_string(post("text"))
    color = mysql_real_escape_string(post("color") or "#000")
    tstr = time.strftime("%H:%M:%S")
    mysql_query(
        "INSERT INTO chat_text (name,text,color,time,timestamp) VALUES ('%s','%s','%s','%s',%d)"
        % (mysql_real_escape_string(login), text, color, tstr, int(time.time()))
    )
    echo("OK")
else:
    echo("OK")
session_save()
