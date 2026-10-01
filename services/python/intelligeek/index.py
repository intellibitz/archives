#!/usr/bin/env python3
"""Front controller: redirect to welcome."""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from _cgiutil import redirect  # noqa: E402

redirect("welcome")
