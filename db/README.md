# Database Architecture & Infrastructure (`db/`)

This directory houses container configurations, deployment scripts, schema definitions, and interactive command references for relational, document, and distributed database engines.

---

## Supported Engines & Standards

| Engine / Standard   | Category                       | Key Files                                                                                                                                                                                                                                                                                                                                                      | Description                                                                                                 |
| :------------------ | :----------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :---------------------------------------------------------------------------------------------------------- |
| **PostgreSQL**      | Relational / Object-Relational | [`postgresql/docker-compose.yml`](./db/postgresql/docker-compose.yml)<br>[`postgresql/myPgsql.sql`](./db/postgresql/myPgsql.sql)<br>[`postgresql/myPgsql.sh`](./db/postgresql/myPgsql.sh) | PostgreSQL 17 Alpine, pgAdmin 4 web console, native healthchecks, Swarm stacks, and administration scripts. |
| **MySQL / MariaDB** | Relational                     | [`mysql/docker-compose.yml`](./db/mysql/docker-compose.yml)<br>[`mysql/myMySQL.sql`](./db/mysql/myMySQL.sql)<br>[`mysql/myMySQLCmd.sh`](./db/mysql/myMySQLCmd.sh)                         | MySQL 8.4 LTS + Node.js 22 LTS Compose service, healthchecks, batch schema scripts, and command cheatsheet. |
| **SQLite**          | Embedded Relational            | [`sqlite/mySqlite.md`](./db/sqlite/mySqlite.md)                                                                                                                                                                                                                                                         | File-based database administration, schemas, and CLI operations.                                            |
| **rqlite**          | Distributed Relational         | [`rqlite/myRqlite.md`](./db/rqlite/myRqlite.md)                                                                                                                                                                                                                                                         | Lightweight distributed relational database built on SQLite and Raft consensus.                             |
| **CouchDB**         | Document Store                 | [`couchdb/myCouchdb.md`](./db/couchdb/myCouchdb.md)                                                                                                                                                                                                                                                     | Multi-master distributed JSON document database and Fauxton dashboard.                                      |
| **TOML**            | Configuration Format           | [`toml/toml-v1.0.0.md`](./db/toml/toml-v1.0.0.md)                                                                                                                                                                                                                                                       | Complete TOML v1.0.0 formal specification and typing rules.                                                 |
| **JSON**            | Serialization Standard         | [`json/myJson.md`](./db/json/myJson.md)                                                                                                                                                                                                                                                                 | JSON schemas, standards, and data processing references.                                                    |

---

## Quickstart

### Launch PostgreSQL with pgAdmin

```bash
cd db/postgresql
docker compose up -d
```

- PostgreSQL available on: `localhost:65432` (User: `postgres`, Password: `${POSTGRES_PASSWORD:-admin}`)
- pgAdmin 4 Web UI available on: `http://localhost:65430`

### Launch MySQL Stack

```bash
cd db/mysql
docker compose up -d
```

- MySQL available on: `127.0.0.1:3306` (Database: `todos`, User: `root`, Password: `secret`)
