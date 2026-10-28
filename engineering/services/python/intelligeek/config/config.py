"""Database connection (legacy mysql_* replacement)."""
from __future__ import annotations

import os
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from _cgiutil import mysql_connect, mysql_select_db, mysql_error  # noqa: E402

HOST = os.environ.get("DB_HOST", "127.0.0.1")
USER = os.environ.get("DB_USER", "geek")
PASSWORD = os.environ.get("DB_PASSWORD", "")
DATABASE = os.environ.get("DB_NAME", "geek")

connect = mysql_connect(HOST, USER, PASSWORD)
mysql_select_db(DATABASE, connect)
