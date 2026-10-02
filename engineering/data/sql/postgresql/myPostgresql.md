#

https://www.postgresql.org/
https://www.postgresql.org/download/linux/ubuntu/
https://wiki.postgresql.org/wiki/Apt
https://ubuntu.com/server/docs/databases-postgresql

#

https://www.postgresql.org/ftp/source/
https://ftp.postgresql.org/pub/source/v16.2/postgresql-16.2.tar.bz2
https://ftp.postgresql.org/pub/source/v16.2/postgresql-16.2.tar.bz2.md5

#

References
--

    • PostgreSQL release notes: https://www.postgresql.org/docs/16/release-16.html
    • Upgrading documentation: https://www.postgresql.org/docs/current/upgrading.
    html
    • PostgreSQL version policy: https://www.postgresql.org/support/versioning/
    • PostgreSQL initdb official documentation: https://www.postgresql.org/docs/
    current/app-initdb.html
    • PostgreSQL pg_ctl official documentation: https://www.postgresql.org/docs/
    current/app-pg-ctl.html
    • pgenv GitHub repository and documentation: https://github.com/theory/pgenv

PostgreSQL is split across several components to install:
--

    • The PostgreSQL server is the part that can serve your databases to applications and users
    and is required to store your data.
    • The PostgreSQL client is the library and client tool to connect to the database server. It
    is not required if you don’t need to connect to the database on the very same machine,
    while it is required on client machines.
    • The PostgreSQL contrib package is a set of well-known extensions and utilities that can
    enhance your PostgreSQL experience. This additional package is developed by the PGDG
    and is therefore well integrated and stable.
    • The PostgreSQL docs is the documentation (e.g., man pages) related to the server and
    the client.
    • PostgreSQL PL/Perl, PL/Python, and PL/Tcl are three components to allow the usage of
    programming languages— Perl, Python, and Tcl, respectively—directly within the PostgreSQL
    server.

The recommended set of components is the server, the client, and the contrib modules;

Installing PostgreSQL from sources
--

Installing PostgreSQL from sources requires downloading a tarball, which is a compressed package
with all the source code files, and starting the compilation. Usually, this takes several minutes,
depending on the power of the machine and the I/O bandwidth. In order to compile PostgreSQL
from source, you will need different tools and libraries and mainly a C compiler compliant with
the C99 standard (or higher). Usually, you already have these tools on a Linux or Unix system;
otherwise, please refer to your operating system documentation on how to install these tools.
Once you have all the dependencies installed, follow the steps given here to compile and install
PostgreSQL:

The very first step is to download the PostgreSQL tarball related to the version you want
to install, verifying that it is correct. For instance, to download version 16.0, you can do
the following:

    $ wget https://ftp.postgresql.org/pub/source/v16.0/postgresql-
    16.0.tar.bz2
    ...
    $ wget https://ftp.postgresql.org/pub/source/v16.0/postgresql-
    16.0.tar.bz2.md5

Before starting the compilation, check that the downloaded tarball is intact:

    $ md5sum --check postgresql-16.0.tar.bz2.md5
    postgresql-16.0.tar.bz2: OK

Once you are sure that the downloaded tarball is not corrupt, you can extract its content
and start the compilation (please consider that the extracted archive will take around 200
MB of disk space, and the compilation will take up some extra space):

    $ tar xjvf postgresql-16.0.tar.bz2
    $ cd postgresql-16.0
    $ ./configure --prefix=/usr/local
    $ make && sudo make install

If you want or need the systemd(1) service file, add the --with-systemd option to the
configure line.

Once the database has been installed, you need to create a user to run the database with,
usually named postgres, and initialize the database directory:

    $ sudo useradd postgres
    $ sudo mkdir -p /postgres/16/data
    $ sudo chown -R postgres:postgres /postgres/16
    $ /usr/local/bin/initdb -D /postgres/16/data

The PostgreSQL Global Development Group (PGDG) maintains an APT repository of PostgreSQL packages for Debian and Ubuntu located at https://apt.postgresql.org/pub/repos/apt/

Installing PostgreSQL via pgenv
--

pgenv is a nice and small tool that allows you to download and manage several instances of dif-
ferent versions of PostgreSQL on the same machine. The idea behind pgenv is to let you explore
different PostgreSQL versions—for instance, to test your application against different major
versions. pgenv does not aim to be an enterprise-class tool to manage in-production instances;
rather, it is a tool to let developers and DBAs experiment with different versions of PostgreSQL
and keep them under control easily.
Of course, being an external tool, pgenv must be installed before it can be used. The installation,
however, is very simple, since the application is made by a single Bash script.
The fastest way to get pgenv installed is to clone the GitHub repository and set the PATH environ-
ment variable to point to the executable directory, as follows:

    $ git clone https://github.com/theory/pgenv
    $ export PATH=$PATH:./pgenv/bin

Now, the pgenv command is at your fingertips, and you can run the command to get a help prompt
and see the available commands.

