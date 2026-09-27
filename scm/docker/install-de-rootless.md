#

https://docs.docker.com/engine/security/rootless/

# You must install newuidmap and newgidmap on the host. These commands are provided by the uidmap package on

# most distros.

# /etc/subuid and /etc/subgid should contain at least 65,536 subordinate UIDs/GIDs for the user.

# In the following example, the user testuser has 65,536 subordinate UIDs/GIDs (231072-296607).

id -u

# 1001

whoami

# testuser

grep ^$(whoami): /etc/subuid

# testuser:231072:65536

grep ^$(whoami): /etc/subgid

# testuser:231072:65536

# Install dbus-user-session package if not installed. Run

sudo apt-get install -y dbus-user-session

# and relogin.

# overlay2 storage driver is enabled by default (Ubuntu-specific kernel patch).

# If the system-wide Docker daemon is already running, consider disabling it: $

sudo systemctl disable --now docker.service docker.socket

# If dockerd-rootless-setuptool.sh is not present, you may need to install the

# docker-ce-rootless-extras package manually, e.g.,

sudo apt-get install -y docker-ce-rootless-extras

# If you installed Docker 20.10 or later with RPM/DEB packages, you should have dockerd-rootless-setuptool.sh in /usr/bin.

# Run dockerd-rootless-setuptool.sh install as a non-root user to set up the daemon:

dockerd-rootless-setuptool.sh install
[INFO] Creating /home/testuser/.config/systemd/user/docker.service
...
[INFO] Installed docker.service successfully.
[INFO] To control docker.service, run: `systemctl --user (start|stop|restart) docker.service`
[INFO] To run docker.service on system startup, run: `sudo loginctl enable-linger testuser`

[INFO] Make sure the following environment variables are set (or add them to ~/.bashrc):

export PATH=/usr/bin:$PATH
export DOCKER_HOST=unix:///run/user/1000/docker.sock

# To remove the systemd service of the Docker daemon, run

dockerd-rootless-setuptool.sh uninstall

- systemctl --user stop docker.service
- systemctl --user disable docker.service
  Removed /home/testuser/.config/systemd/user/default.target.wants/docker.service.
  [INFO] Uninstalled docker.service
  [INFO] This uninstallation tool does NOT remove Docker binaries and data.
  [INFO] To remove data, run:
  `/usr/bin/rootlesskit rm -rf /home/testuser/.local/share/docker`

# Unset environment variables PATH and DOCKER_HOST if you have added them to ~/.bashrc.

# To remove the data directory, run rootlesskit rm -rf ~/.local/share/docker.

# To remove the binaries, remove docker-ce-rootless-extras package if you installed Docker with

# package managers. If you installed Docker with https://get.docker.com/rootless

# (Install without packages), remove the binary files under ~/bin:

cd ~/bin
rm -f containerd containerd-shim containerd-shim-runc-v2 \
ctr docker docker-init docker-proxy dockerd \
dockerd-rootless-setuptool.sh dockerd-rootless.sh rootlesskit rootlesskit-docker-proxy runc vpnkit

systemctl --user start docker
systemctl --user enable docker
sudo loginctl enable-linger $(whoami)

# Starting Rootless Docker as a systemd-wide service (/etc/systemd/system/docker.service) is not supported,

# even with the User= directive.

# Client

# You need to specify either the socket path or the CLI context explicitly.

# To specify the socket path using $DOCKER_HOST:

export DOCKER_HOST=unix://$XDG_RUNTIME_DIR/docker.sock
docker run -d -p 8080:80 nginx

# To specify the CLI context using docker context:

docker context use rootless

# rootless

# Current context is now "rootless"

docker run -d -p 8080:80 nginx

# Rootless Docker in Docker

# To run Rootless Docker inside “rootful” Docker, use the docker:<version>-dind-rootless image instead of docker:<version>-dind.

docker run -d --name dind-rootless --privileged docker:20.10-dind-rootless

# The docker:<version>-dind-rootless image runs as a non-root user (UID 1000).

# However, --privileged is required for disabling seccomp, AppArmor, and mount masks.

# Expose Docker API socket through TCP

# To expose the Docker API socket through TCP, you need to launch dockerd-rootless.sh with DOCKERD_ROOTLESS_ROOTLESSKIT_FLAGS="-p 0.0.0.0:2376:2376/tcp".

DOCKERD_ROOTLESS_ROOTLESSKIT_FLAGS="-p 0.0.0.0:2376:2376/tcp" \
dockerd-rootless.sh \
-H tcp://0.0.0.0:2376 \
--tlsverify --tlscacert=ca.pem --tlscert=cert.pem --tlskey=key.pem

# Expose Docker API socket through SSH

# To expose the Docker API socket through SSH, you need to make sure $DOCKER_HOST is set on the remote host.

ssh -l <REMOTEUSER> <REMOTEHOST> 'echo $DOCKER_HOST'
unix:///run/user/1001/docker.sock
docker -H ssh://<REMOTEUSER>@<REMOTEHOST> run ...

https://docs.docker.com/engine/security/rootless/

#
