#!/usr/bin/env python3
"""IntelliChat login."""
from __future__ import annotations

import os
import re
import sys
import time
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(_ROOT))
sys.path.insert(0, str(Path(__file__).resolve().parent))
from _cgiutil import (  # noqa: E402
    echo, flush_headers, html_start, mysql_connect, mysql_fetch_array,
    mysql_num_rows, mysql_query, mysql_real_escape_string, mysql_select_db,
    post, session_save, session_start, header, end_headers,
)
from lib import config_inc as cfg  # noqa: E402

session_start()
flush_headers()
mysql_connect(cfg.host, cfg.user, cfg.pass_)
mysql_select_db(cfg.database)
cookie_login = ""
cookie_ip = ""
raw = os.environ.get("HTTP_COOKIE", "")
for part in raw.split(";"):
    part = part.strip()
    if part.startswith("login="):
        cookie_login = part.split("=", 1)[1]
    if part.startswith("ip="):
        cookie_ip = part.split("=", 1)[1]
try:
    mysql_query(
        "DELETE FROM chat_online WHERE (name='%s' and ip='%s') OR (unix_timestamp() - utimestmp > 300)"
        % (mysql_real_escape_string(cookie_login), mysql_real_escape_string(cookie_ip))
    )
except Exception as e:
    html_start()
    echo(f"<font size=2><strong>Error</strong> : {e} — try <a href='install.py'>install.py</a></font>")
    raise SystemExit()

statustext = ""
if post("name"):
    name = post("name")
    if re.match(r"^[-a-zA-Z0-9. %$&^*+=.?]+$", name):
        name_e = mysql_real_escape_string(name)
        result = mysql_query(f"SELECT * FROM chat_online WHERE name='{name_e}'")
        if mysql_num_rows(result) > 0:
            statustext = "<strong>Error</strong> : Found This Name In The Room Already."
        else:
            ip = os.environ.get("REMOTE_ADDR", "127.0.0.1")
            t = int(time.time())
            mysql_query(
                f"INSERT INTO chat_online (no,name,ip,utimestmp,avatar) VALUES (0,'{name_e}','{ip}',{t},'001')"
            )
            header(f"Set-Cookie: login={name_e}; Path=/")
            header(f"Set-Cookie: ip={ip}; Path=/")
            html_start()
            echo("<script>location.href='mess.py'</script>")
            session_save()
            raise SystemExit()
    else:
        statustext = "<strong>Error</strong> : Invalid name."

html_start()
echo(f"""<html><head><title>{cfg.title} Login</title>
<link href="style.css" rel="stylesheet" type="text/css">
</head><body>
{statustext}
<form method="post">
Name: <input name="name">
<input type="submit" value="Enter">
</form>
</body></html>""")
session_save()
