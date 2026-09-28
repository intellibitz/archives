#!/usr/bin/env python3
"""Chatr login page"""
from __future__ import annotations
import sys
from pathlib import Path
_HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(_HERE.parents[2]))
from _cgiutil import echo, flush_headers, html_start
flush_headers()
html_start()
echo("""<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Chatr Login - AJAX/PHP Chat</title>
</head>

<body>
<p>Enter a name and click Login to chat. Visit <a href="http://www.sterryit.com/chatr/">Chatr</a> for updates or to get your own.</p>
<p>  
 
</p>
<form name="form1" method="post" action="login.py">
  <table width="397" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td width="229">username:</td>
      <td width="168"><input type="text" name="u"></td>
    </tr>
    <tr>
      <td>password (admin only): </td>
      <td><input name="p" type="password" id="p"></td>
    </tr>
  </table>
  <p>
	<input type="submit" name="Submit" value="Login">
  </p>
</form>

</body>
</html>
""")
