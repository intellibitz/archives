#

    https://rqlite.io/
    https://rqlite.io/docs/
    https://raft.github.io/
    https://www.sqlite.org/index.html

    docker run -p4001:4001 rqlite/rqlite
    brew install rqlite

What’s included?
An rqlite release includes two key binaries:

    rqlited: the actual rqlite server.
    rqlite: a command-line tool, for interacting with rqlite.

Depending on the packaging process, a simple benchmarking tool, rqbench, may also be included.

Quick start
--

Get up and running quickly with rqlite
The quickest way to get running is to download a pre-built release binary, available on the GitHub releases page. Once installed, you can start a single rqlite node like so:

Starts with db created in memory
rqlited -node-id 1 node1

Start with db created on disk
rqlited -on-disk -node-id 1 node1

Once launched rqlite will be listening on http://localhost:4001.

Inserting records
Let’s insert some records using the rqlite shell, using standard SQLite commands.

    $ rqlite
    127.0.0.1:4001> CREATE TABLE foo (id INTEGER NOT NULL PRIMARY KEY, name TEXT)
    0 row affected (0.000668 sec)
    127.0.0.1:4001> .schema
    +-----------------------------------------------------------------------------+
    | sql                                                                         |
    +-----------------------------------------------------------------------------+
    | CREATE TABLE foo (id INTEGER NOT NULL PRIMARY KEY, name TEXT)               |
    +-----------------------------------------------------------------------------+
    127.0.0.1:4001> INSERT INTO foo(name) VALUES("fiona")
    1 row affected (0.000080 sec)
    127.0.0.1:4001> SELECT * FROM foo
    +----+-------+
    | id | name  |
    +----+-------+
    | 1  | fiona |
    +----+-------+

Forming a cluster
--

While not strictly necessary to run rqlite, running multiple nodes means you’ll have a fault-tolerant cluster. Start two more nodes, allowing the cluster to tolerate failure of a single node, like so:

    $ rqlited -node-id 2 -http-addr localhost:4003 -raft-addr localhost:4004 -join http://localhost:4001 node2
    $ rqlited -node-id 3 -http-addr localhost:4005 -raft-addr localhost:4006 -join http://localhost:4001 node3

This demonstration shows all 3 nodes running on the same host. In reality you probably wouldn’t do this, and then you wouldn’t need to select different -http-addr and -raft-addr ports for each rqlite node. Consult the Clustering Guide for more details.

With just these few steps you’ve now got a fault-tolerant, distributed relational database. Running on each rqlite node will be a fully-replicated copy of the SQLite database. Any data you insert will be replicated across the cluster, in a durable and fault-tolerant manner.

    https://rqlite.io/docs/api/
    https://rqlite.io/docs/clustering/

Creating a cluster
--

This section describes manually creating a cluster. If you wish rqlite nodes to automatically find other, and form a cluster, check out auto-clustering.

Let’s say you have 3 host machines, host1, host2, and host3, and that each of these hostnames resolves to an IP address reachable from the other hosts. Instead of hostnames you can use the IP address directly if you wish.

To create a cluster you must first launch a node that can act as the initial leader. Do this as follows on host1:

    host1:$ rqlited -node-id 1 -http-addr host1:4001 -raft-addr host1:4002 ~/node

With this command a single node is started, listening for API requests on port 4001 and listening on port 4002 for intra-cluster communication and cluster-join requests from other nodes. Note that the addresses passed to -http-addr and -raft-addr must be reachable from other nodes so that nodes can find each other over the network – these addresses will be broadcast to other nodes during the Join operation. If a node needs to bind to one address, but broadcast a different address, you must set -http-adv-addr and -raft-adv-addr.

-node-id can be any string, as long as it’s unique for the cluster. It also shouldn’t change, once chosen for this node. The network addresses can change however. This node stores its state at ~/node.

To join a second node to this leader, execute the following command on host2:

    host2:$ rqlited -node-id 2 -http-addr host2:4001 -raft-addr host2:4002 -join http://host1:4001 ~/node

If a node receives a join request, and that node is not actually the leader of the cluster, the receiving node will automatically redirect the requesting node to the Leader node. As a result a node can actually join a cluster by contacting any node in the cluster. You can also specify multiple join addresses, and the node will try each address until joining is successful.

