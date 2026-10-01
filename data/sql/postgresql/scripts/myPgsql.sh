#!/usr/bin/env sh

# https://www.postgresql.org/download/linux/ubuntu/
# Ubuntu includes PostgreSQL by default. To install PostgreSQL on Ubuntu, use the apt (or other apt-driving) command:
sudo apt install postgresql

# Automated repository configuration:
sudo apt install -y postgresql-common
sudo /usr/share/postgresql-common/pgdg/apt.postgresql.org.sh

# To manually configure the Apt repository, follow these steps:
sudo apt install curl ca-certificates
sudo install -d /usr/share/postgresql-common/pgdg
sudo curl -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc --fail https://www.postgresql.org/media/keys/ACCC4CF8.asc
sudo sh -c 'echo "deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc] https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
sudo apt update
sudo apt -y install postgresql

sudo update-rc.d postgresql enable #enables postgresql at boot
sudo service postgresql start #starts the cluster

#https://github.com/docker-library/docs/blob/master/postgres/README.md
#https://postgresql.org/docs

sudo -i #root user
su - postgres #postgres user
sudo su - postgres #postgres user

postgresql://username@host:port/database #connection string format

cat $PGDATA/postgresql.conf #postgres config file
# listen_addresses = '*' #$PGDATA/postgresql.conf
cat $PGDATA/pg_hba.conf #client authentication config file
# host all postgres 127.0.0.1/32 trust #$PGDATA/pg_hba.conf

pg_ctl status -D /var/lib/postgresql/data
export PGDATA=/var/lib/postgresql/data
export EDITOR=/usr/bin/vim

# pg_ctl status|initdb|reload|promote
# pg_ctl start|stop|restart
# pg_ctl stop -m <smart|fast|immediate>

pstree -p postgres
ps -C postgres -af

psql #connected to db postgres as user postgres
psql -U postgres -d postgres

psql -l #lists databases
psql -l -h localhost -U postgres
psql -l postgresql://postgres@localhost:5432/postgres

#$PGDATA files
postgresql.conf #main configuration
postgresql.auto.conf #dynamic settings via sql instructions
pg_hba.conf #available database connections
PG_VERSION #text files contains major ersion number
postmaster.pid #process id of first launched process in cluster

#$PGDATA folders
base #users data, including databases, tables and other objects
global #cluster-wide objects
pg_wal #write ahead log files
pg_stat #permanent statistical information about status and health of cluster
pg_stat_tmp

oid2name #extracts knemonic name from filenode (oid)
oid2name -d template1 -f 3395 #inspect single specfied file in specified db
oid2name -s #shows tablespaces

cat /postgres/16/data/postgresql.conf
# shared_buffers = 512MB
# maintenance_work_mem = 128MB
# checkpoint_completion_target = 0.7
# wal_buffers = 16MB
# work_mem = 32MB
# min_wal_size = 1GB
# max_wal_size = 2GB

$ $EDITOR $PGDATA/pg_hba.conf
... modify the file as you wish ...
$ sudo -u postgres pg_ctl reload -D $PGDATA
server signaled

#It is worth noting that a superuser role can instrument the cluster to reload the configuration
#by means of an SQL statement. Calling the special function pg_reload_conf() will perform the
#same action as issuing a reload to pg_ctl:
# postgres=# SELECT pg_reload_conf();

pg_hba.conf file:
#<connection-type> <database> <role> <remote-machine> <auth-method>
# host    all       luca      carmensita        scram-sha-256
hostssl all       test      192.168.222.1/32  scram-sha-256
# host    digikamdb pgwatch2  192.168.222.4/32  trust
# host    digikamdb enrico    carmensita        reject

# postgres=# SELECT file_name, line_number, type,
database, user_name,
address, auth_method
FROM pg_hba_file_rules;

