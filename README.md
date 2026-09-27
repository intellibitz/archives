# IntelliBitz

<p align="center">
  <strong>Comprehensive DevOps, Systems Infrastructure, Cloud Platforms & Multi-Platform Software Suite</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License: MIT" />
  <img src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF.svg" alt="GitHub CI" />
  <img src="https://img.shields.io/badge/Dependabot-Enabled-02569B.svg" alt="Dependabot" />
  <img src="https://img.shields.io/badge/Docker-Compose%20v2%20%7C%20Alpine-2496ED.svg" alt="Docker" />
  <img src="https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF.svg" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Python-3.13%20%7C%20uv-3776AB.svg" alt="Python" />
  <img src="https://img.shields.io/badge/PostgreSQL-17%20Alpine-336791.svg" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/MySQL-8.4%20LTS-4479A1.svg" alt="MySQL" />
</p>

---

## Overview

**IntelliBitz** is an engineering monorepo and knowledge base providing containerized services, system administration tooling, cloud automation, mobile client architectures, and language toolchains.

The repository is organized into **five foundational pillars**:

```
intellibitz/
├── db/        # Databases: PostgreSQL, MySQL, SQLite, rqlite, CouchDB, TOML, JSON
├── os/        # Operating Systems: Linux (Ubuntu, Arch), Windows/WSL, Language toolchains
├── scm/       # DevOps & SCM: Git, Docker, Kubernetes, Multipass, Gradle, SDKMAN
├── ui/        # User Interfaces: Android clients (IntelliDroid), Web Standards, AsciiDoc
└── web/       # Web & Cloud: Web servers, GCP automation, Enterprise Java EE backends
```

---

## Directory Index & Architecture

### 🗄️ [Databases (`db/`)](./db/README.md)

Container stacks, database administration scripts, schemas, and data specifications.

- **[PostgreSQL](./db/postgresql)**: [`docker-compose.yml`](./db/postgresql/docker-compose.yml) (Postgres + pgAdmin 4), [`myPgsql.sql`](./db/postgresql/myPgsql.sql) schema definitions, [`myPgsql.sh`](./db/postgresql/myPgsql.sh) management script, Docker Swarm [`stack.yml`](./db/postgresql/stack.yml).
- **[MySQL](./db/mysql)**: [`docker-compose.yml`](./db/mysql/docker-compose.yml) stack, [`myMySQL.sql`](./db/mysql/myMySQL.sql) schema, [`myMySQLCmd.sh`](./db/mysql/myMySQLCmd.sh) cheatsheet.
- **[SQLite](./db/sqlite)**: Embedded database usage & CLI commands.
- **[rqlite](./db/rqlite)**: Distributed SQLite with Raft consensus.
- **[CouchDB](./db/couchdb)**: Document-oriented database operations and Fauxton setup.
- **[Specifications](./db/toml)**: [TOML v1.0.0 Specification](./db/toml/toml-v1.0.0.md) and [JSON Standards](./db/json/myJson.md).

### 🖥️ [Operating Systems & Runtimes (`os/`)](./os/README.md)

Operating system administration and programming language environments.

- **[Linux Administration](./os/linux)**: Ubuntu system updates (`apt-full-upgrade.sh`), Samba sharing (`mySamba.sh`), OpenSSH hardening (`ssh/`), GnuPG key management (`gpg/`), Arch Linux architecture guide (`arch/archlinux.org.adoc`), and GRUB dual-boot recovery ([`grub.md`](./os/linux/grub.md)).
- **[Windows & WSL](./os/win)**: WSL configuration and Windows service scripts.
- **[Language Toolchains](./os/lang)**:
  - **[Kotlin](./os/lang/kotlin)**: Tutorial suite (`KotlinLearn`), sandbox (`KotlinPlay`), multiplatform modules (`multiplatform`), web clients (`jsfront`, `mpfsweb`).
  - **[Java SE](./os/lang/javase)**: Tamil font transliterator desktop application ([`FontTransliterator`](./os/lang/javase/FontTransliterator/README.md)), text editors ([`sted`](./os/lang/javase/sted/README.md)).
  - **[Python & AI](./os/lang/python)**: Modern [uv package manager](./os/lang/python/uv.md) (`uv.sh`), Python 3.12/3.13, Hugging Face Hub CLI guide ([`huggingface.md`](./os/lang/python/huggingface.md)), Google Colab & Gemini API ([`colab.md`](./os/lang/python/colab.md)), Meta Llama 3 ([`meta-llama.md`](./os/lang/python/meta-llama.md)).
  - **[Go](./os/lang/go)**: Workspaces, modules, testing, and binaries.
  - **[Clojure](./os/lang/clojure)**: Language Reader syntax and data structure reference ([`myClojure.md`](./os/lang/clojure/myClojure.md)).
  - **[Expect](./os/lang/expect)**: OpenVPN and SFTP interactive automation scripts ([`expect-openvpn.exp`](./os/lang/expect/expect-openvpn.exp), [`expect-sftp.exp`](./os/lang/expect/expect-sftp.exp)).
  - **[PHP](./os/lang/php/README.md)**: Web community portal and interactive AJAX services ([`intelligeek`](./os/lang/php/intelligeek)).