The idea behind pgenv is pretty simple: it is a tool to automate the “boring” stuff—that is, down-
loading, compiling, installing, and starting/stopping a cluster. In order to let pgenv manage a
specific instance, you have to “use” it. When you use an instance, pgenv detects whether the
instance has been initialized or not, and in the latter case, it does the initialization for you.
In order to install versions 16.0 and 15.1 of PostgreSQL, you simply have to run the following
commands:

    $ pgenv build 16.0
    $ pgenv build 15.1

The preceding commands will download and compile the two versions of PostgreSQL, and the
time required for the operations to complete depends on the power and speed of the machine
you are running on. After that, you can decide which instance to start with the use command:

    $ pgenv use 16.0

pgenv is smart enough to see whether the instance you are starting has already been initialized,
or it will initialize it (only the first time) for you.
If you need to stop and change the PostgreSQL version to use, you can issue a stop command
followed by a use command with the targeted version. For instance, to stop running the 16.0
instance and start a 15.1 instance, you can use the following:

    $ pgenv stop
    $ pgenv use 15.1

The pgenv tool provides a lot of other commands to get information about which PostgreSQL
versions are installed, what is executing (if any), and so on.
If you are searching for a quick way to test and run different PostgreSQL versions on the same
machine, pgenv is a good tool.

On Docker
--

https://hub.docker.com/_/postgres

https://github.com/docker-library/docs/tree/master/postgres/README.md

How to use this image

start a postgres instance
--

    $ docker run --name some-postgres -e POSTGRES_PASSWORD=mysecretpassword -d postgres

The default postgres user and database are created in the entrypoint with initdb.

The postgres database is a default database meant for use by users, utilities and third party applications.

... or via psql
--

    $ docker run -it --rm --network some-network postgres psql -h some-postgres -U postgres
    psql (14.3)
    Type "help" for help.

    postgres=# SELECT 1;
    ?column?
    ----------
            1
    (1 row)

... via docker-compose or docker stack deploy

Example docker-compose.yml for postgres:
--

    #Use postgres/example user/password credentials
    services:

    db:
        image: postgres
        restart: always
        # set shared memory limit when using docker-compose
        shm_size: 128mb
        # or set shared memory limit when deploy via swarm stack
        #volumes:
        #  - type: tmpfs
        #    target: /dev/shm
        #    tmpfs:
        #      size: 134217728 # 128*2^20 bytes = 128Mb
        environment:
        POSTGRES_PASSWORD: example

    adminer:
        image: adminer
        restart: always
        ports:
        - 8080:8080

Run

    docker stack deploy -c stack.yml postgres

(or)

    docker-compose -f stack.yml up

, wait for it to initialize completely, and visit

http://swarm-ip:8080,
http://localhost:8080, or
http://host-ip:8080 (as appropriate).

Another way:

To run PostgreSQL in a Docker container on Ubuntu, you can follow these steps:

First, pull the PostgreSQL Docker Official Image from Docker Hub. You can do this by running the following command in your terminal1:

    docker pull postgres

After pulling the image, you can start a PostgreSQL container using the docker run command2. Replace <password> with your desired password:

    docker run -d --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=<password> -v postgres:/var/lib/postgresql/data postgres:14

In this command:

    -d runs the container in detached mode (in the background).
    --name postgres assigns the name “postgres” to the container.
    -p 5432:5432 maps port 5432 inside Docker as port 5432 on the host machine.
    -e POSTGRES_PASSWORD=<password> sets the environment variable POSTGRES_PASSWORD to your chosen password.
    -v postgres:/var/lib/postgresql/data creates a Docker volume named “postgres” and mounts it to /var/lib/postgresql/data in the container.
    postgres:14 is the name and tag of the Docker image to use2.

Please replace <password> with your actual password. Remember to keep your password secure.

Now, PostgreSQL should be running in a Docker container on your Ubuntu system, and you should be able to interact with it on port 54323.

How to extend this image
--

There are many ways to extend the postgres image. Without trying to support every possible use case, here are just a few that we have found useful.

Environment Variables
--

The PostgreSQL image uses several environment variables which are easy to miss. The only variable required is POSTGRES_PASSWORD, the rest are optional.

Warning: the Docker specific variables will only have an effect if you start the container with a data directory that is empty; any pre-existing database will be left untouched on container startup.

POSTGRES_PASSWORD
--

This environment variable is required for you to use the PostgreSQL image. It must not be empty or undefined. This environment variable sets the superuser password for PostgreSQL. The default superuser is defined by the POSTGRES_USER environment variable.

Docker Secrets
--

As an alternative to passing sensitive information via environment variables, _FILE may be appended to some of the previously listed environment variables, causing the initialization script to load the values for those variables from files present in the container. In particular, this can be used to load passwords from Docker secrets stored in /run/secrets/<secret_name> files. For example:

    $ docker run --name some-postgres -e POSTGRES_PASSWORD_FILE=/run/secrets/postgres-passwd -d postgres

Currently, this is only supported for POSTGRES_INITDB_ARGS, POSTGRES_PASSWORD, POSTGRES_USER, and POSTGRES_DB.

Quickstart
--

    sudo apt install -y postgresql-common
    sudo /usr/share/postgresql-common/pgdg/apt.postgresql.org.sh

