#!/usr/bin/env python3
"""Gmail contact import stub (2006 flow no longer operable)."""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[3]))
from _cgiutil import echo, flush_headers, html_start  # noqa: E402

flush_headers()
html_start()
echo(
    "gmail contact import is not operable under modern auth; "
    "see historical PHP in git history"
)
