#!/usr/bin/env python3
"""Import contacts entry."""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[2]))
from _cgiutil import echo, flush_headers, html_start, post  # noqa: E402

flush_headers()
html_start()
if not post("username"):
    echo("<script>location.href='form.py'</script>")
else:
    echo("<p>Import started for " + post("username") + "</p>")
    echo("<p>Configure scripts/gmail.py for provider-specific import.</p>")
