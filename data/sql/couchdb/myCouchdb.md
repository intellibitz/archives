#

    https://couchdb.apache.org/

    https://docs.couchdb.org/en/stable/install/unix.html#installing

You should create a special couchdb user for CouchDB.

On many Unix-like systems you can run:

    adduser --system \
            --shell /bin/bash \
            --group --gecos \
            "CouchDB Administrator" couchdb

You must make sure that the user has a working POSIX shell and a writable home directory.

You can test this by:

Trying to log in as the couchdb user

Running pwd and checking the present working directory

As a recommendation, copy the rel/couchdb directory into /home/couchdb or /Users/couchdb.

Ex: copy the built couchdb release to the new user’s home directory:

    cp -R /path/to/couchdb/rel/couchdb /home/couchdb

Change the ownership of the CouchDB directories by running:

    chown -R couchdb:couchdb /home/couchdb

Change the permission of the CouchDB directories by running:

    find /home/couchdb -type d -exec chmod 0770 {} \;

Update the permissions for your ini files:

    chmod 0644 /home/couchdb/etc/*

https://docs.couchdb.org/en/stable/config/auth.html#config-admins
3.6.1. Server Administrators¶
[admins]¶
Changed in version 3.0.0: CouchDB requires an admin account to start. If an admin account has not been created, CouchDB will print an error message and terminate.

CouchDB server administrators and passwords are not stored in the _users database, but in the last [admins] section that CouchDB finds when loading its ini files. See :config:intro for details on config file order and behaviour. This file (which could be something like /opt/couchdb/etc/local.ini or /opt/couchdb/etc/local.d/10-admins.ini when CouchDB is installed from packages) should be appropriately secured and readable only by system administrators:

    [admins]
    ;admin = mysecretpassword
    admin = -hashed-6d3c30241ba0aaa4e16c6ea99224f915687ed8cd,7f4a3e05e0cbc6f48a0035e3508eef90
    architect = -pbkdf2-43ecbd256a70a3a2f7de40d2374b6c3002918834,921a12f74df0c1052b3e562a23cd227f,10000

Administrators can be added directly to the [admins] section, and when CouchDB is restarted, the passwords will be salted and encrypted. You may also use the HTTP interface to create administrator accounts; this way, you don’t need to restart CouchDB, and there’s no need to temporarily store or transmit passwords in plaintext. The HTTP /_node/{node-name}/_config/admins endpoint supports querying, deleting or creating new admin accounts:

    GET /_node/nonode@nohost/_config/admins HTTP/1.1
    Accept: application/json
    Host: localhost:5984
    HTTP/1.1 200 OK
    Cache-Control: must-revalidate
    Content-Length: 196
    Content-Type: application/json
    Date: Fri, 30 Nov 2012 11:37:18 GMT
    Server: CouchDB (Erlang/OTP)
    {
        "admin": "-hashed-6d3c30241ba0aaa4e16c6ea99224f915687ed8cd,7f4a3e05e0cbc6f48a0035e3508eef90",
        "architect": "-pbkdf2-43ecbd256a70a3a2f7de40d2374b6c3002918834,921a12f74df0c1052b3e562a23cd227f,10000"
    }

If you already have a salted, encrypted password string (for example, from an old ini file, or from a different CouchDB server), then you can store the “raw” encrypted string, without having CouchDB doubly encrypt it.

    PUT /_node/nonode@nohost/_config/admins/architect?raw=true HTTP/1.1
    Accept: application/json
    Content-Type: application/json
    Content-Length: 89
    Host: localhost:5984

    "-pbkdf2-43ecbd256a70a3a2f7de40d2374b6c3002918834,921a12f74df0c1052b3e562a23cd227f,10000"
    HTTP/1.1 200 OK
    Cache-Control: must-revalidate
    Content-Length: 89
    Content-Type: application/json
    Date: Fri, 30 Nov 2012 11:39:18 GMT
    Server: CouchDB (Erlang/OTP)

    "-pbkdf2-43ecbd256a70a3a2f7de40d2374b6c3002918834,921a12f74df0c1052b3e562a23cd227f,10000"

Further details are available in security, including configuring the work factor for PBKDF2, and the algorithm itself at PBKDF2 (RFC-2898).

Changed in version 1.4: PBKDF2 server-side hashed salted password support added, now as a synchronous call for the _config/admins API.

You can start the CouchDB server by running:

    sudo -i -u couchdb /home/couchdb/bin/couchdb

This uses the sudo command to run the couchdb command as the couchdb user.

When CouchDB starts it should eventually display following messages:

{database_does_not_exist,[{mem3_shards,load_shards_from_db,"_users" ...
Don’t be afraid, we will fix this in a moment.

To check that everything has worked, point your web browser to:

    http://127.0.0.1:5984/_utils/index.html

From here you should verify your installation by pointing your web browser to:

    http://localhost:5984/_utils/index.html#verifyinstall

Your installation is not complete. Be sure to complete the Setup steps for a single node or clustered installation.

    https://docs.couchdb.org/en/stable/setup/index.html#setup
    https://docs.couchdb.org/en/stable/setup/cluster.html

#
