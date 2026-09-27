#!/usr/bin/bash
#adb exec-out run-as package.name cat databases/file > file
#adb -d shell "run-as com.intellibitz cat databases/intellibitz.db" > /tmp/intellibitz.db.sqlite
#adb pull /tmp/intellibitz.db.sqlite /tmp/intellibitz.db.sqlite

#adb -d shell "run-as com.intellibitz cat databases/intellibitz.db" > /tmp/intellibitz.db.sqlite

adb -d exec-out run-as com.intellibitz cat databases/intellibitz.db > /tmp/intellibitz.db.sqlite
adb -d shell "run-as com.intellibitz cat shared_prefs/com.intellibitz_preferences.xml" > /tmp/com.intellibitz_preferences.xml
