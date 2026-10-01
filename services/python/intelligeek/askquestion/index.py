#!/usr/bin/env python3
"""FAQ list."""
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
echo("""<html>
	<head>
		<h3>FAQ</h3>
	</head>
<body>
""")
result = mysql_query("select faqid, question, answer from faq")
while True:
    row = mysql_fetch_row(result)
    if not row:
        break
    faqid, question, answer = row[0], row[1], row[2] if len(row) > 2 else ""
    echo(f"<p><b>{question}</b><br>{answer}<br>")
    echo(f"<a href='answerquestion.py?questionno={faqid}'>Answer</a></p>")
echo("</body></html>")
