sudo apt install ubuntu-desktop xrdp
sudo systemctl enable --now xrdp

sudo ufw allow from any to any port 3389 protoc tcp
/etc/xrdp/startwm.sh
/etc/ssh/sshd_config
export XAUTHORITY=$HOME/.Xauthority 
xhost +

sudo apt-get install -y kubuntu-desktop
# install xrdp
sudo apt-get install xrdp
sudo cp /etc/xrdp/xrdp.ini /etc/xrdp/xrdp.ini.bak
sudo sed -i 's/3389/3390/g' /etc/xrdp/xrdp.ini
sudo sed -i 's/max_bpp=32/#max_bpp=32\nmax_bpp=128/g' /etc/xrdp/xrdp.ini
sudo sed -i 's/xserverbpp=24/#xserverbpp=24\nxserverbpp=128/g' /etc/xrdp/xrdp.ini
sudo ufw allow 3390
Lastly, enable Dbus

#enable dbus
sudo systemctl enable dbus
sudo /etc/init.d/dbus start
sudo /etc/init.d/xrdp start
# check xrdp status
sudo /etc/init.d/xrdp status

Now launch your Windows Remote Desktop Connection, or mstsc from the Windows Start menu.

Remote Desktop Connection - How To Enable WSL2 Ubuntu GUI and use RDP to Remote
enter localhost:3390 and connect. You will be prompt by this screen keep the session as xorg, and enter your WSL2 username and password.

If xrdp is running, stop it via:
> sudo /etc/init.d/xrdp stop

Edit the startwm.sh file:
> sudo nano /etc/xrdp/startwm.sh
Comment out the following two lines by adding a “#” at the beginning of each line:
test -x /etc/X11/Xsession && exec /etc/X11/Xsession
exec /bin/sh /etc/X11/Xsession

Below the now commented lines, add:
#startxfce4
startplasma-X11

Save the file, start xrdp with
> sudo /etc/init.d/xrdp start

Now you should be able to start the remote desktop session on windows and login with your linux username and password


Sure, here are the steps to enable a remote desktop connection to WSL2 Ubuntu on Windows 10:

Update Ubuntu: Run the following commands to update Ubuntu:

sudo apt update
sudo apt -y upgrade

Install a GUI: Install a GUI for Ubuntu. For example, you can install Kubuntu desktop or xfce4 with the following command:

sudo apt-get install -y kubuntu-desktop
OR
sudo apt-get install -y xfce4 xfce4-goodies

Install and Configure XRDP: Install XRDP and change the RDP port to 3390 with the following commands:

sudo apt-get install xrdp
sudo cp /etc/xrdp/xrdp.ini /etc/xrdp/xrdp.ini.bak
sudo sed -i 's/3389/3390/g' /etc/xrdp/xrdp.ini
sudo sed -i 's/max_bpp=32/#max_bpp=32\\nmax_bpp=128/g' /etc/xrdp/xrdp.ini
sudo sed -i 's/xserverbpp=24/#xserverbpp=24\\nxserverbpp=128/g' /etc/xrdp/xrdp.ini

Enable Dbus: Enable Dbus and start XRDP with the following commands:

sudo systemctl enable dbus
sudo /etc/init.d/dbus start
sudo /etc/init.d/xrdp start

Connect via RDP: Now launch your Windows Remote Desktop Connection, or mstsc from the Windows Start menu. Enter localhost:3390 and connect. Keep the session as xorg, and enter your WSL2 username and password1.

Please note that these steps assume you already have WSL2 running and have at least Ubuntu installed1. If you haven’t done so, please install WSL2 and Ubuntu first.

It seems like you’re having trouble with XRDP on WSL2 Ubuntu where it connects but doesn’t show the desktop. Here are a few steps you can try:

Install a GUI distro to Ubuntu: You can install a GUI distro like Kubuntu to your Ubuntu. You can do this by running the following command1:
sudo apt-get install -y kubuntu-desktop

Configure XRDP: Install and configure XRDP, and change the RDP port to 33901. You can do this by running the following commands1:
sudo apt-get install xrdp
sudo cp /etc/xrdp/xrdp.ini /etc/xrdp/xrdp.ini.bak
sudo sed -i 's/3389/3390/g' /etc/xrdp/xrdp.ini
sudo sed -i 's/max_bpp=32/#max_bpp=32\\nmax_bpp=128/g' /etc/xrdp/xrdp.ini
sudo sed -i 's/xserverbpp=24/#xserverbpp=24\\nxserverbpp=128/g' /etc/xrdp/xrdp.ini

Enable Dbus: Lastly, enable Dbus1. You can do this by running the following commands1:
sudo systemctl enable dbus
sudo /etc/init.d/dbus start
sudo /etc/init.d/xrdp start

Export Environment Variables: You need to export the following environment variables2:
export GNOME_SHELL_SESSION_MODE=ubuntu
export XDG_CURRENT_DESKTOP=ubuntu:GNOME
You can add these lines to ~/.xsessionrc or /etc/xrdp/startwm.sh at the very start and then reboot your PC2.
Unset DBUS_SESSION_BUS_ADDRESS and XDG_RUNTIME_DIR: If you still face issues, you can try unsetting DBUS_SESSION_BUS_ADDRESS and XDG_RUNTIME_DIR3. Add the following lines at the top of /etc/xrdp/startwm.sh3:
unset DBUS_SESSION_BUS_ADDRESS
unset XDG_RUNTIME_DIR
Then restart xrdp with sudo systemctl restart xrdp3.
Remember to log out of your account on Ubuntu desktop before trying to connect via RDP on Windows2. If you’re still facing issues, please let me know! I’m here to help.

To set Plasma Desktop as the default remote desktop in WSL2 Ubuntu using XRDP, you can follow these steps:

First, install XRDP and KDE Plasma Desktop if you haven’t already. You can do this by running the following commands in your Ubuntu terminal:
sudo apt install xrdp -y
sudo apt install kde-plasma-desktop -y

Next, you need to create a .xsession file in your home directory that will start the Plasma Desktop. You can do this with the following command:
echo "/usr/bin/startplasma-x11" > ~/.xsession

To increase the quality of the remote session, you can modify the xrdp.ini file with the following commands:
sudo sed -i 's/max_bpp=32/#max_bpp=32\nmax_bpp=128/g' /etc/xrdp/xrdp.ini
sudo sed -i 's/xserverbpp=24/#xserverbpp=24\nxserverbpp=128/g' /etc/xrdp/xrdp.ini

Finally, restart the XRDP service with the following command:
sudo systemctl restart xrdp

Now, when you connect to your WSL2 Ubuntu using a Remote Desktop client, it should start the Plasma Desktop by default123.

