#!/usr/bin/env python3
"""Handle image upload."""
from __future__ import annotations

import sys
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[2]
_HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(_ROOT))
from _cgiutil import echo, files, flush_headers, html_start, post  # noqa: E402

flush_headers()
f = files("image")
if f is not None:
    oname = getattr(f, "filename", None) or "upload.bin"
    dest = _HERE / "upload"
    dest.mkdir(exist_ok=True)
    data = f.file.read() if hasattr(f, "file") else b""
    (dest / oname).write_bytes(data if isinstance(data, (bytes, bytearray)) else bytes(data))
    imgnum = int(post("imgnum") or 0)
    html_start()
    echo(
        "<html><head><script>\n"
        "\tvar par = window.parent.document;\n"
        "\tvar images = par.getElementById('images');\n"
        f"\tvar imgdiv = images.getElementsByTagName('div')[{imgnum}];\n"
        "\tvar image = imgdiv.getElementsByTagName('img')[0];\n"
        "\timgdiv.removeChild(image);\n"
        "\tvar image_new = par.createElement('img');\n"
        f"\timage_new.src = 'resize.py?pic={oname}';\n"
        "\timage_new.className = 'loaded';\n"
        "\timgdiv.appendChild(image_new);\n"
        "</script></head></html>"
    )
    raise SystemExit()
html_start()
echo(
    '<html><head></head><body><form method="post" enctype="multipart/form-data">'
    '<input type="file" name="image"><input type="submit"></form></body></html>'
)
