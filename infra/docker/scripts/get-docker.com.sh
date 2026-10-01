#!/bin/sh

curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh ./get-docker.sh --dry-run

curl -fsSL https://get.docker.com/rootless -o get-docker-rootless.sh
sh ./get-docker-rootless.sh