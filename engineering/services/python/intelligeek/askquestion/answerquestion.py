#!/usr/bin/env python3
"""Answer form for a FAQ id."""
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
faqid = get("questionno")
result = mysql_query(f"select question from faq where faqid='{faqid}'")
row = mysql_fetch_row(result)
qtext = row[0] if row else ""
echo(f"<html><body><h3>{qtext}</h3>")
echo(f"""<form method="post" action="answerdb.py">
<input type="hidden" name="faqid" value="{faqid}">
<textarea name="answer"></textarea>
<input name="emailidans" placeholder="email">
<input type="submit" value="Submit">
</form></body></html>""")
