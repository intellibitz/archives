https://docs.docker.com/

#docker.io (default registry for docker images)

https://hub.docker.com/

https://github.com/docker-library/official-images

https://mobyproject.org/

https://www.opencontainers.org/

Docker SysVinit config file at

    /etc/default/docker

Docker Ubuntu systemd service config file at

    /lib/systemd/system/docker.service

Docker daemon config file at

    /etc/docker/daemon.json

In a default Linux installation, the client talks to the daemon via a local IPC/Unix
socket at

    /run/docker.sock.
    /var/run/docker.sock.

On Windows this happens via a named pipe at

    npipe://
    //./pipe/docker_engine.

    service docker status
    systemctl is-active docker
    sudo systemctl stop docker.service
    sudo systemctl stop docker.socket
    systemctl status docker.service

Note: In a standard, out-of-the-box Linux installation, the Docker dae-
mon implements the Docker API on a local Unix socket at /var/run/-
docker.sock. On Windows, it listens on a named pipe at npipe:////./pipe/
docker_-engine. It’s also possible to configure the Docker daemon to listen on the
network. The default non-TLS network port for Docker is 2375, the default
TLS port is 2376.

Containers are all about making apps simple to build, ship, and run. The end-to-end
process looks like this:

    1. Start with your application code and dependencies
    2. Create a Dockerfile that describes your app, dependencies, and how to run it
    3. Build it into an image by passing the Dockerfile to the docker build command
    4. Push the new image to a registry (optional)
    5. Run a container from the image

docker
--

The base command for the Docker CLI.

Options
--

    Option	Default	Description

    --config	/root/.docker	Location of client config files
    -c, --context		Name of the context to use to connect to the daemon (overrides DOCKER_HOST env var and default context set with docker context use)
    -D, --debug		Enable debug mode
    -H, --host		Daemon socket to connect to
    -l, --log-level	info	Set the logging level (debug, info, warn, error, fatal)
    --tls		Use TLS; implied by --tlsverify
    --tlscacert	/root/.docker/ca.pem	Trust certs signed only by this CA
    --tlscert	/root/.docker/cert.pem	Path to TLS certificate file
    --tlskey	/root/.docker/key.pem	Path to TLS key file
    --tlsverify		Use TLS and verify the remote

Subcommands
--

    Command	Description

    docker builder	Manage builds
    docker buildx	Docker Buildx
    docker checkpoint	Manage checkpoints
    docker compose	Docker Compose
    docker config	Manage Swarm configs
    docker container	Manage containers
    docker context	Manage contexts
    docker debug	Get a shell into any container or image. An alternative to debugging with `docker exec`.
    docker image	Manage images
    docker init	Creates Docker-related starter files for your project
    docker inspect	Return low-level information on Docker objects
    docker login	Log in to a registry
    docker logout	Log out from a registry
    docker manifest	Manage Docker image manifests and manifest lists
    docker network	Manage networks
    docker node	Manage Swarm nodes
    docker plugin	Manage plugins
    docker scout	Command line tool for Docker Scout
    docker search	Search Docker Hub for images
    docker secret	Manage Swarm secrets
    docker service	Manage Swarm services
    docker stack	Manage Swarm stacks
    docker swarm	Manage Swarm
    docker system	Manage Docker
    docker trust	Manage trust on Docker images
    docker version	Show the Docker version information
    docker volume	Manage volumes

https://docs.docker.com/network/

The following example runs a Redis container, with Redis binding to localhost, then running the redis-cli command and connecting to the Redis server over the localhost interface.

    docker run -d --name redis example/redis --bind 127.0.0.1
    docker run --rm -it --network container:redis example/redis-cli -h 127.0.0.1

Published ports
--

By default, when you create or run a container using docker create or docker run, the container doesn't expose any of its ports to the outside world. Use the --publish or -p flag to make a port available to services outside of Docker. This creates a firewall rule in the host, mapping a container port to a port on the Docker host to the outside world. Here are some examples:

    Flag value	Description
    -p 8080:80	Map port 8080 on the Docker host to TCP port 80 in the container.
    -p 192.168.1.100:8080:80	Map port 8080 on the Docker host IP 192.168.1.100 to TCP port 80 in the container.
    -p 8080:80/udp	Map port 8080 on the Docker host to UDP port 80 in the container.
    -p 8080:80/tcp -p 8080:80/udp	Map TCP port 8080 on the Docker host to TCP port 80 in the container, and map UDP port 8080 on the Docker host to UDP port 80 in the container.

