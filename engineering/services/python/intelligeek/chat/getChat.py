#!/usr/bin/env python3
"""JSON AJAX chat backend (legacy getChat.py)."""
from __future__ import annotations

import html
import sys
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from _cgiutil import (  # noqa: E402
    echo,
    flush_headers,
    get,
    post,
    session_get,
    session_start,
    session_save,
)

sys.path.insert(0, str(Path(__file__).resolve().parent))
import database  # noqa: E402

database.db_connect()
session_start()
user_name = session_get("username") or ""

flush_headers(
    [
        "Expires: Mon, 26 Jul 1997 05:00:00 GMT",
        "Last-Modified: " + datetime.utcnow().strftime("%a, %d %b %Y %H:%M:%S") + "GMT",
        "Cache-Control: no-cache, must-revalidate",
        "Pragma: no-cache",
        "Content-Type: text/plain; charset=utf-8",
    ]
)

message = post("message")
if message:
    chat_id = database.db_input(get("chat"))
    sql = (
        "INSERT INTO message(chat_id, user_id, user_name, message, post_time) VALUES ("
        + chat_id
        + ", 1, '"
        + database.db_input(user_name)
        + "', '"
        + database.db_input(message)
        + "', NOW())"
    )
    database.db_query(sql)

if post("action") == "reset":
    sql = "DELETE FROM message WHERE chat_id = " + database.db_input(get("chat"))
    database.db_query(sql)

json = '{"messages": {'
chat = get("chat")
if not chat:
    json += '"message":[ {'
    json += (
        '"id":  "0",'
        '"user": "Admin",'
        '"text": "You are not currently in a chat session.  &lt;a href=&quot;&quot;&gt;Enter a chat session here&lt;/a&gt;",'
        f'"time": "{datetime.now().strftime("%I:%M").lstrip("0")}"'
        "}]"
    )
else:
    last = get("last") or "0"
    sql = (
        "SELECT message_id, user_name, message, date_format(post_time, '%h:%i') as post_time"
        " FROM message WHERE chat_id = "
        + database.db_input(chat)
        + " AND message_id > "
        + database.db_input(last)
    )
    message_query = database.db_query(sql)
    if database.db_num_rows(message_query) > 0:
        json += '"message":[ '
        while True:
            message_array = database.db_fetch_array(message_query)
            if not message_array:
                break
            json += "{"
            json += (
                f'"id":  "{message_array["message_id"]}",'
                f'"user": "{html.escape(str(message_array["user_name"]))}",'
                f'"text": "{html.escape(str(message_array["message"]))}",'
                f'"time": "{message_array["post_time"]}"'
                "},"
            )
        json += "]"
    else:
        json += '"message":[]'
json += "}}"
echo(json)
session_save()
