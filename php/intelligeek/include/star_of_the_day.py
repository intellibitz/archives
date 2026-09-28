#!/usr/bin/env python3
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from _cgiutil import echo, html_start  # noqa: E402

html_start()
echo("""<h3>Star of the Day</h3>

<p>
""")
echo("test")
echo("""
</p>
""")