Important
--

Publishing container ports is insecure by default. Meaning, when you publish a container's ports it becomes available not only to the Docker host, but to the outside world as well.

If you include the localhost IP address (127.0.0.1) with the publish flag, only the Docker host can access the published container port.

    docker run -p 127.0.0.1:8080:80 nginx

If you want to make a container accessible to other containers, it isn't necessary to publish the container's ports. You can enable inter-container communication by connecting the containers to the same network, usually a bridge network.

IP address and hostname
--

By default, the container gets an IP address for every Docker network it attaches to. A container receives an IP address out of the IP subnet of the network. The Docker daemon performs dynamic subnetting and IP address allocation for containers. Each network also has a default subnet mask and gateway.

You can connect a running container to multiple networks, either by passing the --network flag multiple times when creating the container, or using the docker network connect command for already running containers. In both cases, you can use the --ip or --ip6 flags to specify the container's IP address on that particular network.

In the same way, a container's hostname defaults to be the container's ID in Docker. You can override the hostname using --hostname. When connecting to an existing network using docker network connect, you can use the --alias flag to specify an additional network alias for the container on that network.

User-defined networks
--

You can create custom, user-defined networks, and connect multiple containers to the same network. Once connected to a user-defined network, containers can communicate with each other using container IP addresses or container names.

The following example creates a network using the bridge network driver and running a container in the created network:

    docker network create -d bridge my-net
    docker run --network=my-net -itd --name=container3 busybox

Drivers
--

The following network drivers are available by default, and provide core networking functionality:

    Driver	Description
    bridge	The default network driver.
    host	Remove network isolation between the container and the Docker host.
    none	Completely isolate a container from the host and other containers.
    overlay	Overlay networks connect multiple Docker daemons together.
    ipvlan	IPvlan networks provide full control over both IPv4 and IPv6 addressing.
    macvlan	Assign a MAC address to a container.

For more information about the different drivers, see Network drivers overview.

Container networks
--

In addition to user-defined networks, you can attach a container to another container's networking stack directly, using the --network container:<name|id> flag format.

The following flags aren't supported for containers using the container: networking mode:

    --add-host
    --hostname
    --dns
    --dns-search
    --dns-option
    --mac-address
    --publish
    --publish-all
    --expose

docker run
--

The docker run command runs a command in a new container, pulling the image if needed and starting the container.

You can restart a stopped container with all its previous changes intact using docker start. Use docker ps -a to view a list of all containers, including those that are stopped.

    --expose		Expose a port or a range of ports
    --mount		Attach a filesystem mount to the container
    --name		Assign a name to the container
    --network		Connect a container to a network
    -p, --publish		Publish a container's port(s) to the host
    -P, --publish-all		Publish all exposed ports to random ports
    --pull	missing	Pull image before running (always, missing, never)
    --restart	no	Restart policy to apply when a container exits
    --rm		Automatically remove the container when it exits
    --shm-size		Size of /dev/shm
    --storage-opt		Storage driver options for the container
    --tmpfs		Mount a tmpfs directory
    -t, --tty		Allocate a pseudo-TTY
    -u, --user		Username or UID (format: <name|uid>[:<group|gid>])
    -v, --volume		Bind mount a volume
    --volume-driver		Optional volume driver for the container
    --volumes-from		Mount volumes from the specified container(s)
    -w, --workdir		Working directory inside the container

docker exec
--

The docker exec command runs a new command in a running container.

The command you specify with docker exec only runs while the container's primary process (PID 1) is running, and it isn't restarted if the container is restarted.

The command runs in the default working directory of the container.

The command must be an executable. A chained or a quoted command doesn't work.

    This works: docker exec -it my_container sh -c "echo a && echo b"
    This doesn't work: docker exec -it my_container "echo a && echo b"

