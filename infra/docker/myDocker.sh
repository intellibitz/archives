#client:docker, engine:dockerd
sudo usermod -aG docker zbook

sudo service docker start
service docker status
systemctl is-active docker

dockerd -H unix:// -H tcp://0.0.0.0:2376
dockerd -H fd:// -H tcp://0.0.0.0:2376

#    /lib/systemd/system/docker.service
# -H fd:// -H tcp://0.0.0.0: #change in systemd service config (UNSAFE!)

docker version
docker -H ssh://user@host version #remote connect to docker
docker info

docker login #password will be stored unencrypted in /home/zbook/.docker/config.json.
# docker login --username <YOUR_USERNAME> #use access token for password, if 2fa enabled

docker images #lists all images stored in local image cache
docker images -q #returns all <image-id>
docker images --digests
# docker images --digests <repo-name>

docker images --filter dangling=true
docker images --filter=reference="*:latest"
docker images --format "{{.Size}}"
docker images --format "{{.Repository}}: {{.Tag}}: {{.Size}}"

# docker search <repo-name>
docker search intellibitz #searches docker hub with repository name matching 'intellibitz'
docker search alpine
docker search alpine --filter "is-official=true"

# docker pull <repository> #pulls repository with defaults 'latest' tag
# docker pull <repository>:<tag>

docker pull hello-world
docker run hello-world

docker history ubuntu:latest
docker inspect ubuntu:latest #shows image meta-data and layer-data (configuration and runtime info)
docker manifest inspect ubuntu

docker pull ubuntu:latest
# docker run <image> <app> #uses the image to run the app, container runs until app exits
docker run -it ubuntu:latest /bin/bash #-it connects terminal window to container shell
docker run  -v ./content:/content -w /content -i -t  ubuntu pwd
docker run --name ubuntu --rm -it -v /mnt/NTFS:/mnt/NTFS -u muthu -w /home/muthu intellibitz/docker:ubuntu-latest

docker pull nigelpoulton/tu-demo:v2 #pulls repository of 'nigelpoulton' (notice username prefix before repository)
docker pull intellibitz/intellibitz-repo:alpha

docker pull gcr.io/google-containers/git-sync:v3.1.5 #pulls from 3rd party gcr.io registry

ctrl-pq #exits container, without killing it
docker ps #lists container in running state
docker ps -a # lists all container in running and stopped state
docker ps -aq #lists all container id

# docker exec <options> <container-name or container-id> <command/app>
# docker exec -it [<container_id> or <container_name>] bash

# docker stop <container-name or container-id> #stops container, SIGTERM, SIGKILL after 10s
# docker start <container-name or container-id> #restarts a stopped container
# docker rm <container-name or container-id> #deletes a stopped container

# docker build -t <image>
# docker build -t <repo:tag> -f <docker-file> <build-context-dir>
# docker build -t <repo:tag> <build-context-dir>
docker build -t test:latest .

# docker history <repo:tag> #lists the instructions used to build the image

# docker tag <current-tag> <new-tag>
# docker tag <repo:tag> <user>/<repo:tag>
docker tag web:app1 intellibitz/web:app1
docker tag 0e5574283393 fedora/httpd:version1.0
docker tag httpd fedora/httpd:version1.0
docker tag httpd:test fedora/httpd:version1.0.test
docker tag 0e5574283393 myregistryhost:5000/fedora/httpd:version1.0

# docker run -d --name <app-name> --publish <hostport:containerport> <image>
# docker run -d --name <app-name> --publish <port:port> <repo:tag>
docker run -d --name web1 --publish 8080:8080 test:latest

docker pull alpine:latest
docker pull mcr.microsoft.com/powershell:latest

docker image rm
docker image remove
docker rmi #will remove tag only, if image has multiple tags
# docker rmi <image-id> #deletes image (container must not be running or in stopped state)
docker rmi 44dd6f223004
docker rmi $(docker images -q) #deletes all images
docker rmi $(docker ps -aq) -f #deletes all containers without warning

docker run -it alpine:latest sleep 5 #runs container and invokes sleep command
docker run --name neversaydie -it --restart always alpine sh
docker run --name neversaydie-unlessstopped -it --restart unless-stopped alpine sh

docker run -d --name webserver -p 80:8080 nigelpoulton/ddd-book:web0.1 #runs app in daemon mode (note: -d flag)

docker tag hello-world:latest intellibitz/docker:hello-world
# docker login -u <username> -p <password>
docker push intellibitz/docker:hello-world #The push refers to repository [docker.io/intellibitz/docker]

docker buildx version
docker buildx build --platform linux/arm/v7 -t myimage:arm-v7 .
docker buildx create --driver=docker-container --name=container
docker buildx build --builder=container --platform=linux/amd64,linux/arm64,linux/arm/v7 \
# -t <user>/<repo>:<tag> --push .

docker compose version
compose.yaml
Dockerfile
docker compose up & #builds or pulls all required images, creates all required containers, networks and volumes, and starts all required containers (default compose.yaml or compose.yml used)
# docker compose -f <compose-file-name> up &
docker compose -f my-compose.yml up &

docker compose up --detach #starts all containers in background

docker compose ps #lists each container in the compose app, commands and networks
docker compose top #lists processes running inside of each service (container)

docker compose ls
docker network ls
docker volume ls
# docker volume inspect <volume-name>

docker compose rm #deletes stopped compose containers and networks, will not delete volumes and images by default
docker compose stop #stops without deleting resources
docker compose restart #restarts stopped compose app, for any new changes to reflect, redeploy
docker compose down #stops and deletes running compose app, containers and networks
docker compose down --volumes --rmi all #stops and deletes containers, networks and any volumes and images
docker compose logs -f
docker compose logs -f postgres
docker compose logs -f pgadmin

# docker swarm init --advertise-addr <ip>:<port> --listen-addr <ip>:<port> #creates new swarm
docker swarm init --advertise-addr 10.0.0.1:2377 --listen-addr 10.0.0.1:2377

docker node ls #lists node in swarm

