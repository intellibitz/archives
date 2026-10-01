#!/usr/bin/env python3
"""Chatr install form"""
from __future__ import annotations
import sys
from pathlib import Path
_HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(_HERE.parents[2]))
from _cgiutil import echo, flush_headers, html_start
flush_headers()
html_start()
echo("""<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Chatr Installation</title>
<style type="text/css">
<!--
.style1 {font-family: Arial, Helvetica, sans-serif}
-->
</style>
</head>
<body>



<form action="install2.py" method="post">
  <p class="style1">Welcome to Chatr Installation. <br>
    <br>
  This page will help you configure your installation with this one form. After this, you'll be open for chat. </p>
  <p class="style1">When entering your chat, the user may choose any nick except those based on the administrator's.</p>
  <table width="812" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td width="294" class="style1">Please enter the administrator nick: <br>
      <br></td>
      <td width="518" valign="top" class="style1"><input name="admin_nick" type="text" id="admin_nick"></td>
    </tr>
    <tr>
      <td class="style1">Choose a password: <br>
        <br>
      </td>
      <td class="style1"><input name="password" type="password" id="password">
      <br></td>
    </tr>
    <tr>
      <td class="style1">Enter the fully qualified path to your chat folder including the trailing slash:<br>
(example: http://www.sterryit.com/chat/)<br>
<br></td>
      <td valign="top" class="style1"><input name="path" type="text" id="path" value="http://" size="80"></td>
    </tr>
    <tr>
      <td class="style1">Notification email address. Sent when anyone enters your chatroom. Enter your cellphone email address for maximal effect. (optional) </td>
      <td valign="top" class="style1"><input name="email_address" type="text" id="email_address" value="none" size="50"></td>
    </tr>
  </table>
  <p class="style1">
    <input type="submit" name="Submit" value="Submit">
    <input type="reset" name="Reset" value="Reset">
</p>
</form>






</body>
</html>
""")
