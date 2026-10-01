"""Database connection (legacy mysql_* replacement)."""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from _cgiutil import mysql_connect, mysql_select_db, mysql_error  # noqa: E402

HOST = "192.168.1.6"
USER = "geek"
PASSWORD = "geek"
DATABASE = "geek"

connect = mysql_connect(HOST, USER, PASSWORD)
mysql_select_db(DATABASE, connect)
