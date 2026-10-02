#!/usr/bin/env sh
# https://multipass.run/docs/use-a-blueprint

multipass launch docker --name docker-dev --cpus 4 --memory 8G --disk 60G --bridged

# This command will create an instance based on the docker blueprint, with 4 CPU cores, 8GB of RAM, 50 GB of disk space, and connect that instance to the (predefined) bridged network.

# Blueprints also provide a way of exchanging files between the host and the instance. For this, a folder named multipass/<instance name> is created in the user’s home directory on the host and mounted in <instance name> in the user’s home directory on the instance.

