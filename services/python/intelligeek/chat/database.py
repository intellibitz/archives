"""Chat DB helpers (legacy database.php)."""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from _cgiutil import (  # noqa: E402
    mysql_fetch_row,
    mysql_num_rows,
    mysql_query,
    mysql_real_escape_string,
)

import config.config  # noqa: F401,E402 side-effect DB connect


def db_connect():
    return True


def db_input(string):
    return mysql_real_escape_string(string)


def db_query(query):
    return mysql_query(query)


def db_fetch_array(db_query):
    row = mysql_fetch_row(db_query)
    if row is None:
        return None
    if isinstance(row, dict):
        return row
    return {
        "message_id": row[0],
        "user_name": row[1],
        "message": row[2],
        "post_time": row[3],
    }


def db_num_rows(db_query):
    return mysql_num_rows(db_query)