Once executed you now have a cluster of two nodes. Of course, for fault-tolerance you need a 3-node cluster, so launch a third node like so on host3:

    host3:$ rqlited -node-id 3 -http-addr host3:4001 -raft-addr host3:4002 -join http://host1:4001 ~/node

When simply restarting a node, there is no further need to pass -join. However, if a node does attempt to join a cluster it is already a member of, and neither its node ID or Raft network address has changed, then the cluster Leader will ignore the join request as there is nothing to do – the joining node is already a fully-configured member of the cluster. However, if either the node ID or Raft network address of the joining node has changed, the cluster Leader will first automatically remove the joining node from the cluster configuration before processing the join request. For most applications this is an implementation detail which can be safely ignored, and cluster-joins are basically idempotent.

You’ve now got a fault-tolerant, distributed, relational database. It can tolerate the failure of any node, even the leader, and remain operational.

Node IDs
--

You can set the Node ID (-node-id) to anything you wish, as long as it’s unique for each node.

    https://rqlite.io/docs/clustering/automatic-clustering/

For simplicity, let’s assume you want to run a 3-node rqlite cluster. The network addresses of the nodes are $HOST1, $HOST2, and $HOST3. To bootstrap the cluster, use the -bootstrap-expect option like so:

Node 1:

    rqlited -node-id 1 -http-addr=$HOST1:4001 -raft-addr=$HOST1:4002 \
    -bootstrap-expect 3 -join http://$HOST1:4001,http://$HOST2:4001,http://$HOST3:4001 data

Node 2:

    rqlited -node-id 2 -http-addr=$HOST2:4001 -raft-addr=$HOST2:4002 \
    -bootstrap-expect 3 -join http://$HOST1:4001,http://$HOST2:4001,http://$HOST3:4001 data

Node 3:

    rqlited -node-id 3 -http-addr=$HOST3:4001 -raft-addr=$HOST3:4002 \
    -bootstrap-expect 3 -join http://$HOST1:4001,http://$HOST2:4001,http://$HOST3:4001 data

-bootstrap-expect should be set to the number of nodes that must be available before the bootstrapping process will commence, in this case 3. You also set -join to the HTTP URL of all 3 nodes in the cluster. It’s also required that each launch command has the same values for -bootstrap-expect and -join.

After the cluster has formed, you can launch more nodes with the same options. A node will always attempt to first perform a normal cluster-join using the given join addresses, before trying the bootstrap approach.

Docker
--

With Docker you can launch every node identically:

    docker run rqlite/rqlite -bootstrap-expect 3 -join http://$HOST1:4001,http://$HOST2:4001,http://$HOST3:4001

where $HOST[1-3] are the expected network addresses of the containers

    https://rqlite.io/docs/guides/backup/

Backing up rqlite
--

rqlite supports hot backing up a node. You can retrieve a copy of the underlying SQLite database via the rqlite shell, or by directly access the API. Retrieving a full copy of the SQLite database is the recommended way to backup a rqlite system.

To backup to a file via the rqlite shell issue the following command:

    127.0.0.1:4001> .backup bak.sqlite3

backup file written successfully
This command will write the SQLite database file to bak.sqlite3.

You can also access the rqlite API directly, via a HTTP GET request to the endpoint /db/backup. For example, using curl, and assuming the node is listening on localhost:4001, you could retrieve a backup as follows:

    curl -s -XGET localhost:4001/db/backup -o bak.sqlite3

Generating a SQL text dump
--

You can also dump the database in SQL text format via the CLI as follows:

    127.0.0.1:4001> .dump bak.sql

SQL text file written successfully
The API can also be accessed directly:

    curl -s -XGET localhost:4001/db/backup?fmt=sql -o bak.sql

Examples
--

The following examples show a trivial database being generated by sqlite3 and then loaded into a rqlite node listening on localhost.

HTTP
Be sure to set the Content-type header as shown, depending on the format of the upload.

    ~ $ sqlite3 restore.sqlite
    SQLite version 3.14.1 2016-08-11 18:53:32
    Enter ".help" for usage hints.
    sqlite> CREATE TABLE foo (id integer not null primary key, name text);
    sqlite> INSERT INTO "foo" VALUES(1,'fiona');
    sqlite>

