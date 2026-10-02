#!/usr/bin/env python3
"""Post question form."""
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
echo("""<html>
<head><title>Post Questions</title></head>
<body>
<form name="postquestion" method="post" action= "post_questiondb.py">
<table id="postquestion" class="postquestion">
<tr><td>Select The Subject </td>
<td>
	
	</td></tr>
	<tr><td>Level</td><td><select name ="level">
	<option value="1">simple </option>
	<option value="2">medium </option>
	<option value="3">hard </option>
</select></td></tr>
<tr><td>Question</td><td><textarea name="q" cols="50" rows="2"></textarea></td></tr>
<tr><td>A</td><td><textarea name="a" cols="50" rows="1.5"></textarea></td></tr>
<tr><td>B</td><td><textarea name="b" cols="50" rows="1.5"></textarea></td></tr>
<tr><td>C</td><td><textarea name="c" cols="50" rows="1.5"></textarea></td></tr>
<tr><td>D</td><td><textarea name="d" cols="50" rows="1.5"></textarea></td></tr>
<tr><td>Select Answer</td><td><select name ="ans">
	<option value="1">A </option>
	<option value="2">B </option>
	<option value="3">C </option>
	<option value="4">D </option>
</select></td></tr>
<tr><td height="10px"></td></tr>
<tr><td></td><td align="center"><input type="submit" name="submit" id="submit" value="ADD"/></td></tr>
</table>
</form>
</body>
</html>


""")
