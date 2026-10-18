#!/usr/bin/env bash
# ==============================================================================
# setup_linuxshare_permissions.sh
# Ensures /mnt/linuxshare/src/github.com/intellibitz/archives is read/write
# for both Linux and Windows (WinBtrfs).
# ==============================================================================
set -euo pipefail

TARGET_DIR="${1:-/mnt/linuxshare/src/github.com/intellibitz/archives}"
PRIMARY_USER="${SUDO_USER:-$(id -un)}"
PRIMARY_UID="${SUDO_UID:-$(id -u)}"
PRIMARY_GID="${SUDO_GID:-$(id -g)}"
WIN_UID=65534 # nobody (WinBtrfs fallback UID)
WIN_GID=100   # winusers / WinBtrfs default group

echo "=== Setting up cross-platform permissions for: $TARGET_DIR ==="
echo "Primary User: $PRIMARY_USER ($PRIMARY_UID:$PRIMARY_GID)"
echo "Windows Identity: $WIN_UID:$WIN_GID"

# Ensure GID 100 group exists on Linux
if ! getent group "$WIN_GID" >/dev/null 2>&1; then
    echo "Creating group 'winusers' (GID $WIN_GID)..."
    sudo groupadd -g "$WIN_GID" winusers
fi

# Ensure primary user is in GID 100 group
if ! id -nG "$PRIMARY_USER" | grep -qw "winusers"; then
    echo "Adding $PRIMARY_USER to winusers group..."
    sudo usermod -aG winusers "$PRIMARY_USER"
fi

# Set ownership
echo "Ensuring ownership ($PRIMARY_UID:$PRIMARY_GID)..."
sudo chown -R "$PRIMARY_UID:$PRIMARY_GID" "$TARGET_DIR"

# Set permissions: dirs 777 (rwxrwxrwx), files 666 (rw-rw-rw-), preserve existing execute bits
echo "Setting base permissions..."
sudo chmod -R a+rwX "$TARGET_DIR"

# Set setgid bit on all directories for group inheritance
echo "Setting setgid on directories..."
sudo find "$TARGET_DIR" -type d -exec chmod g+s {} +

# Apply POSIX ACLs
echo "Applying POSIX ACLs..."
ACL_SPEC="u:${PRIMARY_UID}:rwx,u:${WIN_UID}:rwx,g:${PRIMARY_GID}:rwx,g:${WIN_GID}:rwx,m::rwx,o::rwx"
sudo setfacl -R -m "$ACL_SPEC" "$TARGET_DIR"
sudo setfacl -R -d -m "$ACL_SPEC" "$TARGET_DIR"

# Git repository configuration
if [ -d "$TARGET_DIR/.git" ]; then
    echo "Configuring repository git settings..."
    git -C "$TARGET_DIR" config core.sharedRepository world
    git -C "$TARGET_DIR" config core.filemode false
    git -C "$TARGET_DIR" config core.trustctime false
    
    # Ensure system-wide safe directory is configured
    sudo git config --system --add safe.directory "$TARGET_DIR" 2>/dev/null || true
fi

echo "✅ Done! Repository is fully read/write for both Linux and Windows."
