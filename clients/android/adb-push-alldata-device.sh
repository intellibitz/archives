#!/usr/bin/bash
#adb exec-out run-as package.name cat databases/file > file
#adb -d shell "run-as com.intellibitz cat databases/intellibitz.db" > /tmp/intellibitz.db.sqlite
#adb pull /tmp/intellibitz.db.sqlite /tmp/intellibitz.db.sqlite

#adb -d shell "run-as com.intellibitz cat databases/intellibitz.db" > /tmp/intellibitz.db.sqlite

#adb -d exec-out run-as com.intellibitz cat databases/intellibitz.db > /tmp/intellibitz.db.sqlite
adb -d  shell "run-as com.intellibitz chmod 777 databases/intellibitz.db"
adb -d  shell 'run-as com.intellibitz' push /tmp/intellibitz.db.sqlite  /data/data/com.intellibitz/databases/intellibitz.db