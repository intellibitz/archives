#!/usr/bin/env python3
"""Drop timed-out Chatr users."""
from __future__ import annotations

import sys
import time
import urllib.request
from pathlib import Path

_HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(_HERE))
sys.path.insert(0, str(_HERE.parents[2]))
from _cgiutil import echo, flush_headers, html_start  # noqa: E402
import config as cfg  # noqa: E402

now = time.time()
pings_path = _HERE / cfg.pings_file
users_path = _HERE / cfg.users_file
pings = pings_path.read_text(encoding="utf-8", errors="replace").splitlines() if pings_path.exists() else []
userlines = users_path.read_text(encoding="utf-8", errors="replace").splitlines() if users_path.exists() else []
newuserlines: list[str] = []
drop = False
usertodrop = None
for line in pings:
    if not line.strip():
        continue
    parts = line.split(",")
    if len(parts) < 2:
        continue
    ping_uid, timestamp = parts[0], float(parts[1])
    for ul in userlines:
        ud = ul.split(",")
        if not ud or ud[0] != ping_uid:
            continue
        if timestamp >= now - float(cfg.timeout):
            newuserlines.append(ul)
        else:
            usertodrop = ud[1] if len(ud) > 1 else ping_uid
            drop = True
if drop:
    users_path.write_text("\n".join(newuserlines) + ("\n" if newuserlines else ""), encoding="utf-8")
    try:
        urllib.request.urlopen(
            f"{cfg.path_to_chat}posttext.py?u={cfg.admin_id}&t={usertodrop} timed out",
            timeout=2,
        )
    except Exception:
        pass

if __name__ == "__main__":
    flush_headers()
    html_start()
    echo("timeout checked")
