# IntelliBitz Technology Roadmap

A unified architectural roadmap spanning operating system runtimes, developer tooling, database backends, middleware, and user interfaces.

---

## Architecture Matrix

```
┌────────────────────────────────────────────────────────────────────────┐
│                         IntelliBitz Ecosystem                          │
├─────────────────┬─────────────────┬──────────────────┬─────────────────┤
│    OS & Lang    │   SCM & DevOps  │    Databases     │    UI & Apps    │
│  (os/linux,     │  (scm/docker,   │  (db/postgresql, │   (ui/android,  │
│   os/lang)      │   scm/git)      │   db/mysql)      │    ui/client)   │
└────────┬────────┴────────┬────────┴────────┬─────────┴────────┬────────┘
         │                 │                 │                  │
         ▼                 ▼                 ▼                  ▼
┌─────────────────┬─────────────────┬──────────────────┬─────────────────┐
│ • Ubuntu Server │ • Docker Engine │ • PostgreSQL 16+ │ • IntelliDroid  │
│ • Arch Linux    │ • Docker Swarm  │ • pgAdmin 4      │ • Modern Kotlin │
│ • Kotlin Multi  │ • Multipass VMs │ • MySQL / Maria  │ • WHATWG Specs  │
│ • Go Workspaces │ • Kubernetes    │ • SQLite / Raft  │ • Web Standards │
│ • Python & AI   │ • Gradle Multi  │ • CouchDB / JSON │ • Mobile Suites │
└─────────────────┴─────────────────┴──────────────────┴─────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Middleware & Cloud (web/)                       │
├────────────────────────────────────────────────────────────────────────┤
│ • Ktor Microservices & Reactive APIs                                   │
│ • Google Cloud Platform Automation & Compute Engine                    │
│ • Reverse Proxies: Nginx, Apache HTTPD, Caddy TLS                      │
│ • Enterprise Java EE / Jakarta EE Backends                             │
└────────────────────────────────────────────────────────────────────────┘
```

---

## Status & Milestones

### 1. OS & Language Runtimes (`os/`)

- [x] **Linux/Ubuntu**: Desktop and server installation, update scripts, network shares (`mySamba.sh`), and OpenSSH hardening.
- [x] **Arch Linux**: Distribution architecture and package management reference (`archlinux.org.adoc`).
- [x] **Kotlin Multiplatform**: Full-stack Kotlin modules, learning suite (`KotlinLearn`), playground (`KotlinPlay`), and JS frontend.
- [x] **Go Workspaces**: Multi-module workspace management and dependency tracking (`myGo.sh`).
- [x] **Python & AI**: Modern AI ecosystem integration (Hugging Face Hub, Google Colab Gemini API, Meta Llama 3).
- [ ] **Ktor Native Service**: Embedded Ktor runtime on Linux container base.

### 2. SCM & Infrastructure (`scm/`)

- [x] **Git**: AsciiDoc Pro Git documentation (`git-scm.org.adoc`), credential helper setups, GitHub CLI automation.
- [x] **Docker**: Docker Engine automated install, rootless setup, multi-container Compose stacks.
- [x] **Multipass**: VM clustering, GPU/xrdp desktop instances, and automated Docker blueprints.
- [x] **Kubernetes**: CLI toolchain (`kubectl`) and orchestration cheat sheets.
- [x] **Gradle**: Modern Gradle Wrapper standards, Kotlin DSL accessor tooling, multi-project authoring.

### 3. Database Infrastructure (`db/`)

- [x] **PostgreSQL**: Production Docker Compose with persistent data volumes and pgAdmin 4 web console.
- [x] **MySQL / MariaDB**: Docker Compose service, automated database schema initialization (`myMySQL.sql`).
- [x] **Embedded & Distributed Relational**: SQLite CLI operations and rqlite Raft clustering guide.
- [x] **Document & Config Standards**: CouchDB Fauxton operations, TOML v1.0.0 specification, JSON schema standards.

### 4. Middleware & Web (`web/`)

- [x] **Web Servers**: Automated reverse proxy and container recipes for Nginx, Apache, and Caddy.
- [x] **Google Cloud Platform**: CLI scripts for IAM, service accounts, compute instances, and persistent disks.
- [x] **Enterprise Backend**: Java EE multi-module architectures (`intellidocs`, `32tango`).
- [ ] **Ktor API Gateway**: Transitioning enterprise backends to lightweight Ktor microservices.

### 5. User Interfaces & Clients (`ui/`)

- [x] **Android Suite Modernization**: Flagship [IntelliDroid](./ui/android/IntelliDroid/README.md) and 100% Kotlin standalone modules ([MEvents](./ui/android/MEvents/README.md), [TwRends](./ui/android/TwRends/README.md), [UDigg](./ui/android/UDigg/README.md), [wuffittracker](./ui/android/wuffittracker/README.md)) upgraded to Android SDK 35, Kotlin 2.0.21, Gradle 8.13, and AGP 8.7.2.
- [x] **Cloud & Mobile Client Systems**: Reference architectures for peer-to-peer exchange ([booksExchange](./ui/android/booksExchange/README.md)), martial arts matchmaking ([fiteclub](./ui/android/fiteclub/README.md)), and proximity discovery ([mobeegal](./ui/android/mobeegal/README.md)).
- [x] **Web Standards**: Complete specification guides for modern frontend (HTML, CSS, JS, HTTP/3, WebSockets, Storage).
- [x] **Repository Documentation & License Unification**: Consolidated READMEs, deleted legacy plain-text/GPL files, and standardized on single root MIT license across all sub-projects.
- [ ] **Jetpack Compose UI**: Modern declarative UI migration for IntelliDroid client components.
