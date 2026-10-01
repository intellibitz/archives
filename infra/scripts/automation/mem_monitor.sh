#!/bin/bash
while true; do
    clear
    echo "--- MEMORY & SWAP REALTIME ---"
    free -h
    echo ""
    echo "--- ZRAM STATUS ---"
    zramctl
    echo ""
    echo "--- TOP MEMORY CONSUMERS ---"
    ps aux --sort=-%mem | head -n 10 | awk "{print \$4, \$11}"
    sleep 2
done
