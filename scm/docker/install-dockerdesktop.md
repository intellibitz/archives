#

https://docs.docker.com/desktop/install/linux-install/

https://docs.docker.com/desktop/install/linux-install/#system-requirements
System requirements
To install Docker Desktop successfully, your Linux host must meet the following general requirements:

64-bit kernel and CPU support for virtualization.
KVM virtualization support. Follow the KVM virtualization support instructions to check if the KVM kernel modules are enabled and how to provide access to the kvm device.
QEMU must be version 5.2 or newer. We recommend upgrading to the latest version.
systemd init system.
Gnome, KDE, or MATE Desktop environment.
For many Linux distros, the Gnome environment does not support tray icons. To add support for tray icons, you need to install a Gnome extension. For example, AppIndicator.
At least 4 GB of RAM.
Enable configuring ID mapping in user namespaces, see File sharing.
Docker Desktop for Linux runs a Virtual Machine (VM). For more information on why, see Why Docker Desktop for Linux runs a VM.

#1. Uninstall previous docker versions
#2. Set up docker repository
#3. Set up KVM
#4. Install docker desktop using apt repository

# Important

# Docker Desktop on Linux runs a Virtual Machine (VM) so creates and uses a custom docker context

# desktop-linux on startup.

# This means images and containers deployed on the Linux Docker Engine (before installation) are

# not available in Docker Desktop for Linux.

# Docker Desktop for Linux and Docker Engine can be installed side-by-side on the same machine. Docker Desktop for Linux stores containers and images in an isolated storage location within a VM and offers controls to restrict its resources. Using a dedicated storage location for Docker Desktop prevents it from interfering with a Docker Engine installation on the same machine.

# While it’s possible to run both Docker Desktop and Docker Engine simultaneously, there may be situations where running both at the same time can cause issues. For example, when mapping network ports (-p / --publish) for containers, both Docker Desktop and Docker Engine may attempt to reserve the same port on your machine, which can lead to conflicts (“port already in use”).

# We generally recommend stopping the Docker Engine while you’re using Docker Desktop to prevent the Docker Engine from consuming resources and to prevent conflicts as described above.

# Use the following command to stop the Docker Engine service:

sudo systemctl stop docker docker.socket containerd
sudo systemctl disable docker docker.socket containerd

# Docker Desktop runs a VM that requires KVM support.

# The kvm module should load automatically if the host has virtualization support. To load the module manually, run:

modprobe kvm

lsmod | grep kvm
kvm_amd 167936 0
ccp 126976 1 kvm_amd
kvm 1089536 1 kvm_amd
irqbypass 16384 1 kvm

ls -al /dev/kvm

# after the following group addition, logout and login again to see the group changes for the user

sudo usermod -aG kvm $USER

#There are a few post-install configuration steps done through the post-install script contained in the deb package.

The post-install script:

Sets the capability on the Docker Desktop binary to map privileged ports and set resource limits.
Adds a DNS name for Kubernetes to /etc/hosts.
Creates a link from /usr/bin/docker to /usr/local/bin/com.docker.cli.

#Launch Docker Desktop
To start Docker Desktop for Linux, search Docker Desktop on the Applications menu and open it. This launches the Docker menu icon and opens the Docker Dashboard, reporting the status of Docker Desktop.
#Alternatively, open a terminal and run:
systemctl --user start docker-desktop

#When Docker Desktop starts, it creates a dedicated context that the Docker CLI can use as a target and

# sets it as the current context in use. This is to avoid a clash with a local Docker Engine that may be

# running on the Linux host and using the default context. On shutdown, Docker Desktop resets the

# current context to the previous one.

#The Docker Desktop installer updates Docker Compose and the Docker CLI binaries on the host. It

# installs Docker Compose V2 and gives users the choice to link it as docker-compose from the

# Settings panel. Docker Desktop installs the new Docker CLI binary that includes cloud-integration

# capabilities in /usr/local/bin and creates a symlink to the classic Docker CLI at

# /usr/local/bin/com.docker.cli.

#After you’ve successfully installed Docker Desktop, you can check the versions of these binaries by

# running the following commands:

docker compose version
Docker Compose version v2.5.0
docker --version
Docker version 20.10.14, build a224086349
docker version
Client: Docker Engine - Community
Cloud integration: 1.0.24
Version: 20.10.14
API version: 1.41
...

#To enable Docker Desktop to start on login, from the Docker menu, select

# Settings > General > Start Docker Desktop when you log in.

#Alternatively, open a terminal and run:
systemctl --user enable docker-desktop

#To stop Docker Desktop, select the Docker menu icon to open the Docker menu and select

# Quit Docker Desktop.

#Alternatively, open a terminal and run:
systemctl --user stop docker-desktop

How do I switch between Docker Desktop and Docker Engine
The Docker CLI can be used to interact with multiple Docker Engines. For example, you can use the same Docker CLI to control a local Docker Engine and to control a remote Docker Engine instance running in the cloud. Docker Contexts allow you to switch between Docker Engines instances.

When installing Docker Desktop, a dedicated “desktop-linux” context is created to interact with Docker Desktop. On startup, Docker Desktop automatically sets its own context (desktop-linux) as the current context. This means that subsequent Docker CLI commands target Docker Desktop. On shutdown, Docker Desktop resets the current context to the default context.

# Use the docker context ls command to view what contexts are available on your machine. The current context is indicated with an asterisk (*);

docker context ls
NAME DESCRIPTION DOCKER ENDPOINT ...
default * Current DOCKER_HOST based configuration unix:///var/run/docker.sock ...
desktop-linux unix:///home/<user>/.docker/desktop/docker.sock ...

# If you have both Docker Desktop and Docker Engine installed on the same machine, you can run the docker context use command to switch between the Docker Desktop and Docker Engine contexts. For example, use the “default” context to interact with the Docker Engine;

docker context use default
default
Current context is now "default"

# And use the desktop-linux context to interact with Docker Desktop:

docker context use desktop-linux
desktop-linux
Current context is now "desktop-linux"

Note:

# Docker does not provide support for running Docker Desktop in nested virtualization scenarios. We recommend that you run Docker Desktop for Linux natively on supported distributions.

#
