#!/usr/bin/env python3
"""Newsletter body fragment (legacy news_content.php)."""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from _cgiutil import echo, html_start  # noqa: E402

html_start()
echo(
    """Hello 

H r U?
aalsdfa
adsfaslfa
sadflkadslf
adsfadsf"""
)
