#!/bin/bash
if [ -n "$1" ]; then
	pkill -f "$1"
#	pgrep -f "$1" | xargs kill -9
#	ps aux | grep -ie "$1" | grep -v grep | awk '{print $2}' | xargs kill -9
fi
