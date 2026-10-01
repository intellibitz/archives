#https://docs.docker.com/engine/install/linux-postinstall/
#Configure Docker to start on boot

# sudo systemctl enable docker

sudo systemctl enable docker.service
sudo systemctl enable containerd.service

sudo systemctl disable docker.service
sudo systemctl disable containerd.service

#