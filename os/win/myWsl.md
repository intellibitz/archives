https://learn.microsoft.com/en-us/windows/wsl/wsl-config

GNOME Desktop
--

To run the Ubuntu GNOME desktop from WSL2 Ubuntu, you’ll need to install a desktop environment and a Windows X server. Here are the steps:

Install Ubuntu on WSL2: First, ensure that you have Ubuntu installed on your WSL2. You can download it from the Microsoft Store1.

Install a Desktop Environment: Next, install a desktop environment on your Ubuntu WSL2. For GNOME, you can use the following command:

    sudo apt update
    sudo apt install ubuntu-desktop

Install a Windows X Server: An X server is needed to display the graphical interface of your Linux apps on Windows. Examples of X servers you can use are VcXsrv, X410, and Xming.

Configure Display Settings: In your Ubuntu terminal, add the following line to the end of your ~/.bashrc file:

    export DISPLAY=$(awk '/nameserver / {print $2; exit}' /etc/resolv.conf 2>/dev/null):0
    export LIBGL_ALWAYS_INDIRECT=1

Then, source the ~/.bashrc file:

    source ~/.bashrc

Run the Desktop Environment: You can now run the GNOME desktop environment with the command:

    gnome-session

Please note that running a full desktop environment can be resource-intensive and might not work perfectly. For most use cases, running individual GUI apps is recommended.

Transport end point is not connected
--

Acpid used for power management can cause the systemctl of wsl to get stuck, while modemmanager is related to the network card and can cause wsl's network settings to get stuck. Simply remove and lock these three when installing the desktop

    apt purge -y acpid acpi-support modemmanager
    apt-mark hold acpid acpi-support modemmanager

Steps to Solve the Issue:
Disable WSL Configuration:

    Open the wsl.conf file.
    Remove the [boot] section.
    Restart WSL:

Execute the following command to shut down WSL:

    wsl --shutdown

Update and Upgrade:

Run the following commands to update and upgrade packages:

    sudo apt-get update && sudo apt-get upgrade

By following these steps, the issue was successfully resolved.

To connect to Ubuntu from Windows using xrdp, follow these steps:
--

Install xrdp on Ubuntu: xrdp is an RDP server for Linux that listens for and accepts RDP connections from clients. To install xrdp on Ubuntu, use the following commands12:

    sudo apt update
    sudo apt install xrdp -y

Enable xrdp: Enable xrdp to start after reboot and run the remote desktop sharing server1:

    sudo systemctl enable --now xrdp

Open a Firewall Port: Open a firewall port 3389 for incoming traffic12:

    sudo ufw allow from any to any port 3389 proto tcp

Connect from Windows:

On your Windows machine, open the Remote Desktop Connection client. Enter the Ubuntu’s remote desktop share IP address or hostname1. Optionally, allow Windows to save your credentials1.
Click ‘Yes’ when prompted by the message: ‘The identity of the remote computer cannot be verified’. Enter the password of the remote Ubuntu user1. You should now be remotely connected to the Ubuntu Desktop share from your Windows computer1
