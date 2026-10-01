#https://docs.docker.com/engine/install/linux-postinstall/
# Manage Docker as a non-root user
# The Docker daemon binds to a Unix socket, not a TCP port. By default it’s the root user that owns the
#  Unix socket, and other users can only access it using sudo. The Docker daemon always runs as the root user.

# If you don’t want to preface the docker command with sudo, create a Unix group called docker and
#  add users to it. When the Docker daemon starts, it creates a Unix socket accessible by members of the
#  docker group. On some Linux distributions, the system automatically creates this group when installing 
# Docker Engine using a package manager. In that case, there is no need for you to manually create the group.
sudo groupadd docker
sudo usermod -aG docker $USER

#On Linux, you can also run the following command to activate the changes to groups:
newgrp docker 
# Verify that you can run docker commands without sudo
docker run hello-world

#If you initially ran Docker CLI commands using sudo before adding your user to the docker group,
# you may see the following error, which indicates that your ~/.docker/ directory was created
# with incorrect permissions due to the sudo commands.

#> WARNING: Error loading config file: /home/user/.docker/config.json -
#> stat /home/user/.docker/config.json: permission denied

#To fix this problem, either remove the ~/.docker/ directory (it is recreated automatically,
# but any custom settings are lost), or change its ownership and permissions using the following commands:

sudo chown "$USER":"$USER" /home/"$USER"/.docker -R
sudo chmod g+rwx "$HOME/.docker" -R

#Verify that you can run docker commands without sudo.
docker run hello-world

#