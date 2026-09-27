#!/usr/bin/env sh
#https://github.com/docker-library/docs/blob/master/postgres/README.md
#https://postgresql.org/docs

docker run -d --name postgres -e POSTGRES_PASSWORD=admin -p 65432:5432 postgres #runs daemon

# The default postgres user and database are created in the entrypoint with initdb.
# The postgres database is a default database meant for use by users, utilities and third party applications.

docker run -it --rm --network container:postgres --user postgres --workdir /var/lib/postgresql postgres /bin/bash #runs shell
docker exec -it -u postgres -w /var/lib/postgresql postgres /bin/bash #runs shell
# docker run -d \
# 	--name postgres \
# 	-e SERVER_NAME=postgres \
# 	-e POSTGRES_PASSWORD=admin \
# 	-e PGDATA=/var/lib/postgresql/data/pgdata \
# 	-v /pgdata:/var/lib/postgresql/data \
# 	postgres


docker run -d -v /mnt/NTFS:/mnt/NTFS --name pg -e POSTGRES_PASSWORD=admin -p 65432:5432 postgres #runs daemon
# docker restart <container-id> #restarts daemon

docker exec -it pg /bin/bash #runs shell as root
apt-get update && apt-get install sudo procps vim file less
# touch /etc/sudoers
echo "postgres ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers
# passwd postgres #sets password to user postgres
# usermod -aG sudo postgres #adds postgres user to sudo group

docker run -it --rm --network host postgres /usr/bin/psql postgresql://postgres@localhost:65432/postgres #runs psql
docker run -it --rm --network host postgres /usr/bin/psql -h localhost -p 65432 -U postgres -d postgres #runs psql

docker run -it --rm --network host --user postgres --workdir /var/lib/postgresql postgres /bin/bash #runs shell

docker exec -it -u postgres pg /bin/bash #runs shell as user postgres
docker exec -it -u postgres -w /var/lib/postgresql pg /bin/bash #runs shell as user postgres lands in work dir

docker exec -it -u postgres pg /usr/bin/psql #runs psql
docker exec -it -u postgres pg /usr/bin/psql -l #lists databases