Import the repository key from https://www.postgresql.org/media/keys/ACCC4CF8.asc:

    sudo apt install curl ca-certificates
    sudo install -d /usr/share/postgresql-common/pgdg
    sudo curl -o /usr/share/postgresql-common/pgdg/apt.postgresql.org.asc
        --fail https://www.postgresql.org/media/keys/ACCC4CF8.asc

Create /etc/apt/sources.list.d/pgdg.list. The distributions are called codename-pgdg. In the example, replace bookworm with the actual distribution you are using. File contents:

    deb [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc]
        https://apt.postgresql.org/pub/repos/apt bookworm-pgdg main

(You may determine the codename of your distribution by running lsb_release -c). For a script version of the above file creation, presuming you are using a supported release:

    sudo sh -c 'echo "deb
        [signed-by=/usr/share/postgresql-common/pgdg/apt.postgresql.org.asc]
        https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main"
        > /etc/apt/sources.list.d/pgdg.list'

Finally, update the package lists, and start installing packages:

    sudo apt update
    sudo apt install postgresql-15

The above does not add the sources repo (deb-src) commented out; if you need source packages, you will need to modify /etc/apt/sources.list.d/pgdg.list to add it.

This repository provides "postgresql" and "postgresql-client" meta-packages that depend on the latest postgresql-xy, ... packages, similar to the ones present in Debian and Ubuntu. Once a new PostgreSQL version is released, these meta-packages will be updated to depend on the new version. If you rather want to stay with a particular PostgreSQL version, you should install specific packages like "postgresql-15" instead of "postgresql".

To use the apt repository, follow these steps:

    sudo sh -c 'echo
        "deb https://apt.postgresql.org/pub/repos/apt
            $(lsb_release -cs)-pgdg main"
        > /etc/apt/sources.list.d/pgdg.list'
    wget --quiet -O -
        https://www.postgresql.org/media/keys/ACCC4CF8.asc
        | sudo apt-key add -
    sudo apt-get update
    sudo apt-get -y install postgresql

Debian and Ubuntu provide their own command to control the cluster, pg_ctlcluster(1). The
rationale for that is that on a Debian/Ubuntu operating system, every PostgreSQL version is
installed in its own directory with separate configuration files, so there is a way to run different
versions concurrently and manage them via the operating system. For example, configuration
files are under the /etc/postgresql/16/main directory, while the data directory is set by default
to /var/lib/postgresql/16/main.
If you want to enable PostgreSQL at boot time, you need to run the following command:

    $ sudo update-rc.d postgresql enable

In order to start your cluster, you can use
follows:
the service(1) command as

    $ sudo service postgresql start

$ sudo pg_ctlcluster 16 main status

    pg_ctl: server is running (PID: 62721)
        /usr/lib/postgresql/16/bin/postgres
        "-D" "/var/lib/postgresql/16/main"
        "-c" "config_file=/etc/postgresql/16/main/postgresql.conf"

$ ls /usr/lib/postgresql/16

    bin  lib

By default only connections from the local system are allowed, to enable all other computers to connect to your PostgreSQL server, edit the file /etc/postgresql/*/main/postgresql.conf. Locate the line: #listen_addresses = ‘localhost’ and change it to *:

listen_addresses = '*'

    Note:
    ‘*’ will allow all available IP interfaces (IPv4 and IPv6), to only listen for IPv4 set ‘0.0.0.0’ while ‘::’ allows listening for all IPv6 addresses.

$ ls /var/lib/postgresql/16

    main

$ ls /etc/postgresql/16

    main

By default only connections from the local system are allowed, to enable all other computers to connect to your PostgreSQL server, edit the file /etc/postgresql/*/main/postgresql.conf. Locate the line: #listen_addresses = ‘localhost’ and change it to *:

    listen_addresses = '*'

    Note:
    ‘*’ will allow all available IP interfaces (IPv4 and IPv6), to only listen for IPv4 set ‘0.0.0.0’ while ‘::’ allows listening for all IPv6 addresses.

Now that we can connect to our PostgreSQL server, the next step is to set a password for the postgres user. Run the following command at a terminal prompt to connect to the default PostgreSQL template database:

    sudo -u postgres psql template1

The above command connects to PostgreSQL database template1 as user postgres. Once you connect to the PostgreSQL server, you will be at an SQL prompt. You can run the following SQL command at the psql prompt to configure the password for the user postgres.

    ALTER USER postgres with encrypted password 'your_password';

A connection string in LibPQ is a URI made up of several parts:

    postgresql://username@host:port/database

The syntax of pg_hba.conf

The pg_hba.conf file contains the firewall for incoming connections. Every line within the file
has the following structure:

    <connection-type> <database> <role> <remote-machine> <auth-method>

The authentication method trust should never be used; it allows any role to connect
to the database if the Host-Based-Access (HBA) has a rule that matches the
incoming connection. This is the method that is used when the cluster is initialized
in order to enable the freshly created superuser to connect to the cluster. You can
always use this trick as a last resort if you get yourself locked out of your own cluster.

    host    all       luca      carmensita        scram-sha-256
    hostssl all       test      192.168.222.1/32  scram-sha-256
    host    digikamdb pgwatch2  192.168.222.4/32  trust
    host    digikamdb enrico    carmensita        reject

