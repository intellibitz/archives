#!/usr/bin/expect
set timeout -1
#exp_internal 1
if { $argc < 2 } {
    puts "Usage: $argv0 user pass"
    exit 1
}
set user [lindex $argv 0]
set pass [lindex $argv 1]
set file [lindex $argv 2]
puts "$user : $pass : $file"
spawn sudo
expect "*sudo*:"
interact ".*\r" return
spawn /usr/sbin/openvpn $file
expect "*sername:"
send "$user\r"
expect "*assword:"
send "$pass\r"
interact
