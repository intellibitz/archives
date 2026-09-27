Docker Engine overview
--

https://docs.docker.com/engine/

Docker Engine is an open source containerization technology for building and containerizing your applications.
Docker Engine acts as a client-server application with:

A server with a long-running daemon process dockerd.
APIs which specify interfaces that programs can use to talk to and instruct the Docker daemon.
A command line interface (CLI) client docker.

The CLI uses Docker APIs to control or interact with the Docker daemon through scripting or direct CLI commands.
Many other Docker applications use the underlying API and CLI. The daemon creates and manage Docker objects,
such as images, containers, networks, and volumes.

Docker architecture
--

https://docs.docker.com/get-started/overview/#docker-architecture

Docker uses a client-server architecture. The Docker client talks to the Docker daemon, which does the
heavy lifting of building, running, and distributing your Docker containers. The Docker client and daemon can
run on the same system, or you can connect a Docker client to a remote Docker daemon. The Docker client and
daemon communicate using a REST API, over UNIX sockets or a network interface. Another Docker client is
Docker Compose, that lets you work with applications consisting of a set of containers.

https://docs.docker.com/assets/images/architecture.svg
![](https://docs.docker.com/assets/images/architecture.svg)

https://daringfireball.net/projects/markdown/basics

#

    https://docs.docker.com/engine/security/rootless/

Run the Docker daemon as a non-root user (Rootless mode)
Rootless mode allows running the Docker daemon and containers as a non-root user to mitigate potential vulnerabilities in the daemon and the container runtime.

Rootless mode does not require root privileges even during the installation of the Docker daemon, as long as the prerequisites are met.

Rootless mode was introduced in Docker Engine v19.03 as an experimental feature. Rootless mode graduated from experimental in Docker Engine v20.10.

Starting Rootless Docker as a systemd-wide service (/etc/systemd/system/docker.service) is not supported, even with the User= directive.

Remarks about directory paths:

The socket path is set to $XDG_RUNTIME_DIR/docker.sock by default. $XDG_RUNTIME_DIR is typically set to /run/user/$UID.
The data dir is set to ~/.local/share/docker by default. The data dir should not be on NFS.
The daemon config dir is set to ~/.config/docker by default. This directory is different from ~/.docker that is used by the client.

    https://docs.docker.com/engine/install/linux-postinstall/

Manage Docker as a non-root user
The Docker daemon binds to a Unix socket, not a TCP port. By default it’s the root user that owns the Unix socket, and other users can only access it using sudo. The Docker daemon always runs as the root user.

If you don’t want to preface the docker command with sudo, create a Unix group called docker and add users to it. When the Docker daemon starts, it creates a Unix socket accessible by members of the docker group. On some Linux distributions, the system automatically creates this group when installing Docker Engine using a package manager. In that case, there is no need for you to manually create the group.

    https://docs.docker.com/engine/install/ubuntu/

Install using the convenience script

    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh ./get-docker.sh --dry-run


    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh

Executing docker install script, commit: 7cae5f8b0decc17d6571f9f52eb840fbc13b2737
<...>

Docker provides a convenience script at https://get.docker.com/ to install Docker into development environments non-interactively. The convenience script isn’t recommended for production environments, but it’s useful for creating a provisioning script tailored to your needs. Also refer to the install using the repository steps to learn about installation steps to install using the package repository. The source code for the script is open source, and you can find it in the docker-install repository on GitHub.

Always examine scripts downloaded from the internet before running them locally. Before installing, make yourself familiar with potential risks and limitations of the convenience script:

The script requires root or sudo privileges to run.
The script attempts to detect your Linux distribution and version and configure your package management system for you.
The script doesn’t allow you to customize most installation parameters.
The script installs dependencies and recommendations without asking for confirmation. This may install a large number of packages, depending on the current configuration of your host machine.
By default, the script installs the latest stable release of Docker, containerd, and runc. When using this script to provision a machine, this may result in unexpected major version upgrades of Docker. Always test upgrades in a test environment before deploying to your production systems.
The script isn’t designed to upgrade an existing Docker installation. When using the script to update an existing installation, dependencies may not be updated to the expected version, resulting in outdated versions.

#