Order of rules in pg_hba.conf
The order by which the rules are listed in the pg_hba.conf file matters. The first rule that satisfies
the logic is applied, and the others are skipped.

    host forumdb luca all reject
    host all luca all scram-sha-256

In this way, when luca tries to connect to a database, he gets rejected if the database is forumdb;
otherwise, he can connect (if he passes the required authentication method).

Merging multiple rules into a single one

    host forumdb,learnpgdb luca, enrico samenet scram-sha-256

The pg_hba.conf rules, when applied to a group name (that is, with the + preceding the role
name) include all the direct and indirect members.

    host forumdb +book_authors all scram-sha-256

Remembering that the rule engine stops at the first match, it is possible to place a reject rule before the group
acceptance rule.

    host forumdb luca all reject
    host forumdb +book_authors all scram-sha-256

The first line will prevent the luca role from connecting, even if the following one allows every
member of the book_authors (including luca) to connect: the first match wins and so luca is
locked out of the database.

Using files instead of single roles
The role field of a rule can also be specified as a text file, either line- or comma-separated.

If you specify the role field with an “at” sign prefix (@), the name is interpreted as a line-separated
text file (as a relative name to the PGDATA directory).

    host forumdb @rejected_users.txt all reject
    host forumdb @allowed_users.txt all scram-sha-256

    $ sudo cat $PGDATA/rejected_users.txt
    luca
    enrico
    $ sudo cat $PGDATA/allowed_users.txt
    +book_authors, postgres

As you can see, it is possible to specify the file contents as either a line-separated list or a comma-
separated list of usernames. It is also possible to specify which roles to use as a group by
placing a + sign in front of the role name.

Inspecting pg_hba.conf rules
The pg_hba.conf file contains the rules applied to the incoming connections, but since this file
could be changed manually without making the cluster reload it, how can you be sure of which
rules are applied at the moment? PostgreSQL provides a special catalog named pg_hba_file_rules
that shows which rules have been applied to the cluster.

Including other files in pg_hba.conf
It is possible to include other HBA configuration files into the main pg_hba.conf file. PostgreSQL
provides three main directives:

    • include_file includes a specific file in pg_hba.conf
    • include_if_exist includes a specific file but only if it exist; if it does not exist (or was

removed), no error will occur

    • include_dir includes all files specified in the given directory

Thanks to this directive, it is possible to define a set of small configuration files that will be included
literally in the HBA configuration as if the administrator had edited the pg_hba.conf file directly.
In order to understand where a specific rule comes from, the pg_hba_file_rules catalog includes
a file_name column that reports from which file (and at which line, thanks to line_number) a
rule has been parsed.

Environment Variables
--

The PostgreSQL image uses several environment variables which are easy to miss. The only variable required is POSTGRES_PASSWORD, the rest are optional.

Warning: the Docker specific variables will only have an effect if you start the container with a data directory that is empty; any pre-existing database will be left untouched on container startup.

POSTGRES_PASSWORD
This environment variable is required for you to use the PostgreSQL image. It must not be empty or undefined. This environment variable sets the superuser password for PostgreSQL. The default superuser is defined by the POSTGRES_USER environment variable.

Note 1: The PostgreSQL image sets up trust authentication locally so you may notice a password is not required when connecting from localhost (inside the same container). However, a password will be required if connecting from a different host/container.

Note 2: This variable defines the superuser password in the PostgreSQL instance, as set by the initdb script during initial container startup. It has no effect on the PGPASSWORD environment variable that may be used by the psql client at runtime, as described at https://www.postgresql.org/docs/14/libpq-envars.html. PGPASSWORD, if used, will be specified as a separate environment variable.

POSTGRES_USER
This optional environment variable is used in conjunction with POSTGRES_PASSWORD to set a user and its password. This variable will create the specified user with superuser power and a database with the same name. If it is not specified, then the default user of postgres will be used.

Be aware that if this parameter is specified, PostgreSQL will still show The files belonging to this database system will be owned by user "postgres" during initialization. This refers to the Linux system user (from /etc/passwd in the image) that the postgres daemon runs as, and as such is unrelated to the POSTGRES_USER option. See the section titled "Arbitrary --user Notes" for more details.

POSTGRES_DB
This optional environment variable can be used to define a different name for the default database that is created when the image is first started. If it is not specified, then the value of POSTGRES_USER will be used.

POSTGRES_INITDB_ARGS
This optional environment variable can be used to send arguments to postgres initdb. The value is a space separated string of arguments as postgres initdb would expect them. This is useful for adding functionality like data page checksums: -e POSTGRES_INITDB_ARGS="--data-checksums".

POSTGRES_INITDB_WALDIR
This optional environment variable can be used to define another location for the Postgres transaction log. By default the transaction log is stored in a subdirectory of the main Postgres data folder (PGDATA). Sometimes it can be desireable to store the transaction log in a different directory which may be backed by storage with different performance or reliability characteristics.