# Convert SQLite database file to set of SQL statements and then load

    ~ $ echo '.dump' | sqlite3 restore.sqlite > restore.dump
    ~ $ curl -XPOST localhost:4001/db/load -H "Content-type: text/plain" --data-binary @restore.dump

# Load directly from the SQLite file, which is the recommended process.

    ~ $ curl -v -XPOST localhost:4001/db/load -H "Content-type: application/octet-stream" --data-binary @restore.sqlite

After either command, we can connect to the node, and check that the data has been loaded correctly.

    $ rqlite
    127.0.0.1:4001> SELECT * FROM foo
    +----+-------+
    | id | name  |
    +----+-------+
    | 1  | fiona |
    +----+-------+

rqlite shell
--

The shell supports loading from a SQLite database file or SQL text file. The shell will automatically detect the type of data being used for the restore operation. Below shows an example of loading from the former.

    ~ $ sqlite3 mydb.sqlite
    SQLite version 3.22.0 2018-01-22 18:45:57
    Enter ".help" for usage hints.
    sqlite> CREATE TABLE foo (id integer not null primary key, name text);
    sqlite> INSERT INTO "foo" VALUES(1,'fiona');
    sqlite> .exit
    ~ $ ./rqlite
    Welcome to the rqlite CLI. Enter ".help" for usage hints.
    127.0.0.1:4001> .schema
    +-----+
    | sql |
    +-----+
    127.0.0.1:4001> .restore mydb.sqlite
    database restored successfully
    127.0.0.1:4001> select * from foo
    +----+-------+
    | id | name  |
    +----+-------+
    | 1  | fiona |
    +----+-------+

Caveats
--

Note that SQLite dump files normally contain a command to disable Foreign Key constraints. If you are running with Foreign Key Constraints enabled, and wish to re-enable this, this is the one time you should explicitly re-enable those constraints via the following curl command:

    curl -XPOST 'localhost:4001/db/execute?pretty' -H "Content-Type: application/json" -d '[
        "PRAGMA foreign_keys = 1"
    ]'

    https://rqlite.io/docs/guides/monitoring-rqlite/

    https://rqlite.io/docs/cli/

rqlite is a command line tool for connecting to a rqlite node. Consult the SQLite query language documentation
https://www.sqlite.org/lang.html
for full details on the supported SQL syntax.

    https://rqlite.io/docs/api/api/

    https://github.com/rqlite
    https://github.com/rqlite/gorqlite
    https://github.com/rqlite/rqlite-docker

    https://rqlite.io/docs/guides/kubernetes/
    https://rqlite.io/docs/guides/security/

    https://rqlite.io/docs/guides/performance/

In-memory databases
--

By default rqlite uses an in-memory SQLite database to maximise performance. In this mode no actual SQLite file is created and the entire database is stored in memory. If you wish rqlite to use an actual file-based SQLite database, pass -on-disk to rqlite on start-up.

Does using an in-memory SQLite database put my data at risk?

No.
Since the Raft log is the authoritative store for all data, and it is stored on disk by each node, an in-memory database can be fully recreated on start-up from the information stored in the Raft log. Using an in-memory database does not put your data at risk.

Use a memory-backed filesystem
It is possible to run rqlite entirely on-top of a memory-backed file system. This means that both the Raft log and SQLite database would be stored in memory only. For example, on Linux you can create a memory-based filesystem like so:

    mount -t tmpfs -o size=512m tmpfs /mnt/ramdisk

This comes with risks, however. The behavior of rqlite when a node fails, but committed entries the Raft log have not actually been permanently persisted, is not defined. But if your policy is to completely deprovision your rqlite node, or rqlite cluster, in the event of any node failure, this option may be of interest to you. Perhaps you always rebuild your rqlite cluster from a different source of data, so can recover an rqlite cluster regardless of its state. Testing shows that using rqlite with a memory-only file system can result in 100x improvement in performance.

However, if you enable an on-disk SQLite database, but then place the SQLite database on a memory-backed file system, you can have the best of both worlds. You can dedicate your disk to the Raft log, but still get better read-write concurrency with SQLite. You can specify the SQLite database file path via the -on-disk-path flag.

