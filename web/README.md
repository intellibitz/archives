# Web Infrastructure, Cloud & Backend Services (`web/`)

This directory contains web server configurations, Google Cloud Platform (GCP) automation scripts, Java EE enterprise application architectures, and full-stack backend platforms.

---

## Directory Overview

```
web/
├── apache/            # Apache HTTP Server CLI & Docker container runner
├── nginx/             # Nginx reverse proxy and web server administration
├── caddy/             # Caddy HTTP/2 and automatic HTTPS server
├── nodejs/            # Node.js runtime and environment scripts
├── tomcat/            # Apache Tomcat servlet container management
├── google/            # Google Cloud Platform & App Engine suites
│   ├── *.sh           # Cloud SDK, Compute Engine, IAM & Service Account scripts
│   ├── att/           # ATT client/server App Engine project
│   ├── gwt17-gae-*/   # Google Web Toolkit 1.7 + GAE Twitter Bex engine
│   └── gwt2-spr3-*/   # GWT 2 + Spring 3 + JPA 2 + Hibernate 3.5 stack
└── javaee/            # Enterprise Java EE Multi-Module Maven Projects
    ├── intellidocs/   # Document management EAR/EJB/WAR enterprise suite
    └── intellimeet/   # Collaborative meeting & matchmaking enterprise platform
        ├── 32tango/   # JBoss Seam / Java EE backend (EAR, EJB, WAR)
        └── dating/    # Mobile Android client module
```

---

## Architecture Components

### 1. Web Servers & Reverse Proxies

- **[Apache HTTP Server](./web/apache)**: Multi-platform Apache commands (`myApacheCmd.sh`) and container execution (`runHttpdDocker.sh`).
- **[Nginx](./web/nginx)**: Production web server and reverse proxy commands (`myNginxCmd.sh`).
- **[Caddy](./web/caddy)**: Automated TLS certificate provisioning and modern Caddyfile configuration ([`myCaddy.md`](./web/caddy/myCaddy.md)).
- **[Apache Tomcat](./web/tomcat)**: Tomcat servlet container lifecycle and deployment scripts (`myTomcatCmd.sh`).

### 2. Google Cloud Platform Automation ([`web/google/`](./web/google))

- **GCP Authentication & Projects**: `authlogin-configsetproject.sh`, `projects-list.sh`
- **Compute Engine**: Provisioning VM instances and persistent disks (`compute-instances-create.sh`, `compute-disks-create.sh`)
- **IAM & Service Accounts**: Creating service accounts and inspecting OAuth scopes (`iam-serviceaccounts-create.sh`, `scopeinfo-serviceaccounts.sh`)
- **Cloud SDK**: Automated SDK installation (`install-cloudsdk.sh`)
- **[AT&T Demo Suite](./web/google/att/README.md)**: Cloud-backed Android client and App Engine demonstration backend.

### 3. Java EE Enterprise Platforms ([`web/javaee/`](./web/javaee/README.md))

- **[IntelliDocs](./web/javaee/intellidocs)**: Multi-module Maven enterprise application containing `intellidocs-ear`, `intellidocs-ejb`, `intellidocs-war`, unit tests, and comprehensive user guide.
- **[IntelliMeet](./web/javaee/intellimeet)**: Enterprise collaboration and meeting platform built on JBoss Seam, EJB 3, and JSF (`32tango`), paired with the companion Android mobile client (`dating`).

---

## Licensing

Distributed under the repository's root **[MIT License](./LICENSE)**.