Options
--

    Option	Default	Description
    -d, --detach		Detached mode: run command in the background
    --detach-keys		Override the key sequence for detaching a container
    -e, --env		API 1.25+ Set environment variables
    --env-file		API 1.25+ Read in a file of environment variables
    -i, --interactive		Keep STDIN open even if not attached
    --privileged		Give extended privileges to the command
    -t, --tty		Allocate a pseudo-TTY
    -u, --user		Username or UID (format: <name|uid>[:<group|gid>])
    -w, --workdir		API 1.35+ Working directory inside the container

docker compose up
--

Create and start containers

    Usage	docker compose up [OPTIONS] [SERVICE...]

Description

    Builds, (re)creates, starts, and attaches to containers for a service.

Unless they are already running, this command also starts any linked services.

The docker compose up command aggregates the output of each container (like docker compose logs --follow does). One can optionally select a subset of services to attach to using --attach flag, or exclude some services using --no-attach to prevent output to be flooded by some verbose services.

When the command exits, all containers are stopped. Running docker compose up --detach starts the containers in the background and leaves them running.

If there are existing containers for a service, and the service’s configuration or image was changed after the container’s creation, docker compose up picks up the changes by stopping and recreating the containers (preserving mounted volumes). To prevent Compose from picking up changes, use the --no-recreate flag.

If you want to force Compose to stop and recreate all containers, use the --force-recreate flag.

If the process encounters an error, the exit code for this command is 1. If the process is interrupted using SIGINT (ctrl + C) or SIGTERM, the containers are stopped, and the exit code is 0.

Options
--

    Option	Default	Description
    --abort-on-container-exit		Stops all containers if any container was stopped. Incompatible with -d
    --always-recreate-deps		Recreate dependent containers. Incompatible with --no-recreate.
    --attach		Restrict attaching to the specified services. Incompatible with --attach-dependencies.
    --attach-dependencies		Automatically attach to log output of dependent services
    --build		Build images before starting containers
    -d, --detach		Detached mode: Run containers in the background
    --exit-code-from		Return the exit code of the selected service container. Implies --abort-on-container-exit
    --force-recreate		Recreate containers even if their configuration and image haven't changed
    --no-attach		Do not attach (stream logs) to the specified services
    --no-build		Don't build an image, even if it's policy
    --no-color		Produce monochrome output
    --no-deps		Don't start linked services
    --no-log-prefix		Don't print prefix in logs
    --no-recreate		If containers already exist, don't recreate them. Incompatible with --force-recreate.
    --no-start		Don't start the services after creating them
    --pull	policy	Pull image before running ("always"|"missing"|"never")
    --quiet-pull		Pull without printing progress information
    --remove-orphans		Remove containers for services not defined in the Compose file
    -V, --renew-anon-volumes		Recreate anonymous volumes instead of retrieving data from the previous containers
    --scale		Scale SERVICE to NUM instances. Overrides the scale setting in the Compose file if present.
    -t, --timeout		Use this timeout in seconds for container shutdown when attached or when containers are already running
    --timestamps		Show timestamps
    --wait		Wait for services to be running|healthy. Implies detached mode.
    --wait-timeout		Maximum duration to wait for the project to be running|healthy
    -w, --watch		Watch source code and rebuild/refresh containers when files are updated.

docker image tag
--

Description Create a tag TARGET_IMAGE that refers to SOURCE_IMAGE
Usage docker image tag SOURCE_IMAGE[:TAG] TARGET_IMAGE[:TAG]
Aliases

docker tag

docker image rm
--

Description Remove one or more images
Usage docker image rm [OPTIONS] IMAGE [IMAGE...]
Aliases

docker image remove
docker rmi
Description

Removes (and un-tags) one or more images from the host node. If an image has multiple tags, using this command with the tag as a parameter only removes the tag. If the tag is the only one for the image, both the image and the tag are removed.

This does not remove images from a registry. You cannot remove an image of a running container unless you use the -f option. To see all images on a host use the docker image ls command.

https://docs.docker.com/get-started/06_bind_mounts/

Quick volume type comparisons
--

The following are examples of a named volume and a bind mount using --mount:

    Named volume: type=volume,src=my-volume,target=/usr/local/data
    Bind mount: type=bind,src=/path/to/data,target=/usr/local/data

The following table outlines the main differences between volume mounts and bind mounts.

                                                    Named volumes	Bind mounts

    Host location	                                Docker chooses	You decide
    Populates new volume with container contents	Yes	            No
    Supports Volume Drivers	                        Yes	            No
