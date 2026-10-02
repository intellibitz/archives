#!/usr/bin/env sh

#wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
#sudo apt install curl ca-certificates
#sudo install -d /usr/share/postgresql-common/pgdg
#sudo curl -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc --fail https://www.postgresql.org/media/keys/ACCC4CF8.asc

sudo apt-get -y install ca-certificates gnupg apt-transport-https curl lsb-release

# Import the repository signing key:
sudo mkdir -m 0755 -p /etc/apt/keyrings
sudo rm /etc/apt/keyrings/postgresql-ACCC4CF8-keyring.gpg
curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc | \
 sudo gpg --dearmor -o /etc/apt/keyrings/postgresql-ACCC4CF8-keyring.gpg
sudo chmod a+r /etc/apt/keyrings/postgresql-ACCC4CF8-keyring.gpg

#sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
#deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] https://apt.postgresql.org/pub/repos/apt bookworm-pgdg main
#sudo sh -c 'echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'

# Create the file repository configuration:
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/postgresql-ACCC4CF8-keyring.gpg] \
 https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" | \
 sudo tee /etc/apt/sources.list.d/pgdg.list > /dev/null

# Update the package lists:
sudo apt-get update
# Install the latest version of PostgreSQL.
# If you want a specific version, use 'postgresql-16' or similar instead of 'postgresql':
sudo apt-get -y install postgresql
