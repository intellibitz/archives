#https://help.ubuntu.com/stable/ubuntu-help/index.html
#
su - #root
sudo su - #command prompt with persistent super user privileges
sudo su
sudo -i #root

service --status-all
service --status-all | grep '\[ + \]'
service --status-all | grep '\[ - \]'
service <service-name> start
service <service-name> stop
service <service-name> restart
service <service-name> status
ls /etc/init.d

systemctl --type service --all
systemctl list-unit-files
systemctl list-unit-files --type service -all
systemctl --type service
systemctl --type service --state running
sudo systemctl | grep running
systemctl --type service --all --state exited

systemctl start service-name
systemctl stop service-name
systemctl restart service-name
systemctl status service-name

lshw | less #system details
lscpu
lspci
lsblk

timedatectl
date 
time 
ls -l /etc/localtime
timedatectl list-timezones
sudo timedatectl set-timezone Asia/Kolkata

#Run any one of the following command on Linux to see open ports:
sudo lsof -i -P -n | grep LISTEN
sudo netstat -tulpn | grep LISTEN
sudo ss -tulpn | grep LISTEN
sudo lsof -i:22 ## see a specific port such as 22 ##
sudo nmap -sTU -O IP-address-Here
ss -tulw

#Get the Monitor’s device name
xrandr | grep " connected" | cut -f1 -d " "
#Change the brightness level
xrandr --output [monitor-name] --brightness [brightness-level]
xrandr --output LVDS-1 --brightness 0.75

#keyring
libsecret
secret-tool
seahorse

#keys
ssh
gpg
access-tokens


nano /etc/passwd
upower -i $(upower -e | grep 'BAT') | grep -E "state|tofull|percentage"
ls /dev/sd*
ls /dev/nvme*
sudo dmesg
dd if=/path/to/iso/<image name>.iso of=/dev/sdb bs=512k #writes ISO image to usb

GRUB_CMDLINE_LINUX="" #/etc/default/grub (change boot screen settings)
/etc/gdm3/PostSession/Default #gnome session logout script

cat /etc/os-release
cat /etc/issue
cat /etc/shells

