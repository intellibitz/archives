#!/usr/bin/env sh

sudo apt-get install openssh-server

sudo systemctl enable ssh
## OR enable and start the ssh service immediately ##
sudo systemctl enable ssh --now

sudo systemctl start ssh

ssh userName@Your-server-name-IP
ssh ec2-user@ec2-aws-ip-here

sudo ufw allow ssh
sudo ufw enable
sudo ufw status

ssh-keygen
ssh-copy-id user@ip

/etc/ssh #configuration files

