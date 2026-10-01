#https://download.docker.com/linux/ubuntu/dists/focal/pool/stable/amd64/
# Install from a package
# If you can’t use Docker’s apt repository to install Docker Engine, you can download the 
# deb file for your release and install it manually. You need to download a new file each
#  time you want to upgrade Docker Engine.

https://download.docker.com/linux/ubuntu/dists/.

# Select your Ubuntu version in the list.
# Go to pool/stable/ and select the applicable architecture (amd64, armhf, arm64, or s390x).
# Download the following deb files for the Docker Engine, CLI, containerd, and Docker Compose packages:

containerd.io_<version>_<arch>.deb
docker-ce_<version>_<arch>.deb
docker-ce-cli_<version>_<arch>.deb
docker-buildx-plugin_<version>_<arch>.deb
docker-compose-plugin_<version>_<arch>.deb

[ -f ./containerd.io_1.2.13-2_amd64.deb ] || \
    curl -sO https://download.docker.com/linux/ubuntu/dists/focal/pool/stable/amd64/containerd.io_1.2.13-2_amd64.deb
[ -f ./docker-ce-cli_19.03.12~3-0~ubuntu-focal_amd64.deb ] || \
    curl -sO https://download.docker.com/linux/ubuntu/dists/focal/pool/stable/amd64/docker-ce-cli_19.03.12~3-0~ubuntu-focal_amd64.deb
[ -f ./docker-ce_19.03.12~3-0~ubuntu-focal_amd64.deb ] || \
    curl -sO https://download.docker.com/linux/ubuntu/dists/focal/pool/stable/amd64/docker-ce_19.03.12~3-0~ubuntu-focal_amd64.deb

# Install the .deb packages. Update the paths in the following example to where you downloaded the
#  Docker packages.

# installs with dependencies
# sudo apt update && \
# sudo apt --fix-broken install && \
# sudo apt -f install \
    # iptables libip6tc2 libnetfilter-conntrack3 libnfnetlink0 libnftnl11
#https://docs.docker.com/engine/install/ubuntu/
# sudo dpkg -iE ./docker-ce_19.03.12~3-0~ubuntu-focal_amd64.deb && \
# sudo dpkg -iE ./docker-ce-cli_19.03.12~3-0~ubuntu-focal_amd64.deb && \
# sudo dpkg -iE ./containerd.io_1.2.13-2_amd64.deb

sudo dpkg -i ./containerd.io_<version>_<arch>.deb \
  ./docker-ce_<version>_<arch>.deb \
  ./docker-ce-cli_<version>_<arch>.deb \
  ./docker-buildx-plugin_<version>_<arch>.deb \
  ./docker-compose-plugin_<version>_<arch>.deb

# The Docker daemon starts automatically.
# Verify that the Docker Engine installation is successful by running the hello-world image:

# sudo apt --fix-broken install
sudo service docker start
sudo docker run hello-world

# This command downloads a test image and runs it in a container. When the container runs, it
#  prints a confirmation message and exits.
# You have now successfully installed and started Docker Engine. The docker user group exists but
#  contains no users, which is why you’re required to use sudo to run Docker commands.
#  Continue to Linux post-install to allow non-privileged users to run Docker commands and for other
#  optional configuration steps.

