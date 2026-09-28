#!/usr/bin/env python3
"""Intellichat string helpers."""
from __future__ import annotations

import sys
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(_ROOT))
from _cgiutil import (  # noqa: E402
    echo, end_headers, flush_headers, get, header, html_start,
    mysql_connect, mysql_fetch_row, mysql_num_rows, mysql_query,
    mysql_real_escape_string, mysql_select_db, post, request,
    session_destroy, session_get, session_isset, session_save,
    session_set, session_start, session_unset,
)


def utf8_to_tis620(string: str) -> str:
    """Best-effort port of legacy UTF-8 to TIS-620 mapper."""
    try:
        return string.encode("utf-8").decode("tis-620", errors="replace")
    except Exception:
        return string

def tis620_to_utf8(string: str) -> str:
    try:
        return string.encode("tis-620", errors="replace").decode("utf-8", errors="replace")
    except Exception:
        return string
