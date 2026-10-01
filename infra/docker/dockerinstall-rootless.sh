#!/bin/sh

sudo systemctl stop --now docker.service docker.socket
sudo systemctl disable --now docker.service docker.socket
systemctl --user stop docker ; systemctl --user disable docker
dockerd-rootless-setuptool.sh --force uninstall

sudo apt-get install -y uidmap dbus-user-session docker-ce-rootless-extras
dockerd-rootless-setuptool.sh --force install

sudo rm -rf /etc/systemd/system/user@.service.d
sudo mkdir -m 0775 -p /etc/systemd/system/user@.service.d
sudo chown :"$USER" /etc/systemd/system/user@.service.d -R
cat > /etc/systemd/system/user@.service.d/delegate.conf <<'EOF'
[Service]
Delegate=cpu cpuset io memory pids
EOF

cat <<EOF | sudo tee /etc/sysctl.d/docker.conf
# added by $(id -un) on $(date)
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ping_group_range = 0 2147483647
net.ipv4.ip_unprivileged_port_start=0
EOF

mkdir -p ~/.config/systemd/user/docker.service.d
cat > ~/.config/systemd/user/docker.service.d/override.conf <<'EOF'
[Service]
Environment="DOCKERD_ROOTLESS_ROOTLESSKIT_PORT_DRIVER=slirp4netns"
EOF

sudo modprobe br_netfilter
sudo sysctl -p /etc/sysctl.conf
sudo sysctl --system
sudo systemctl daemon-reload
systemctl --user daemon-reload

sudo loginctl enable-linger "$(whoami)"
export DOCKER_HOST=unix://"$XDG_RUNTIME_DIR"/docker.sock

systemctl --user enable docker ; systemctl --user start docker

docker context use rootless
docker run hello-world
