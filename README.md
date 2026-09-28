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
  <img src="https://img.shields.io/badge/Java-21%20LTS-007396.svg" alt="Java" />
  <img src="https://img.shields.io/badge/Python-3.13%20%7C%20uv-3776AB.svg" alt="Python" />
  <img src="https://img.shields.io/badge/PostgreSQL-17%20Alpine-336791.svg" alt="PostgreSQL" />
</p>

---

## Overview

**IntelliBitz** is an engineering monorepo and knowledge base providing containerized services, system administration tooling, cloud automation, mobile client architectures, and language toolchains.

The repository is organized by **programming languages and technologies**:

```text
archives/
├── java/      # Java EE/SE projects (IntelliDocs, IntelliMeet)
├── kotlin/    # Kotlin apps, Multiplatform, Android clients
├── python/    # Python tools, AI, scripts, package management
├── shell/     # Shell scripts for OS, DevOps, Cloud (Docker, k8s, GCP)
├── sql/       # Database schemas, scripts (PostgreSQL, MySQL)
├── php/       # PHP Web Portals
├── go/        # Go scripts and tools
├── clojure/   # Clojure tutorials and tools
├── expect/    # Expect automation scripts
└── docs/      # Markdown documentation and guides
```

---

## Directory Index & Architecture

### ☕ [Java (`java/`)](./java/)
Enterprise backend architectures and SE projects.
- **[Java EE](./java/javaee/)**: Multi-module Maven enterprise architectures (Java 21).
  - **[IntelliDocs](./java/javaee/intellidocs)**: Enterprise document platform.
  - **[IntelliMeet](./java/javaee/intellimeet)**: Collaborative meeting backend platform (`32tango`).
- **[Java SE](./java/javase/)**: Desktop applications (Tamil font transliterator, text editors).

### 🚀 [Kotlin (`kotlin/`)](./kotlin/)
Modern Kotlin development spanning mobile, web, and multiplatform.
- **[Android (`android/`)](./kotlin/android)**:
  - **[IntelliDroid](./kotlin/android/IntelliDroid)**: Flagship enterprise client (Kotlin 2.0+, SDK 35).
  - 100% Kotlin modules: Event tracker (`MEvents`), Twitter trends (`TwRends`), GPS telemetry (`wuffittracker`).
- **[Multiplatform](./kotlin/multiplatform)**: Shared logic modules.
- **[Web](./kotlin/jsfront)**: JS frontends and tutorials.

### 🐍 [Python (`python/`)](./python/)
Python automation, environment configuration, and AI toolchains.
- Modern [uv package manager](./python/uv.md) (`uv.sh`), Python 3.12/3.13 setups.
- AI integration: Hugging Face Hub CLI guide ([`huggingface.md`](./python/huggingface.md)), Google Colab & Gemini API ([`colab.md`](./python/colab.md)), Meta Llama 3 ([`meta-llama.md`](./python/meta-llama.md)).

### 🐚 [Shell & DevOps (`shell/`)](./shell/)
System administration, container orchestration, and OS utilities.
- **[Linux](./shell/linux)**: Ubuntu updates, Samba sharing, OpenSSH hardening, GnuPG key management.
- **[Docker](./shell/docker)**: Engine install scripts, rootless configuration, Desktop setups.
- **[Kubernetes](./shell/kubernetes)**: `kubectl` installation and operations.
- **[Multipass](./shell/multipass)**: Ubuntu VM clusters and XRDP instances.
- **[Google Cloud](./shell/google)**: Compute Engine VMs, IAM, Cloud SDK automation.

### 🗄️ [SQL & Databases (`sql/`)](./sql/)
Container stacks, schemas, and administration scripts.
- **[PostgreSQL](./sql/postgresql)**: `docker-compose.yml`, schema definitions, Swarm stacks.
- **[MySQL](./sql/mysql)**: `docker-compose.yml` stack and schemas.
- **[SQLite](./sql/sqlite)** & **[rqlite](./sql/rqlite)**: Embedded and distributed SQLite setups.
- **[CouchDB](./sql/couchdb)**: Document-oriented database operations.

### 📚 [Documentation (`docs/`)](./docs/)
System configuration, web server recipes, and client standards.
- Web Servers: Nginx, Apache HTTP Server, Caddy, Apache Tomcat.
- Standards: JSON, TOML, WHATWG client specifications (`client/`).
- SCM Guides: Git (`git/`), Gradle (`gradle/`).

---

## Quickstart Guide

### 1. Launch Local Databases

```bash
# Start PostgreSQL:
cd sql/postgresql
docker compose up -d

# Start MySQL:
cd ../mysql
docker compose up -d
```

### 2. Build the Android Client

```bash
cd kotlin/android/IntelliDroid
./gradlew assembleDebug
```

### 3. Build Java EE Backends

```bash
cd java/javaee/intellimeet/32tango
mvn clean install
```

### 4. Run System & DevOps Automation Scripts

All `.sh` scripts in the repository have executable permissions set and have passed syntax validation:

```bash
# Check syntax of any script
bash -n shell/linux/ubuntu/apt-full-upgrade.sh

# Run script
./shell/linux/ubuntu/apt-full-upgrade.sh
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
