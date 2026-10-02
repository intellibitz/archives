#!/usr/bin/env python3
"""mobeegal.in landing page — includes static HTML fragments."""
from __future__ import annotations

from pathlib import Path

BASE = Path(__file__).resolve().parent


def _read(rel: str) -> str:
    return (BASE / rel).read_text(encoding="utf-8", errors="replace")


def main() -> None:
    print("Content-Type: text/html")
    print()
    print("""<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>""" + _read("include/meta.html") + """</head>
<body>

<div id="header">
            """ + _read("include/header.html") + """
</div>
<div class="colmask leftmenu">
	<div class="colleft">
        <div class="col1">
                    <div id="left_panel">
                        """ + _read("content/home.html") + """
                    </div>
        </div>
        <div class="col2">
                    <div id="right_panel">
                        """ + _read("content/announce.html") + """
                    </div>
        </div>

	</div>

</div>
<div id="footer">
            """ + _read("include/footer.html") + """
</div>

</body>
</html>""")


if __name__ == "__main__":
    main()
