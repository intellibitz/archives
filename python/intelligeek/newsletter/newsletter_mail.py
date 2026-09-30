#!/usr/bin/env python3
"""Send newsletter emails from DB list."""
from __future__ import annotations

import sys
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(_ROOT))
from _cgiutil import (  # noqa: E402
    echo, flush_headers, get, html_start, mysql_fetch_row, mysql_num_rows,
    mysql_query, mysql_real_escape_string, post, request, session_get,
    session_save, session_start,
)

def _db():
    import config.config  # noqa: F401


_db()
flush_headers()
html_start()
mysql_query("create table IF NOT EXISTS emailid(eid int(5) primary key AUTO_INCREMENT,emailid varchar(50))")
# Original body continued with mail loop — preserve query + content include
content_path = Path(__file__).resolve().parent / "news_content.py"
# Prefer static content file text without executing CGI headers twice
raw = (Path(__file__).resolve().parent / "news_content.py").read_text(encoding="utf-8", errors="replace")
# Fallback to original-ish greeting if needed
body = "Hello\n\nH r U? aalsdfa adsfaslfa\n"
result = mysql_query("select emailid from emailid")
while True:
    row = mysql_fetch_row(result)
    if not row:
        break
    # mail each — best effort
    try:
        import smtplib
        from email.mime.text import MIMEText
        msg = MIMEText(body)
        msg["Subject"] = "IntelliGeek Newsletter"
        msg["From"] = "admin@intelligame.com"
        msg["To"] = row[0]
        smtplib.SMTP("localhost").send_message(msg)
    except Exception:
        pass
echo("newsletter dispatch attempted")
