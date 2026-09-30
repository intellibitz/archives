#!/usr/bin/env python3
"""Chat room sidebar widget."""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from _cgiutil import echo, html_start  # noqa: E402

html_start()
echo("""<h3>Chat Room</h3>

<p>

<INPUT type="button" value="Click to Chat!" onClick="window.open('../chat/chat.py','mywindow','width=480px, height=480px, left=0,top=200,screenX=0,screenY=100')">

<INPUT type="button" value="Post a Query!" onClick="window.open('../include/postaquestion.php','mywindow','width=400,height=200,left=0,top=100,screenX=0,screenY=100')">



</p>
""")