Note: on PostgreSQL 9.x, this variable is POSTGRES_INITDB_XLOGDIR (reflecting the changed name of the --xlogdir flag to --waldir in PostgreSQL 10+).

POSTGRES_HOST_AUTH_METHOD
This optional variable can be used to control the auth-method for host connections for all databases, all users, and all addresses. If unspecified then scram-sha-256 password authentication is used (in 14+; md5 in older releases). On an uninitialized database, this will populate pg_hba.conf via this approximate line:

echo "host all all all $POSTGRES_HOST_AUTH_METHOD" >> pg_hba.conf
See the PostgreSQL documentation on pg_hba.conf for more information about possible values and their meanings.

Note 1: It is not recommended to use trust since it allows anyone to connect without a password, even if one is set (like via POSTGRES_PASSWORD). For more information see the PostgreSQL documentation on Trust Authentication.

Note 2: If you set POSTGRES_HOST_AUTH_METHOD to trust, then POSTGRES_PASSWORD is not required.

Note 3: If you set this to an alternative value (such as scram-sha-256), you might need additional POSTGRES_INITDB_ARGS for the database to initialize correctly (such as POSTGRES_INITDB_ARGS=--auth-host=scram-sha-256).

PGDATA
Important Note: when mounting a volume to /var/lib/postgresql, the /var/lib/postgresql/data path is a local volume from the container runtime, thus data is not persisted on the mounted volume.

This optional variable can be used to define another location - like a subdirectory - for the database files. The default is /var/lib/postgresql/data. If the data volume you're using is a filesystem mountpoint (like with GCE persistent disks), or remote folder that cannot be chowned to the postgres user (like some NFS mounts), or contains folders/files (e.g. lost+found), Postgres initdb requires a subdirectory to be created within the mountpoint to contain the data.

For example:

$ docker run -d \
--name some-postgres \
-e POSTGRES_PASSWORD=mysecretpassword \
-e PGDATA=/var/lib/postgresql/data/pgdata \
-v /custom/mount:/var/lib/postgresql/data \
postgres
This is an environment variable that is not Docker specific. Because the variable is used by the postgres server binary (see the PostgreSQL docs), the entrypoint script takes it into account.

Initialization scripts
If you would like to do additional initialization in an image derived from this one, add one or more *.sql, *.sql.gz, or *.sh scripts under /docker-entrypoint-initdb.d (creating the directory if necessary). After the entrypoint calls initdb to create the default postgres user and database, it will run any *.sql files, run any executable *.sh scripts, and source any non-executable *.sh scripts found in that directory to do further initialization before starting the service.

Warning: scripts in /docker-entrypoint-initdb.d are only run if you start the container with a data directory that is empty; any pre-existing database will be left untouched on container startup. One common problem is that if one of your /docker-entrypoint-initdb.d scripts fails (which will cause the entrypoint script to exit) and your orchestrator restarts the container with the already initialized data directory, it will not continue on with your scripts.

For example, to add an additional user and database, add the following to /docker-entrypoint-initdb.d/init-user-db.sh:

#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
CREATE USER docker;
CREATE DATABASE docker;
GRANT ALL PRIVILEGES ON DATABASE docker TO docker;
EOSQL
These initialization files will be executed in sorted name order as defined by the current locale, which defaults to en_US.utf8. Any *.sql files will be executed by POSTGRES_USER, which defaults to the postgres superuser. It is recommended that any psql commands that are run inside of a *.sh script be executed as POSTGRES_USER by using the --username "$POSTGRES_USER" flag. This user will be able to connect without a password due to the presence of trust authentication for Unix socket connections made inside the container.

Additionally, as of docker-library/postgres#253, these initialization scripts are run as the postgres user (or as the "semi-arbitrary user" specified with the --user flag to docker run; see the section titled "Arbitrary --user Notes" for more details). Also, as of docker-library/postgres#440, the temporary daemon started for these initialization scripts listens only on the Unix socket, so any psql usage should drop the hostname portion (see docker-library/postgres#474 (comment) for example).

Database Configuration
There are many ways to set PostgreSQL server configuration. For information on what is available to configure, see the PostgreSQL docs for the specific version of PostgreSQL that you are running. Here are a few options for setting configuration:

Use a custom config file. Create a config file and get it into the container. If you need a starting place for your config file you can use the sample provided by PostgreSQL which is available in the container at /usr/share/postgresql/postgresql.conf.sample (/usr/local/share/postgresql/postgresql.conf.sample in Alpine variants).

Important note: you must set listen_addresses = '*'so that other containers will be able to access postgres.
$ # get the default config
$ docker run -i --rm postgres cat /usr/share/postgresql/postgresql.conf.sample > my-postgres.conf

$ # customize the config

$ # run postgres with custom config
$ docker run -d --name some-postgres -v "$PWD/my-postgres.conf":/etc/postgresql/postgresql.conf -e POSTGRES_PASSWORD=mysecretpassword postgres -c 'config_file=/etc/postgresql/postgresql.conf'
Set options directly on the run line. The entrypoint script is made so that any options passed to the docker command will be passed along to the postgres server daemon. From the PostgreSQL docs we see that any option available in a .conf file can be set via -c.

