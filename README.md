# IntelliBitz

<p align="center">
  <strong>Comprehensive AI Ecosystem, DevOps, Systems Infrastructure, Cloud Platforms & Multi-Platform Software Suite</strong>
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

**IntelliBitz** is an enterprise engineering monorepo and knowledge base providing a complete AI provider ecosystem, containerized services, system administration tooling, cloud automation, mobile client architectures, and language toolchains.

The repository is organized by **functional intent and architectural domain**:

```text
archives/
├── ai/            # AI models, cost analysis, local execution guides & LLM architectures
├── runtimes/      # Operating system runtimes & language environments (Go, Clojure, Python, Shell)
├── infra/         # SCM, DevOps & Infrastructure orchestration (Docker, K8s, GCP, Git, Gradle)
├── data/          # Database systems, schemas & persistence stacks (PostgreSQL, MySQL, SQLite, rqlite)
├── services/      # Backend services, middleware & web servers (Java EE, Python Web, Nginx, Apache)
├── clients/       # User interfaces & client applications (Android, Multiplatform, Java SE, Web Standards)
└── docs/          # Project knowledge base, roadmap, contributing & security guidelines
```

---

## Directory Index & Architecture

### 🤖 [AI Ecosystem & Knowledge Base (`ai/`)](./ai/)
Comprehensive AI models, cost comparisons, and integration guides.
- **[Providers (`ai/`)](./ai/)**: DeepSeek, Qwen, OpenAI, Claude, Xiaomi MiMo, Zhipu GLM, Agnes AI, Google AI, Mistral, and Inclusion AI.
- **[Benchmarks & Guides](./ai/README.md)**: [Cost Ranking](./ai/COST_RANKING.md), [Local Execution](./ai/LOCAL_EXECUTION.md), [Leaderboard](./ai/TOP_100_LEADERBOARD.md), [BYOK API Keys](./ai/BYOK_API_KEYS.md), and [Top 100 GitHub AI Projects](./ai/TOP_100_GITHUB_PROJECTS.md).

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

### 🗄️ [Data & Databases (`data/sql/`)](./data/sql/)
Container stacks, schemas, and persistence administration scripts.
- **[PostgreSQL & MySQL (`data/sql/`)](./data/sql/)**: Production Docker Compose stacks, schemas, and initialization scripts (`data/sql/postgresql/`, `data/sql/mysql/`).
- **[SQLite & rqlite](./data/sql/)**: Embedded and distributed Raft database setups.
- **[CouchDB](./data/sql/)**: Document-oriented database operations.

### ☁️ [Services & Middleware (`services/`)](./services/)
Enterprise backends, web portals, and reverse proxy web servers.
- **[Java EE (`javaee/`)](./services/javaee/)**: Multi-module enterprise architectures (`IntelliDocs`, `IntelliMeet`).
- **[Python Web (`python/intelligeek/`)](./services/python/intelligeek/)**: Web portal applications.
- **[Web Servers (`webservers/`)](./services/webservers/)**: Nginx, Apache HTTP Server, Caddy, Apache Tomcat, and Flatpak configurations.

### 📱 [Clients & User Interfaces (`clients/`)](./clients/)
Mobile apps, multiplatform suites, desktop utilities, and web standards.
- **[Android (`android/`)](./clients/android/)**: Flagship [IntelliDroid](./clients/android/IntelliDroid/README.md) and 100% Kotlin modules ([MEvents](./clients/android/MEvents/README.md), [TwRends](./clients/android/TwRends/README.md), [UDigg](./clients/android/UDigg/README.md), [wuffittracker](./clients/android/wuffittracker/README.md)).
- **[Multiplatform & JS (`clients/`)](./clients/)**: Kotlin Multiplatform modules and JavaScript frontends.
- **[Desktop & Examples (`javase/`, `java-examples/`)](./clients/)**: Java SE desktop applications and Kotlin interoperability examples.
- **[Standards (`standards/`)](./clients/standards/)**: WHATWG specifications, JSON, and TOML standards.

---

## Quickstart Guide

### 1. Launch Local Databases

```bash
# Start PostgreSQL:
cd data/sql/postgresql
docker compose up -d

# Start MySQL:
cd ../mysql
docker compose up -d
```

### 2. Build the Android Client

```bash
cd clients/android/IntelliDroid
./gradlew assembleDebug
```

### 3. Build Java EE Backends

```bash
cd services/javaee
mvn clean install
```

### 4. Run System & DevOps Automation Scripts

All `.sh` scripts in the repository have executable permissions set and have passed syntax validation:

```bash
# Check syntax of any script
bash -n infra/scripts/automation/auto_keep_all.sh

# Run script
./infra/scripts/automation/auto_keep_all.sh
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
