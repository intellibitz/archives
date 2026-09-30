#!/usr/bin/env python3
"""Quiz SMS/email form."""
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


flush_headers()
html_start()
echo(""" <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Test</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta http-equiv=refresh>
<SCRIPT LANGUAGE="JavaScript">
var born = new Date();

var hr=born.getHours();
//document.write(hr);
var time1=born.getMinutes();
document.write("hai");
if(hr==9)
{
document.write("Sending mail");
<!--
setTimeout('document.test.submit()',3000);
//-->
}
else
{
document.write("Sorry mail timed out");
}
</SCRIPT>
 </head>

<body>
<form name="test" id="form1" method="post" action="sendquiz.php">
  <p>
    <input name="pattern" type="text" id="pattern" />
    <input name="show" type="hidden" id="show" value="quickref" />
  </p>
  <p><input type="submit" name="next" value="Next" />
&nbsp;  </p>
</form>
</body>
</html>""")
