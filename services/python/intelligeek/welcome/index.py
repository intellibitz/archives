#!/usr/bin/env python3
"""Intelligeek welcome / play station page."""
from __future__ import annotations

import os
import runpy
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from _cgiutil import echo, flush_headers, html_start, include_text, session_save, session_start  # noqa: E402

errorMessage = ""


def _read_fragment(rel: str) -> str:
    return include_text(ROOT / rel)


def _run_cgi(rel: str) -> str:
    script = ROOT / rel
    proc = subprocess.run(
        [sys.executable, str(script)],
        env=os.environ.copy(),
        capture_output=True,
        text=True,
        errors="replace",
    )
    out = proc.stdout or ""
    if "\r\n\r\n" in out:
        _, out = out.split("\r\n\r\n", 1)
    elif "\n\n" in out:
        _, out = out.split("\n\n", 1)
    return out


if __name__ == "__main__":
    session_start()
    flush_headers()
    html_start()
    echo(
        """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<link rel="alternate" type="application/rss+xml" title="Latest Itelligeek News" href="../rssfeed/index.xml">
<link rel="shortcut icon" href="../images/favicon.ico" />
<head>
"""
    )
    echo(_read_fragment("include/head_script"))
    echo(
        """
</head>

<body>


<div id="header">
<h2 class="header_txt">Learning Made Fun</h2>

</div>


<div id="navcontainer">"""
    )
    echo(_read_fragment("include/top_menu"))
    echo(
        """
</div>

<div id="left">
 <div id="login"></div>
 <label id="msg" class="msg" />"""
    )
    if errorMessage:
        echo(
            f'<p><strong><font color="#990000">{errorMessage}</font></strong></p>'
        )
    echo(
        """
 <!--star_of_the_day include disabled in legacy markup-->
 """
    )
    echo(_run_cgi("newsletter/index.py"))
    echo(
        """

</div>



<div id="right">

	<div id="score"></div>

	"""
    )
    echo(_run_cgi("include/chat_box.py"))
    echo(_run_cgi("include/who_is_online.py"))
    echo(
        """
</div>

<div id="content" class="content">
<h3>Play Station</h3>

<!--<p id="play_station">The default text to display is how to play the game and more..</p>-->
<p id="play_station">
    """
    )
    echo(_read_fragment("include/how_to_play_more"))
    echo(
        """
</p>
</div>



<div id="footer"> """
    )
    footer_path = ROOT / "include" / "footer"
    if footer_path.exists():
        echo(_read_fragment("include/footer"))
    echo(
        """
</div>

<script  type="text/javascript" src="../js/ajax_xmlhttp.js"></script>
<script  type="text/javascript" src="../js/datetimepicker.js"></script>
<script  type="text/javascript" src="../js/registervalid.js"></script>
<script  type = "text/javascript" src="../js/checkavailablity.js"></script>
<script type="text/javascript"  src="../js/intellibitz.js"></script>
<script type="text/javascript"  src="../js/questionpost.js"></script>
<script  type = "text/javascript" src="../js/login.js"></script>
<script  type = "text/javascript" src="../js/loginvalid.js"></script>
<script type="text/javascript" src="../js/cpswd.js"></script>
<script type="text/javascript" src="../js/fpswdvalid.js"></script>

</body>
</html>
"""
    )
    runpy.run_path(str(ROOT / "login" / "uploaddb.py"), run_name="__main__")
    session_save()
