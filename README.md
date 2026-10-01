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

The repository is organized by **functional intent and architectural domain**:

```text
archives/
├── runtimes/      # Operating system runtimes & language environments (Go, Clojure, Python, Shell)
├── infra/         # SCM, DevOps & Infrastructure orchestration (Docker, K8s, GCP, Git, Gradle)
├── data/          # Database systems, schemas & persistence stacks (PostgreSQL, MySQL, SQLite)
├── services/      # Backend services, middleware & web servers (Java EE, Python Web, Nginx, Apache)
├── clients/       # User interfaces & client applications (Android, Multiplatform, Java SE, Web Standards)
└── docs/          # Project knowledge base, roadmap, contributing & security guidelines
```

---

## Directory Index & Architecture

### ⚙️ [Runtimes (`runtimes/`)](./runtimes/)
Operating system runtimes and language environment scripts.
- **[Go & Clojure](./runtimes/)**: Workspace scripts and tutorials.
- **[Python Runtimes](./runtimes/python/)**: Package management (`uv`, `pip`), conda setups, and AI toolchain guides.
- **[Shell Systems](./runtimes/shell/)**: Linux system administration, brew, and SDKman configurations.

### 🚀 [Infrastructure & DevOps (`infra/`)](./infra/)
Container orchestration, cloud automation, and developer toolchains.
- **[Containerization & Cloud](./infra/)**: Docker, Kubernetes (`kubectl`), Multipass VMs, and Google Cloud automation.
- **[SCM & Build Systems](./infra/)**: Git guides and Gradle wrapper standards.
- **[Automation Scripts](./infra/expect/)**: Expect automation scripts.

### 🗄️ [Data & Databases (`data/`)](./data/)
Container stacks, schemas, and persistence administration scripts.
- **[PostgreSQL & MySQL](./data/)**: Production Docker Compose stacks, schemas, and initialization scripts.
- **[SQLite & rqlite](./data/)**: Embedded and distributed Raft database setups.
- **[CouchDB](./data/)**: Document-oriented database operations.

### ☁️ [Services & Middleware (`services/`)](./services/)
Enterprise backends, web portals, and reverse proxy web servers.
- **[Java EE (`javaee/`)](./services/javaee/)**: Multi-module enterprise architectures (`IntelliDocs`, `IntelliMeet`).
- **[Python Web (`python/intelligeek/`)](./services/python/intelligeek/)**: Web portal applications.
- **[Web Servers (`webservers/`)](./services/webservers/)**: Nginx, Apache HTTP Server, Caddy, Apache Tomcat, and Flatpak configurations.

### 📱 [Clients & User Interfaces (`clients/`)](./clients/)
Mobile apps, multiplatform suites, desktop utilities, and web standards.
- **[Android (`android/`)](./clients/android/)**: Flagship [IntelliDroid](./clients/android/IntelliDroid/README.md) and 100% Kotlin modules (`MEvents`, `TwRends`, `UDigg`, `wuffittracker`).
- **[Multiplatform & JS](./clients/)**: Kotlin Multiplatform modules and JavaScript frontends.
- **[Desktop & Examples (`javase/`, `java-examples/`)](./clients/)**: Java SE desktop applications and Kotlin interoperability examples.
- **[Standards (`standards/`)](./clients/standards/)**: WHATWG specifications, JSON, and TOML standards.

---

## Quickstart Guide

### 1. Launch Local Databases

```bash
# Start PostgreSQL:
cd data/postgresql
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
