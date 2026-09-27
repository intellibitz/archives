#!/usr/bin/env bash
# ==============================================================================
# Kubernetes Tools Installation (kubelet, kubeadm, kubectl)
# Official Community Repository: pkgs.k8s.io (Kubernetes 1.30+)
# ==============================================================================

set -euo pipefail

K8S_VERSION="${1:-v1.31}"

echo "Configuring Kubernetes package repository for version: ${K8S_VERSION}..."

sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl gpg

# Create keyrings directory
sudo mkdir -m 0755 -p /etc/apt/keyrings

# Remove deprecated Google Cloud archive key if present
sudo rm -f /etc/apt/keyrings/kubernetes-archive-keyring.gpg
sudo rm -f /etc/apt/sources.list.d/kubernetes.list

# Download official Kubernetes package signing key from pkgs.k8s.io
curl -fsSL "https://pkgs.k8s.io/core:/stable:/${K8S_VERSION}/deb/Release.key" | \
    sudo gpg --dearmor --yes -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
sudo chmod 0644 /etc/apt/keyrings/kubernetes-apt-keyring.gpg

# Add official Kubernetes repository source
echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/${K8S_VERSION}/deb/ /" | \
    sudo tee /etc/apt/sources.list.d/kubernetes.list > /dev/null

# Configure kernel modules required for container runtimes
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter

# Configure sysctl networking parameters
cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system

# Install Kubernetes CLI and node components
sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl

echo "Kubernetes tools (${K8S_VERSION}) installed successfully!"
kubectl version --client --output=yaml || true
