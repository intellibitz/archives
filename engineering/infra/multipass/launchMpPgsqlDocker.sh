#!/usr/bin/env sh
# https://multipass.run/docs/use-a-blueprint
# Blueprints also provide a way of exchanging files between the host and the instance. For this, a folder named multipass/<instance name> is created in the user’s home directory on the host and mounted in <instance name> in the user’s home directory on the instance.
#https://hub.docker.com/_/postgres/
#https://github.com/docker-library/docs/blob/master/postgres/README.md
#https://postgresql.org/docs

multipass launch docker --name postgres-d --cpus 1 --memory 1G --disk 10G
multipass shell postgres-d

# docker run -d -v /mnt/NTFS:/mnt/NTFS --name pg -e POSTGRES_PASSWORD=admin -p 65432:5432 postgres #runs daemon
docker run -d -v /mnt/NTFS:/mnt/NTFS --name pg -e POSTGRES_PASSWORD=admin -p 65432:5432 intellibitz/postgres #runs daemon
# docker restart <container-id> #restarts daemon

docker run -it --rm --network host --user postgres --workdir /var/lib/postgresql intellibitz/postgres /bin/bash #runs shell
docker exec -it -u postgres pg /bin/bash #runs shell as user postgres
docker exec -it -u postgres -w /var/lib/postgresql pg /bin/bash #runs shell as user postgres lands in work dir
docker exec -it pg /bin/bash #runs shell as root

docker exec -it -u postgres pg /usr/bin/psql #runs psql
docker exec -it -u postgres pg /usr/bin/psql -l #lists databases
docker run -it --rm --network host postgres /usr/bin/psql postgresql://postgres@localhost:65432/postgres #runs psql
docker run -it --rm --network host postgres /usr/bin/psql -h localhost -p 65432 -U postgres -d postgres #runs psql




