#!/usr/bin/env python3
"""Newsletter signup widget (legacy newsletter/index.php)."""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from _cgiutil import echo, html_start  # noqa: E402

html_start()
echo(
    """<html>
<head>
<script type="text/javascript" language="javascript" src="../js/newsletter.js"></script>
</head>
</html>
<h3>Newsletter Signup</h3>
<p>
<span id="message_1"></span>&nbsp;&nbsp;
<input class="field" type="text" onclick="clear_text_box('email_for_new_letter')" value="your e-mail" name="email_for_news_letter" id="email_for_new_letter" >
&nbsp;&nbsp;&nbsp;&nbsp;
<img src="../images/bullet2.jpg" alt="submit" name="subscribe_news_letter" onclick="email_validation_submit('email_for_new_letter','message_1');clear_text_box('email_for_new_letter')">
</p>
<br>
"""
)