An alternative approach would be to place the SQLite on-disk database on a disk different than that storing the Raft log, but this is unlikely to be as performant as an in-memory file system for the SQLite database.

In-memory Database Limits
rqlite was not designed for very large datasets: While there are no hardcoded limits in the rqlite software, the nature of Raft means that the entire SQLite database is periodically copied to disk, and occasionally copied, in full, between nodes. Your hardware may not be able to process those large data operations successfully. You should test your system carefully when working with multi-GB databases.

In-memory SQLite databases (the default configuration) are currently limited to 2GiB in size. One way to get around this limit is to use an on-disk database, by passing -on-disk to rqlited. But this could impact write-performance significantly, since disk is slower than memory. If you switch to on-disk SQLite, and find write-performance suffers, there are a couple of ways to address this. One option is to place the Raft log on one disk, and the SQLite database on a different disk.

Another option is to run rqlite in on-disk mode but place the SQLite database file on a memory-backed filesystem. That way you can use larger databases, and still have performance similar to running with an in-memory SQLite database.

In either case to control where rqlite places the SQLite database file, set -on-disk-startup and -on-disk-path when launching rqlited. Note that you should still place the data directory on an actual disk, so that the Raft log is always on a physical disk, to ensure your data is not lost if a node retarts.

Linux example
An example of running rqlite with a SQLite file on a memory-backed file system, and keeping the data directory on persistent disk, is shown below. The data directory is where the Raft log is stored. The example below would allow up to a 4GB SQLite database.

# Create a RAM disk, and then launch rqlite, telling it to

# put the SQLite database on the RAM disk.

    mount -t tmpfs -o size=4096m tmpfs /mnt/ramdisk
    rqlited -on-disk -on-disk-startup -on-disk-path /mnt/ramdisk/node1/db.sqlite /path_to_persistent_disk/data

where /path_to_persistent_disk/data is a directory path on your persistent disk.

Setting -on-disk-startup is also important because it disables an optimization rqlite performs at startup, when using an on-disk SQLite database. rqlite, by default, initially builds any on-disk database in memory first, before moving it to disk. It does this to reduce startup times. But with databases larger than 2GiB, this optimization can cause rqlite to fail to start. To avoid this issue, you can disable this optimization via the flag, but your startup times may be noticeably longer.

How do I access the database?
The primary way to access the database is via the HTTP API. You can access it directly, or use a client library. For more casual use you can use the command line tool. It is also technically possible to read the SQLite file directly, but it’s not officially supported.

Is it a drop-in replacement for SQLite?
No. While it does use SQLite as its storage engine, you must only write to the database via the HTTP API. That said, since it basically exposes SQLite, all the power of that database is available. It is also possible that any system built on top of SQLite only needs small changes to work with rqlite.

rqlite is distributed. Does that mean it can increase SQLite performance?
Yes, but only for reads. It does not provide any scaling for writes, since all writes must go through the leader. rqlite is distributed primarily for replication and fault tolerance, not for performance. In fact write performance is reduced relative to a standalone SQLite database, because of the round-trips between nodes and the need to write to the Raft log.

    https://rqlite.io/docs/api/queued-writes/
    https://rqlite.io/docs/api/bulk-api/

    https://rqlite.io/docs/design/

Raft
--

The Raft layer always creates a file – it creates the Raft log. This log stores the set of committed SQLite commands, in the order which they were executed. This log is authoritative record of every change that has happened to the system. It may also contain some read-only queries as entries, depending on read-consistency choices. Since every node in an rqlite cluster applies the entries log in exactly the same way, this guarantees that the SQLite database is the same on every node.

SQLite
--

By default, the SQLite layer doesn’t create a file. Instead, it creates the database in memory. rqlite can create the SQLite database on disk, if so configured at start-time, by passing -on-disk to rqlited at startup. Regardless of whether rqlite creates a database entirely in memory, or on disk, the SQLite database is completely recreated everytime rqlited starts, using the information stored in the Raft log.

Log Compaction and Truncation
--

rqlite automatically performs log compaction, so that disk usage due to the log remains bounded. After a configurable number of changes rqlite snapshots the SQLite database, and truncates the Raft log. This is a technical feature of the Raft consensus system, and most users of rqlite need not be concerned with this.

#
