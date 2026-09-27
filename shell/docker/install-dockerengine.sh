#

#https://docs.docker.com/engine/install/ubuntu/

sudo apt-get remove docker docker-engine docker.io containerd runc
sudo apt-get update

# Update the apt package index and install packages to allow apt to use a repository over HTTPS:
sudo apt-get install \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

# Add Docker’s official GPG key:
sudo mkdir -m 0755 -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Set up the repository:
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
  | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Your default umask may be incorrectly configured, preventing detection of the repository
#  public key file. Try granting read permission for the Docker public key file before
#  updating the package index:
sudo chmod a+r /etc/apt/keyrings/docker.gpg
sudo apt-get update

# Install LATEST Docker Engine, containerd, and Docker Compose.
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo docker run hello-world

# To install a specific version of Docker Engine, start by list the available versions in the repository:
# List the available versions:
apt-cache madison docker-ce | awk '{ print $3 }'
5:20.10.16~3-0~ubuntu-jammy
5:20.10.15~3-0~ubuntu-jammy
5:20.10.14~3-0~ubuntu-jammy
5:20.10.13~3-0~ubuntu-jammy

# Install specific version Docker Engine, containerd, and Docker Compose.
VERSION_STRING=5:20.10.13~3-0~ubuntu-jammy
sudo apt-get install docker-ce=$VERSION_STRING docker-ce-cli=$VERSION_STRING \
    containerd.io docker-buildx-plugin docker-compose-plugin
sudo docker run hello-world


#