$ docker run -d --name some-postgres -e POSTGRES_PASSWORD=mysecretpassword postgres -c shared_buffers=256MB -c max_connections=200
Locale Customization
You can extend the Debian-based images with a simple Dockerfile to set a different locale. The following example will set the default locale to de_DE.utf8:

FROM postgres:14.3
RUN localedef -i de_DE -c -f UTF-8 -A /usr/share/locale/locale.alias de_DE.UTF-8
ENV LANG de_DE.utf8
Since database initialization only happens on container startup, this allows us to set the language before it is created.

Also of note, Alpine-based variants starting with Postgres 15 support ICU locales. Previous Postgres versions based on alpine do not support locales; see "Character sets and locale" in the musl documentation for more details.

You can set locales in the Alpine-based images with POSTGRES_INITDB_ARGS to set a different locale. The following example will set the default locale for a newly initialized database to de_DE.utf8:

$ docker run -d -e LANG=de_DE.utf8 -e POSTGRES_INITDB_ARGS="--locale-provider=icu --icu-locale=de-DE" -e POSTGRES_PASSWORD=mysecretpassword postgres:15-alpine

Arbitrary --user Notes
As of docker-library/postgres#253, this image supports running as a (mostly) arbitrary user via --user on docker run. As of docker-library/postgres#1018, this is also the case for the Alpine variants.

The main caveat to note is that postgres doesn't care what UID it runs as (as long as the owner of /var/lib/postgresql/data matches), but initdb does care (and needs the user to exist in /etc/passwd):

$ docker run -it --rm --user www-data -e POSTGRES_PASSWORD=mysecretpassword postgres
The files belonging to this database system will be owned by user "www-data".
...

$ docker run -it --rm --user 1000:1000 -e POSTGRES_PASSWORD=mysecretpassword postgres
initdb: could not look up effective user ID 1000: user does not exist
The three easiest ways to get around this:

