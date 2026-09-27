# Contributing to IntelliBitz

Thank you for your interest in contributing to IntelliBitz! We welcome contributions to our infrastructure scripts, developer tools, database configurations, and applications.

---

## Code of Conduct

This project adheres to the Contributor Covenant [Code of Conduct](./CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

---

## How Can I Contribute?

### 1. Reporting Bugs

- Search existing issues to ensure the bug hasn't already been reported.
- If it hasn't, open a new issue using our [Bug Report Template](./.github/ISSUE_TEMPLATE/bug_report.md).
- Include clear reproduction steps, environment details (OS, shell version, Docker version), and expected vs actual behavior.

### 2. Suggesting Enhancements

- Open an issue using our [Feature Request Template](./.github/ISSUE_TEMPLATE/feature_request.md).
- Clearly describe the proposed feature, rationale, and use cases.

### 3. Submitting Pull Requests

- Fork the repository and create a descriptive feature branch from `main`:
  ```bash
  git checkout -b feature/your-feature-name
  ```
- Make your changes following the development guidelines below.
- Ensure scripts and configs pass syntax checks:
  ```bash
  # Check bash script syntax
  bash -n your_script.sh

  # Check docker compose syntax
  docker compose -f your_compose.yml config
  ```
- Commit your changes with clear, descriptive commit messages.
- Push to your fork and submit a Pull Request against the `main` branch.

---

## Repository Structure & Guidelines

The repository is structured into 5 foundational domains:

- **`db/` (Databases)**: PostgreSQL, MySQL, SQLite, CouchDB, and distributed databases.
  - Keep configuration files reproducible and documented with a matching `README.md`.
  - Place `.sql` schema scripts alongside service definitions.
- **`os/` (Operating Systems & Language Runtimes)**:
  - Linux distributions (Ubuntu, Arch) and Windows WSL scripts.
  - Programming language runtimes (`os/lang/`): Kotlin, Java SE, Python, Go, Clojure, PHP.
  - Scripts must have proper shebangs (`#!/usr/bin/env bash`), executable permissions, and pass `bash -n`.
  - Cheatsheets and prose documentation should use `.md` or `.adoc` instead of disguised `.sh` extensions.
- **`scm/` (Source Control & DevOps)**:
  - Tooling for Git, Docker, Kubernetes, Multipass, Gradle, and SDK managers.
  - Multi-container setups should include a clean `docker-compose.yml` with health checks and persistent volume declarations.
- **`ui/` (User Interfaces)**:
  - Android applications (such as [IntelliDroid](./ui/android/IntelliDroid)) and legacy clients.
  - Web standards and client documentation in `ui/client/`.
  - Do NOT commit IDE user files (`*.iws`, `.idea/`, `local.properties`). Use `local.properties.example` for SDK templates.
- **`web/` (Web Servers & Backends)**:
  - Web servers (Nginx, Apache, Caddy, Node.js, Tomcat).
  - Cloud backends (Google Cloud Platform, App Engine).
  - Java EE enterprise modules.

---

## Coding Standards

### Shell Scripts

- Use `#!/usr/bin/env bash` or `#!/usr/bin/env sh`.
- Quote variables to prevent word splitting: `"$VAR"`.
- Use functions and modular structure for complex tasks.
- Avoid committing hardcoded secrets, machine-specific paths, or user credentials.

### Kotlin & Java

- Adhere to the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) and [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
- Build with the included Gradle wrapper (`./gradlew build`).

### Documentation

- Use GitHub Flavored Markdown (`.md`) or AsciiDoc (`.adoc`).
- Use relative links for repository navigation.
- Keep table of contents and indexes up to date.

---

## License

By contributing to IntelliBitz, you agree that your contributions will be licensed under the project's [MIT License](./LICENSE).
