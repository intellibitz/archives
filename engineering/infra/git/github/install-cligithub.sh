#https://github.com/cli/cli/blob/trunk/docs/install_linux.mD
#Install:
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-key C99B11DEB97541F0
sudo apt-add-repository https://cli.github.com/packages
sudo apt update
sudo apt install gh

#Note: If you are behind a firewall, the connection to keyserver.ubuntu.com might fail. In that case, try running sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-key C99B11DEB97541F0.

#Note: most systems will have apt-add-repository already. If you get a command not found error, try running sudo apt install software-properties-common and trying these steps again.

#Upgrade:
sudo apt update
sudo apt install gH