allow the image to use the nss_wrapper library to "fake" /etc/passwd contents for you (see docker-library/postgres#448 for more details)

bind-mount /etc/passwd read-only from the host (if the UID you desire is a valid user on your host):

$ docker run -it --rm --user "$(id -u):$(id -g)" -v /etc/passwd:/etc/passwd:ro -e POSTGRES_PASSWORD=mysecretpassword postgres
The files belonging to this database system will be owned by user "jsmith".
...
initialize the target directory separately from the final runtime (with a chown in between):

Arbitrary --user Notes
--

As of docker-library/postgres#253, this image supports running as a (mostly) arbitrary user via --user on docker run. As of docker-library/postgres#1018, this is also the case for the Alpine variants.

The main caveat to note is that postgres doesn't care what UID it runs as (as long as the owner of /var/lib/postgresql/data matches), but initdb does care (and needs the user to exist in /etc/passwd):

$ docker run -it --rm --user www-data -e POSTGRES_PASSWORD=mysecretpassword postgres
The files belonging to this database system will be owned by user "www-data".
...

$ docker run -it --rm --user 1000:1000 -e POSTGRES_PASSWORD=mysecretpassword postgres
initdb: could not look up effective user ID 1000: user does not exist
The three easiest ways to get around this:

allow the image to use the nss_wrapper library to "fake" /etc/passwd contents for you (see docker-library/postgres#448 for more details)

bind-mount /etc/passwd read-only from the host (if the UID you desire is a valid user on your host):

$ docker run -it --rm --user "$(id -u):$(id -g)" -v /etc/passwd:/etc/passwd:ro -e POSTGRES_PASSWORD=mysecretpassword postgres
The files belonging to this database system will be owned by user "jsmith".
...
initialize the target directory separately from the final runtime (with a chown in between):

$ docker volume create pgdata
$ docker run -it --rm -v pgdata:/var/lib/postgresql/data -e POSTGRES_PASSWORD=mysecretpassword postgres
The files belonging to this database system will be owned by user "postgres".
...
( once it's finished initializing successfully and is waiting for connections, stop it )
$ docker run -it --rm -v pgdata:/var/lib/postgresql/data bash chown -R 1000:1000 /var/lib/postgresql/data
$ docker run -it --rm --user 1000:1000 -v pgdata:/var/lib/postgresql/data postgres
LOG: database system was shut down at 2017-01-20 00:03:23 UTC
LOG: MultiXact member wraparound protections are now enabled
LOG: autovacuum launcher started
LOG: database system is ready to accept connections

Caveats
--

If there is no database when postgres starts in a container, then postgres will create the default database for you. While this is the expected behavior of postgres, this means that it will not accept incoming connections during that time. This may cause issues when using automation tools, such as docker-compose, that start several containers simultaneously.

Also note that the default /dev/shm size for containers is 64MB. If the shared memory is exhausted you will encounter ERROR: could not resize shared memory segment . . . : No space left on device. You will want to pass --shm-size=256MB for example to docker run, or alternatively in docker-compose.

Where to Store Data
--

Important note: There are several ways to store data used by applications that run in Docker containers. We encourage users of the postgres images to familiarize themselves with the options available, including:

Let Docker manage the storage of your database data by writing the database files to disk on the host system using its own internal volume management. This is the default and is easy and fairly transparent to the user. The downside is that the files may be hard to locate for tools and applications that run directly on the host system, i.e. outside containers.
Create a data directory on the host system (outside the container) and mount this to a directory visible from inside the container. This places the database files in a known location on the host system, and makes it easy for tools and applications on the host system to access the files. The downside is that the user needs to make sure that the directory exists, and that e.g. directory permissions and other security mechanisms on the host system are set up correctly.
The Docker documentation is a good starting point for understanding the different storage options and variations, and there are multiple blogs and forum postings that discuss and give advice in this area. We will simply show the basic procedure here for the latter option above:

Create a data directory on a suitable volume on your host system, e.g. /my/own/datadir.

Start your postgres container like this:

$ docker run --name some-postgres -v /my/own/datadir:/var/lib/postgresql/data -e POSTGRES_PASSWORD=mysecretpassword -d postgres:tag
The -v /my/own/datadir:/var/lib/postgresql/data part of the command mounts the /my/own/datadir directory from the underlying host system as /var/lib/postgresql/data inside the container, where PostgreSQL by default will write its data files.

Terms
--

    Cluster: the whole PostgreSQL service.

    Postmaster: the first process the cluster executes, and this process is responsible for keeping
    track of the activities of the whole cluster. The postmaster spawns a backend process
    every time a new connection is established.

    Database: an isolated data container to which users (or applications) can connect. A
    cluster can handle multiple databases. A database can be made up of different objects,
    including schemas (namespaces), tables, triggers, and other objects you will see as the
    book progresses.

    PGDATA: the directory that, on persistent storage, is fully dedicated to PostgreSQL and
    its data. PostgreSQL stores the data within such a directory.

    WALs: the intent log of database changes, used to recover data from a critical crash.

pg_ctl
--

The pg_ctl command-line utility allows you to perform different actions on a cluster, mainly
initialize, start, restart, stop, and so on. pg_ctl accepts the command to execute as the first argument,
followed by other specific arguments—the main commands are as follows:

    • start, stop, and restart execute the corresponding actions on the cluster.
    Chapter 2 23
    • status reports the current status (running or not) of the cluster.
    • initdb (or init for short) executes the initialization of the cluster, possibly removing
    any previously existing data.
    • reload causes the PostgreSQL server to reload the configuration, which is useful when
    you want to apply configuration changes.
    • promote is used when the cluster is running as a replica server (namely a standby node)
    and, from now on, must be detached from the original primary becoming independent
    (replication will be explained in later chapters).

The PGDATA directory
--

is structured in several files and subdirectories. The main files are as follows:
postgresql.conf is the main configuration file, used by default when the service is started.
postgresql.auto.conf is the automatically included configuration file used to store
dynamically changed settings via SQL instructions.
pg_hba.conf is the HBA file that provides the configuration regarding available database
connections.
PG_VERSION is a text file that contains the major version number (useful when inspecting
the directory to understand which version of the cluster has managed the PGDATA directory).
postmaster.pid is the PID of the postmaster process, the first launched process in the
cluster.

The main directories available in PGDATA are as follows:
base is a directory that contains all the users’ data, including databases, tables, and other
objects.
global is a directory containing cluster-wide objects.
pg_wal is the directory containing the WAL files.
pg_stat and pg_stat_tmp are, respectively, the storage of permanent and temporary
statistical information about the status and health of the cluster.
Of course, all files and directories in PGDATA are important for the cluster to work properly, but
so far, the preceding is the “core” list of objects that are fundamental in PGDATA itself.

From the preceding example, it should be clear that every SQL table is stored as a file with a
numeric name. However, PostgreSQL does not allow a single file to be greater than 1 GB in size,
so what happens if a table grows beyond that limit? PostgreSQL “attaches” another file with a
numeric extension that indicates the next chunk of 1 GB of data. In other words, if your table is
stored in the 123 file, the second gigabyte will be stored in the 123.1 file, and if another gigabyte
of storage is needed, another file, 123.2, will be created. Therefore, the filenode refers to the very
first file related to a specific table, but more than one file can be stored on disk.

Tablespaces
PostgreSQL pretends to find all its data within the PGDATA directory, but that does not mean that
your cluster is “jailed” in this directory. In fact, PostgreSQL allows “escaping” the PGDATA directory
by means of tablespaces. A tablespace is a directory that can be outside the PGDATA directory and
can also belong to different storage. Tablespaces are mapped into the PGDATA directory by means
of symbolic links stored in the pg_tblspc subdirectory. In this way, the PostgreSQL processes do
not have to look outside PGDATA, but are still able to access “external” storage. A tablespace can
be used to achieve different aims, such as enlarging the storage data or providing different stor-
age performances for specific objects. For instance, you can create a tablespace on a slow disk to
contain infrequently accessed objects and tables, keeping fast storage within another tablespace
for frequently accessed objects.
You don’t have to make links by yourself: PostgreSQL provides the TABLESPACE feature to manage
this and the cluster will create and manage the appropriate links under the pg_tblspc subdirectory.

Exploring configuration files and parameters
The main configuration file for PostgreSQL is postgresql.conf, a text-based file that drives the
cluster when it starts.
Every configuration parameter is associated with a context, and depending on the context, you
can apply changes with or without a cluster restart. Available contexts are as follows:
internal: A group of parameters that are set at compile time and therefore cannot be
changed at runtime.
postmaster: All the parameters that require the cluster to be restarted (that is, to kill the
postmaster process and start it again) to activate them.
sighup: All the configuration parameters that can be applied with a SIGHUP signal sent to
the postmaster process, which is equivalent to issuing a reload signal in the operating
system service manager.
backend and superuser-backend: All the parameters that can be set at runtime but will
be applied to the next normal or administrative connection.
user and superuser: A group of settings that can be changed at runtime and are imme-
diately active for normal and administrative connection.

    $ cat /postgres/16/data/postgresql.conf
    shared_buffers = 512MB
    maintenance_work_mem = 128MB
    checkpoint_completion_target = 0.7
    wal_buffers = 16MB
    work_mem = 32MB
    min_wal_size = 1GB
    max_wal_size = 2GB

The PostgreSQL HBA file (pg_hba.conf )
--

is another text file that contains the connection allowance:
it lists the databases, users, and networks that are allowed to connect to your cluster. The HBA
method can be thought of as a firewall embedded into PostgreSQL. As an example, the following
is an excerpt from a pg_hba.conf file:
hosts
all luca 192.168.222.1/32 md5
hostssl all enrico 192.168.222.1/32 md5
In short, the preceding lines mean that the user luca can connect to any database in the cluster
with the machine with the IPv4 address 192.168.222.1, while the user enrico can connect to
any database from the same machine but only on an SSL-encrypted connection. All the available
pg_hba.conf rules will be detailed in a later chapter, but for now, it is sufficient to know that this
file acts as a “list of firewall rules” for incoming connections.

Summary
--

PostgreSQL can handle several databases within a single cluster, served out of disk storage con-
tained in a single directory named PGDATA. The cluster runs many different processes; one, in
particular, is named postmaster and is in charge of spawning other processes, one per client
connection, and keeping track of the status of maintenance processes.
The configuration of the cluster is managed via text-based configuration files, the main one being
postgresql.conf. It is possible to filter incoming user connections by means of rules placed in
the pg_hba.conf text file.
You can interact with the cluster status by means of the pg_ctl tool or, depending on your oper-
ating system, by other provided programs, such as service or systemctl.
This chapter has presented you with the relevant information so that you are able not only to
install PostgreSQL but also to start and stop it regularly, integrate it with your operating system,
and connect to the cluster.

Introduction to users and groups
PostgreSQL distinguishes between users and groups of users: the former represents someone, either
a person or an application, that could connect to the cluster and perform activities; the latter
represents a collection of users that share some common properties, most commonly permissions
on cluster objects.
In order to connect interactively or via an application to a PostgreSQL database, you need to have
login credentials. In particular, a database user, a user who is allowed to connect to that specific
database, must exist.
Database users are somewhat similar to operating system users: they have a username and an
(encrypted) password and are known to the PostgreSQL cluster. Similarly to operating system
users, database users can be grouped into user groups in order to make their management easier.
In SQL, and therefore also in PostgreSQL, the concepts of both a single user account and a group
of accounts are encompassed by the concept of a role.

A role can be a single account, a group of accounts, or even both depending on how you design it;
however, in order to make management easier, a role should express one and only one concept
at a time: that is, it should be either a single user or a single group, but not both.
While a role can be used simultaneously as a group or a single user, we strongly
encourage you to keep the two concepts of user and group separate—it will simplify
the management of your infrastructure.
Every role must have a unique name or identifier, usually called a username.
A role represents a collection of database permissions and connection properties. The two el-
ements are orthogonal. You can set up a role simply as a container for other roles, configuring
the contained roles to hold the assigned permissions, or you can have a role that holds all the
permissions for contained roles, or mix and match these two approaches.

It is important to understand that a role is defined at the cluster level, while permissions are
defined at the database level. This means that the same role can have different privileges and
properties depending on the database it is using (for instance, being allowed to connect to one
database and not to another).
Since a role is defined at the cluster level, it must have a unique name within the
entire cluster.

Role passwords, connections, and availability
--

Every connection to PostgreSQL must be made to a specific database, no matter the user that is
opening the connection. Connecting to a database in the cluster means that the role must au-
thenticate itself, and therefore, there must be an authentication mechanism, the username and
password being the most classical ones.
When a user attempts to connect to a database, PostgreSQL checks the login credentials and a
few other properties of the user to ensure that it is allowed to log in and has valid credentials.

Using a role as a group
--

A group is a role that contains other roles. It’s that simple!
Usually, when you want to create a group, all you need to do is create a role without the LOGIN
option and then add all the members one after the other to the containing role. Adding a role to
a containing role makes the latter a group.
