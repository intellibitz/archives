# Expect Automation Scripts

Automated interactive sessions using [Expect / Tcl](https://core.tcl-lang.org/expect/index).

---

## Prerequisites

Ensure `expect` is installed:

```bash
sudo apt install expect
```

---

## Scripts

### 1. `expect-openvpn.exp`

Automates VPN credential entry and connection initialization:

```bash
./expect-openvpn.exp <username> <password> <config.ovpn>
```

### 2. `expect-sftp.exp`

Automates non-interactive SFTP authentication:

```bash
./expect-sftp.exp <username> <password> <host>
```
