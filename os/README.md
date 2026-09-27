# Operating Systems & Language Runtimes (`os/`)

This directory provides system administration scripts, environment configurations, security recipes, and language runtimes across Linux, Windows/WSL, and multiple programming language ecosystems.

---

## Directory Breakdown

### 1. [`linux/`](./os/linux) - Linux System Administration

- **Ubuntu**: System updates (`apt-full-upgrade.sh`), server hardening (`myUbuntuServer.sh`), Samba network shares (`mySamba.sh`), and desktop setup.
- **Arch Linux**: Official distribution architecture and installation guides (`arch/archlinux.org.adoc`).
- **SSH & GPG Security**: OpenSSH configuration (`ssh/openssh.com.adoc`, `ssh/mySSH.md`), GnuPG key management (`gpg/gnupg.org.adoc`, `gpg/myGpg.md`).
- **Shell Utilities**: Process management (`psgrep.sh`, `psgrepkill.sh`), file change listeners (`stat-cmd-loop-whenfilechange.sh`), disk space monitors (`ndf.sh`), Zsh and Bash configurations.
- **Bootloader**: GRUB dual-boot recovery guide ([`grub.md`](./os/linux/grub.md)).
- **Remote Desktop**: Xrdp configuration and remote session management (`xrdp/myXrdp.sh`).

### 2. [`win/`](./os/win) - Windows & WSL Environment

- Windows Subsystem for Linux (WSL) setup and troubleshooting (`myWsl.md`, `myWsl.bat`).
- Windows Apache service management scripts (`sc-httpd.bat`).

### 3. [`lang/`](./os/lang) - Programming Languages & Toolchains

| Language        | Path                                                                                          | Highlights                                                                                                                                                                                                                                                     |
| :-------------- | :-------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Kotlin**      | [`lang/kotlin/`](./os/lang/kotlin)     | Kotlin tutorial suite (`KotlinLearn`), playground (`KotlinPlay`), multiplatform modules (`multiplatform`), web clients (`jsfront`, `mpfsweb`).                                                                                                                 |
| **Java SE**     | [`lang/javase/`](./os/lang/javase)     | Tamil font transliterator desktop utility (`FontTransliterator`), text editors (`sted`), JDK 7 toolchains.                                                                                                                                                     |
| **Python & AI** | [`lang/python/`](./os/lang/python)     | [uv modern package manager](./os/lang/python/uv.md) (`uv.sh`), Python 3.12/3.13, Hugging Face Hub CLI guide (`huggingface.md`), Google Colab & Gemini API (`colab.md`), Meta Llama 3 (`meta-llama.md`). |
| **Go**          | [`lang/go/`](./os/lang/go)             | Go modules, workspaces (`go.work`), testing, and packaging (`myGo.sh`).                                                                                                                                                                                        |
| **Clojure**     | [`lang/clojure/`](./os/lang/clojure)   | Clojure Reader, data structures, and evaluation guide (`myClojure.md`).                                                                                                                                                                                        |
| **Expect**      | [`lang/expect/`](./os/lang/expect)     | Interactive session automation for OpenVPN and SFTP (`expect-openvpn.exp`, `expect-sftp.exp`).                                                                                                                                                                 |
| **PHP**         | [`lang/php/`](./os/lang/php/README.md) | Community portal and interactive web applications ([`intelligeek`](./os/lang/php/intelligeek)).                                                                                                         |

---

## Licensing

Distributed under the repository's root **[MIT License](./LICENSE)**.
