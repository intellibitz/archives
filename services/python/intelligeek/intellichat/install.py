#!/usr/bin/env python3
"""Create IntelliChat tables."""
from __future__ import annotations

import sys
from pathlib import Path

_ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(_ROOT))
sys.path.insert(0, str(Path(__file__).resolve().parent))
from _cgiutil import (  # noqa: E402
    echo, flush_headers, html_start, mysql_connect, mysql_query, mysql_select_db,
)
from lib import config_inc as cfg  # noqa: E402

flush_headers()
html_start()
echo(f"<html><head><title>Install FirstTime Of {cfg.title}</title></head><body>")
mysql_connect(cfg.host, cfg.user, cfg.pass_)
mysql_select_db(cfg.database)
install = [
    """CREATE TABLE IF NOT EXISTS `chat_group` (
  `id` int(11) NOT NULL auto_increment,
  `groupname` text,
  `grouppass` text,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB""",
    """CREATE TABLE IF NOT EXISTS `chat_online` (
  `no` int(11) default '0',
  `name` varchar(30) NOT NULL default '',
  `ip` varchar(20) NOT NULL default '',
  `utimestmp` int(11) default NULL,
  `avatar` char(3) NOT NULL default '',
  `mystatus` varchar(30) default 'online',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB""",
    """CREATE TABLE IF NOT EXISTS `chat_text` (
  `no` int(11) NOT NULL auto_increment,
  `name` varchar(30) default NULL,
  `text` text,
  `color` varchar(20) default NULL,
  `time` varchar(30) default NULL,
  `timestamp` int(11) default NULL,
  `sendto` varchar(30) default NULL,
  `togroup` varchar(30) default NULL,
  PRIMARY KEY  (`no`)
) ENGINE=InnoDB""",
]
for sql in install:
    try:
        mysql_query(sql)
        echo("<p>OK</p>")
    except Exception as e:
        echo(f"<p>Error: {e}</p>")
echo("<p>Done. <a href='login.py'>Login</a></p></body></html>")
