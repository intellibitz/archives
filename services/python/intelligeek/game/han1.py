#!/usr/bin/env python3
"""Hangman mini-game (han1)."""
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


import random
from _cgiutil import session_set
session_start()
flush_headers()
html_start()
words = ["PYTHON", "HANGMAN", "INTELLIGEEK", "ARCHIVE"]
if not session_get("hang_word"):
    session_set("hang_word", random.choice(words))
    session_set("hang_guessed", "")
word = session_get("hang_word") or "PYTHON"
guessed = session_get("hang_guessed") or ""
letter = (get("letter") or post("letter") or "").upper()
if letter and letter not in guessed:
    guessed += letter
    session_set("hang_guessed", guessed)
display = " ".join(ch if ch in guessed else "_" for ch in word)
echo("""<HTML>
<HEAD><TITLE>Hangman</TITLE></HEAD>
<BODY><DIV ALIGN = 'center'>
""")
echo("<h2>" + display + "</h2>")
echo("<p>Guess a letter:</p><form method='get'>")
for ch in "ABCDEFGHIJKLMNOPQRSTUVWXYZ":
    echo("<input type='submit' name='letter' value='" + ch + "'>")
echo("</form></DIV></BODY></HTML>")
session_save()
