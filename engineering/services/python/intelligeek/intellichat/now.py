#!/usr/bin/env python3
"""Socket time probe (legacy)."""
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


import socket
flush_headers()
html_start()
host = "203.146.140.216"
port = 8010
try:
    s = socket.create_connection((host, port), timeout=3)
    data = s.recv(1024)
    s.close()
    echo(data.decode("utf-8", errors="replace"))
except Exception as e:
    echo(str(e))
