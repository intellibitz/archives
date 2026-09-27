#!/usr/bin/expect
set timeout -1
#exp_internal 1
if { $argc != 4 } {
    puts "Usage: $argv0 host user pass command"
    exit 1
}
set host [lindex $argv 0]
set user [lindex $argv 1]
set pass [lindex $argv 2]
set command [lindex $argv 3]
spawn sftp -oStrictHostKeyChecking=no -oCheckHostIP=no $user@$host
expect "*assword:"
send "$pass\r"
expect "sftp>"
send "$command\r"
expect "sftp>"
send "exit\r"
expect eof