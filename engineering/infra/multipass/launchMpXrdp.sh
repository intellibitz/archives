#!/usr/bin/env bash
# https://multipass.run/docs/set-up-a-graphical-interface

set -euo pipefail

# Launch Ubuntu 24.04 LTS Desktop VM
multipass launch 24.04 --name xrdp-desktop --cpus 4 --memory 8G --disk 60G
multipass shell xrdp-desktop

# Install graphical desktop and XRDP service
sudo apt update
sudo apt install -y ubuntu-desktop xrdp
sudo passwd ubuntu

multipass list
multipass info xrdp-desktop

# Connect via Remmina RDP client:
# remmina -c rdp://<vm-ip-address>
