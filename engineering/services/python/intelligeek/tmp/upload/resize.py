#!/usr/bin/env python3
"""Resize uploaded JPEG (Pillow if available)."""
from __future__ import annotations

import sys
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[2]
_HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(_ROOT))
from _cgiutil import echo, end_headers, flush_headers, get, header, html_start  # noqa: E402

pic = get("pic")
flush_headers()
if not pic:
    html_start()
    echo("error")
    raise SystemExit()
path = _HERE / "upload" / pic
if not path.exists():
    html_start()
    echo("error")
    raise SystemExit()
try:
    from PIL import Image
    import io

    im = Image.open(path)
    im.thumbnail((100, 100))
    buf = io.BytesIO()
    im.convert("RGB").save(buf, format="JPEG")
    header("Content-Type: image/jpeg")
    end_headers()
    sys.stdout.buffer.write(buf.getvalue())
except ImportError:
    header("Content-Type: image/jpeg")
    end_headers()
    sys.stdout.buffer.write(path.read_bytes())
