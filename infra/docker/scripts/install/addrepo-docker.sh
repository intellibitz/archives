#

# https://docs.docker.com/engine/install/ubuntu/#set-up-the-repository
#https://docs.docker.com/engine/install/ubuntu/
#Update the apt package index and install packages to allow apt to use a repository over HTTPS:
sudo apt-get update

# sudo apt-get install \
    # apt-transport-https \
    # ca-certificates \
    # curl \
    # gnupg-agent \
    # software-properties-common
# Update the apt package index and install packages to allow apt to use a repository over HTTPS:
sudo apt-get install \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

# Add Docker’s official GPG key:
# curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add - # OBSOLETE
sudo mkdir -m 0755 -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | \
  sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

#Verify that you now have the key with the fingerprint 9DC8 5822 9FC7 DD38 854A  E2D8 8D81 803C 0EBF CD88,
# by searching for the last 8 characters of the fingerprint.
# sudo apt-key fingerprint 0EBFCD88 # OBSOLETE
#To add the nightly or test repository, add the word nightly or test (or both) after the word stable in the commands below.
#Use the following command to set up the stable repository. 
# sudo add-apt-repository \
#    "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
#    $(lsb_release -cs) \
#    stable" # OBSOLETE

# Set up the repository:
echo \
  "deb [arch=$(dpkg --print-architecture) \
  signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" \
  | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null


#