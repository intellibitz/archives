#https://docs.docker.com/engine/install/linux-postinstall/
#Configure Docker to stop on boot

# sudo systemctl disable docker

sudo systemctl disable docker.service
sudo systemctl disable containerd.service

#