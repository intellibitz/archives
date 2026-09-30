#!/usr/bin/env python3
"""Intellichat entry / cookie gate."""
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


from lib import config_inc
import os
flush_headers()
html_start()
cookies = os.environ.get("HTTP_COOKIE", "")
if "login=" in cookies:
    echo("""<script>location.href='mess.py'</script>""")
else:
    echo("""<script>location.href='login.py'</script>""")
