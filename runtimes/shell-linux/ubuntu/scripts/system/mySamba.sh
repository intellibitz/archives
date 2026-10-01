#https://ubuntu.com/tutorials/install-and-configure-samba#1-overview
sudo apt install samba samba-common
sudo nano /etc/samba/smb.conf
[sambashare]
    comment = Samba on Ubuntu
    path = /home/username/sambashare
    read only = no
    browsable = yes

sudo service smbd restart
sudo ufw allow samba
sudo smbpasswd -a username
