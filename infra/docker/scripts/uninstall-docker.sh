#

#https://docs.docker.com/engine/install/ubuntu/
#Uninstalls old versions
sudo apt-get remove \
  docker \
  docker-engine \
  docker.io \
  containerd \
  runc

#sudo apt-get purge docker-ce docker-ce-cli containerd.io
#Uninstall the Docker Engine, CLI, containerd, and Docker Compose packages:
sudo apt-get purge \
  docker-ce \
  docker-ce-cli \
  containerd.io \
  docker-buildx-plugin \
  docker-compose-plugin \
  docker-ce-rootless-extras

#Images, containers, volumes, or custom configuration files on your host aren’t automatically removed.
# To delete all images, containers, and volumes:
# deletes docker installation folder
sudo rm -rf /var/lib/docker
sudo rm -rf /var/lib/containerd

# stops services
# Use the following command to stop the Docker Engine services:
sudo systemctl stop docker docker.socket containerd
sudo systemctl disable docker docker.socket containerd

#