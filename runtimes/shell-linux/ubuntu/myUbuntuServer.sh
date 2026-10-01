#https://ubuntu.com/server/docs
#

sudo -i #root
sudo su

service --status-all
service --status-all | grep '\[ + \]'
service --status-all | grep '\[ - \]'
# service <service-name> start
# service <service-name> stop
# service <service-name> restart
# service <service-name> status
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

ip a
ip addr show
hostname -I
uname -a
uptime
lsb_release -a
ifconfig
arp -a
curl ifconfig.me #prints public ip

sudo apt-get install openssh-server
sudo cp /etc/ssh/sshd_config /etc/ssh/sshd_config.factory-defaults
sudo chmod a-w /etc/ssh/sshd_config.factory-defaults
# sudo gedit /etc/ssh/sshd_config
sudo systemctl enable ssh
sudo systemctl start ssh
sudo ss -lnp | grep sshd
ssh-keygen -t rsa -b 4096
# ssh-copy-id <username>@<host>

timedatectl list-timezones
# sudo timedatectl set-timezone <timeszone>

systemd-analyze cat-config systemd/logind.conf
sudo systemctl restart systemd-logind

#If for some reason you wish to enable the root account, simply give it a password:
sudo passwd
#Sudo will prompt you for your password, and then ask you to supply a new password for root as shown below:
# [sudo] password for username: (enter your own password)
# Enter new UNIX password: (enter a new password for root)
# Retype new UNIX password: (repeat new password for root)
# passwd: password updated successfully
#• To disable the root account password, use the following passwd syntax:
sudo passwd -l root

#To add a user account, use the following syntax, and follow the prompts to give the account a password and
#identifiable characteristics, such as a full name, phone number, etc.
sudo adduser username
#• To delete a user account and its primary group, use the following syntax:
sudo deluser username

sudo chown -R root:root /home/username/
sudo mkdir /home/archived_users/
sudo mv /home/username /home/archived_users/

#To temporarily lock or unlock a user password, use the following syntax, respectively:
sudo passwd -l username
sudo passwd -u username
#• To add or delete a personalized group, use the following syntax, respectively:
sudo addgroup groupname
sudo delgroup groupname
#• To add a user to a group, use the following syntax:
sudo adduser username groupname
sudo usermod -aG groupname username

sudo chmod 0750 /home/username
#A much more eﬀicient approach to the matter would be to modify the adduser global default permissions when
#creating user home folders. Simply edit the file
 /etc/adduser.conf
# and modify the DIR_MODE variable to something
#appropriate, so that all new home directories will receive the correct permissions.
# DIR_MODE=0750

#SSH Access by Disabled Users
#Simply disabling/locking a user password will not prevent a user from logging into your server remotely if they have
#previously set up SSH public key authentication. They will still be able to gain shell access to the server, without
#the need for any password. Remember to check the users home directory for files that will allow for this type of
#authenticated SSH access, e.g.
#  /home/username/.ssh/authorized_keys.
#Remove or rename the directory .ssh/ in the user’s home folder to prevent further SSH authentication capabilities.
#Be sure to check for any established SSH connections by the disabled user, as it is possible they may have existing
#inbound or outbound connections. Kill any that are found.
who | grep username # (to get the pts/# terminal)
sudo pkill -f pts/#
#Restrict SSH access to only user accounts that should have it. For example, you may create a group called “sshlogin”
sudo addgroup sshlogin
#and add the group name as the value associated with the AllowGroups variable located in the file
#  /etc/ssh/sshd_config
# AllowGroups sshlogin
#Then add your permitted SSH users to the group “sshlogin”, and restart the SSH service.
sudo adduser username sshlogin
sudo systemctl restart sshd.service



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
# xrandr --output [monitor-name] --brightness [brightness-level]
xrandr --output LVDS-1 --brightness 0.75

cat /sys/class/backlight/intel_backlight/brightness
cat /sys/class/backlight/intel_backlight/actual_brightness
cat /sys/class/backlight/intel_backlight/max_brightness
echo 400 >  /sys/class/backlight/intel_backlight/brightness

# What are ROCKs?
# The Open Container Initiative (OCI) establishes standards for constructing container images that can be
#  reliablyinstalled across a variety of compliant host environments.
# Ubuntu’s LTS Docker Image Portfolio provides OCI-compliant images that receive stable security updates and
#  predictable software updates, thus ensuring consistency in both maintenance schedule and operational 
# interfaces for the underlying software your software builds on.
https://ubuntu.com/security/docker-images

https://hub.docker.com/u/ubuntu/
https://gallery.ecr.aws/ubuntu
https://gallery.ecr.aws/lts

#