### 🔄 [DevOps & Source Control (`scm/`)](./scm/README.md)

CI/CD workflows, containerization, and developer tooling.

- **[Docker](./scm/docker)**: Engine install scripts, rootless daemon configuration ([`dockerinstall-rootless.sh`](./scm/docker/dockerinstall-rootless.sh)), Docker Desktop setup guide ([`install-dockerdesktop.md`](./scm/docker/install-dockerdesktop.md)), Compose templates (HTTPD, Postgres, Ubuntu).
- **[Git & Platforms](./scm/git)**: Pro Git complete AsciiDoc guide ([`git-scm.org.adoc`](./scm/git/git-scm.org.adoc)), GitHub CLI automation, GitLab, Bitbucket.
- **[Kubernetes](./scm/kubernetes)**: `kubectl` installation script and operations reference.
- **[Multipass](./scm/multipass)**: Lightweight Ubuntu VM cluster automation, XRDP desktop instances, and Docker blueprint containers.
- **[Gradle](./scm/gradle)**: AsciiDoc Gradle user guides ([`gradle.org.adoc`](./scm/gradle/gradle.org.adoc)), sample multi-project builds (`GradleAuthoring`, `GradleRunning`).
- **[Package Managers](./scm/brew)**: Homebrew automated installer ([`install-brew.sh`](./scm/brew/install-brew.sh)), Flatpak, and SDKMAN.

### 📱 [User Interfaces & Clients (`ui/`)](./ui/README.md)

Mobile client applications and web frontend living standards.

- **[Android Applications Suite (`ui/android/`)](./ui/android/README.md)**:
  - **[IntelliDroid](./ui/android/IntelliDroid/README.md)**: Flagship modern enterprise client (SDK 35, Gradle 8.13, AGP 8.7.2, Kotlin 2.0.21, Coroutines, Room, Firebase FCM, Socket.IO).
  - **Modernized 100% Kotlin Modules**: Event tracker ([`MEvents`](./ui/android/MEvents/README.md)), Twitter trends ([`TwRends`](./ui/android/TwRends/README.md)), social bookmarking ([`UDigg`](./ui/android/UDigg/README.md)), GPS telemetry & tests ([`wuffittracker`](./ui/android/wuffittracker/README.md)).
  - **Full-Stack Reference Suites**: Barcode scanner book exchange ([`booksExchange`](./ui/android/booksExchange/README.md)), sparring network ([`fiteclub`](./ui/android/fiteclub/README.md)), proximity discovery ([`mobeegal`](./ui/android/mobeegal/README.md)).
  - **Device Tools**: ADB data pull/push utilities, KVM acceleration setup (`install-kvm.sh`), and [`local.properties.example`](./ui/android/local.properties.example).
- **[Web Client Standards](./ui/client/README.md)**: Curated WHATWG & W3C specifications for HTML, CSS, JavaScript, HTTP/3, WebSockets, Storage, and URL.
- **[AsciiDoc Documentation](./ui/asciidoc)**: Asciidoctor authoring documentation.

### 🌐 [Web & Cloud Infrastructure (`web/`)](./web/README.md)

Web servers, cloud provisioning, and enterprise backend architectures.

- **[Web Servers](./web)**: Configuration recipes for Apache HTTP Server, Nginx, Caddy TLS reverse proxy, Node.js, and Apache Tomcat.
- **[Google Cloud Platform](./web/google)**: CLI scripts for Compute Engine VM and disk creation, IAM service accounts, OAuth scope inspection, Cloud SDK automation, and [AT&T Demo Suite](./web/google/att/README.md).
- **[Java EE Enterprise Backends](./web/javaee/README.md)**: Multi-module Maven enterprise architectures:
  - **[IntelliDocs](./web/javaee/intellidocs)**: Enterprise document platform (`intellidocs-ear`, `intellidocs-ejb`, `intellidocs-war`).
  - **[IntelliMeet](./web/javaee/intellimeet)**: Collaborative meeting platform (`32tango` backend paired with companion `dating` Android mobile client).

---

## Quickstart Guide

### 1. Launch Local Databases

```bash
# Start PostgreSQL 16 + pgAdmin 4:
cd db/postgresql
docker compose up -d

# Start MySQL 8.0:
cd ../mysql
docker compose up -d
```

### 2. Build the Android Client

```bash
cd ui/android/IntelliDroid
./gradlew assembleDebug
```

### 3. Explore Kotlin Multiplatform

```bash
cd os/lang/kotlin/multiplatform
./gradlew build
```

### 4. Run System & DevOps Automation Scripts

All `.sh` scripts in the repository have executable permissions set and have passed syntax validation:

```bash
# Check syntax of any script
bash -n os/linux/ubuntu/apt-full-upgrade.sh

# Run script
./os/linux/ubuntu/apt-full-upgrade.sh
```

---

## Technology Roadmap

See [`roadmap.md`](./roadmap.md) for the active architecture status, component milestones, and future development plans.

---

## Contributing & Community

Contributions are warmly welcomed! Please review:

- [Contributing Guidelines](./CONTRIBUTING.md)
- [Contributor Code of Conduct](./CODE_OF_CONDUCT.md)
- [Security Policy](./SECURITY.md)

---

## License

This repository is licensed under the [MIT License](./LICENSE).
