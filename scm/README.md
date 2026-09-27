# Source Control Management & DevOps (`scm/`)

This directory contains infrastructure automation, continuous integration toolchains, containerization setups, virtualization recipes, and developer environment management.

---

## Tooling Matrix

| Domain               | Path                                                                                                                                                                                                                                                   | Key Capabilities                                                                                                                                                                                                                                                                                                                                                                                                     |
| :------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Git & Platforms**  | [`git/`](./scm/git)                                                                                                                                                                             | AsciiDoc Pro Git guide ([`git-scm.org.adoc`](./scm/git/git-scm.org.adoc)), GitHub CLI automation (`github/`), GitLab (`gitlab/`), Bitbucket (`bitbucket/`).                                                                                                                                                                                                   |
| **Docker**           | [`docker/`](./scm/docker)                                                                                                                                                                       | Docker Engine installation, rootless Docker ([`dockerinstall-rootless.sh`](./scm/docker/dockerinstall-rootless.sh)), Docker Desktop ([`install-dockerdesktop.md`](./scm/docker/install-dockerdesktop.md)), multi-service Compose files, Dockerfile templates.                                          |
| **Kubernetes**       | [`kubernetes/`](./scm/kubernetes)                                                                                                                                                               | `kubectl` CLI installation via official community repository `pkgs.k8s.io` ([`bin/kubectlinstall.sh`](./scm/kubernetes/bin/kubectlinstall.sh)), cluster operations reference ([`myKubectl.md`](./scm/kubernetes/myKubectl.md)).                                                                        |
| **Multipass VMs**    | [`multipass/`](./scm/multipass)                                                                                                                                                                 | Canonical Multipass VM orchestration, Docker blueprint instances, PostgreSQL and XRDP desktop VMs.                                                                                                                                                                                                                                                                                                                   |
| **Gradle**           | [`gradle/`](./scm/gradle)                                                                                                                                                                       | Comprehensive AsciiDoc Gradle guides ([`gradle.org.adoc`](./scm/gradle/gradle.org.adoc)), sample multi-project builds ([`GradleAuthoring/`](./scm/gradle/GradleAuthoring), [`GradleRunning/`](./scm/gradle/GradleRunning)), Kotlin DSL reports. |
| **Package Managers** | [`brew/`](./scm/brew)<br>[`flatpak/`](./scm/flatpak)<br>[`sdkman/`](./scm/sdkman) | Homebrew automated installer ([`install-brew.sh`](./scm/brew/install-brew.sh)), Flatpak flathub configs, SDKMAN multi-JDK manager.                                                                                                                                                                                                                            |
| **IDEs**             | [`ide/`](./scm/ide)                                                                                                                                                                             | JetBrains Toolbox and NetBeans environment setup.                                                                                                                                                                                                                                                                                                                                                                    |

---

## DevOps Quickstarts

### Install Docker Engine on Ubuntu

```bash
./scm/docker/dockerinstall.sh
```

### Setup Rootless Docker

```bash
./scm/docker/dockerinstall-rootless.sh
```

### Launch an Ubuntu VM via Multipass

```bash
./scm/multipass/launchMpDocker.sh
```
