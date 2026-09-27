# Resolving GRUB Boot Issues After Ubuntu Upgrade

When dual-booting Ubuntu and Windows (10 or 11), a system upgrade may occasionally prevent GRUB from detecting or loading Windows. Follow the solutions below to restore boot entries.

---

## 1. Force Text-Based GRUB Terminal

Modify `/etc/default/grub`:

```bash
sudo nano /etc/default/grub
```

Set:

```ini
GRUB_TERMINAL=console
```

Update GRUB configuration:

```bash
sudo update-grub
# or
sudo update-grub2
```

---

## 2. Enable and Verify `os-prober`

Ensure `os-prober` is installed and permitted to detect other operating systems:

```bash
sudo apt install os-prober
sudo os-prober --debug
```

In `/etc/default/grub`, ensure `GRUB_DISABLE_OS_PROBER=false` is set:

```bash
echo "GRUB_DISABLE_OS_PROBER=false" | sudo tee -a /etc/default/grub
sudo update-grub
```

If needed, add a custom GRUB menu entry in `/etc/grub.d/40_custom`:

```bash
menuentry "Windows 10/11" {
    insmod part_gpt
    insmod fat
    search --no-floppy --fs-uuid --set=root <EFI-PARTITION-UUID>
    chainloader /EFI/Microsoft/Boot/bootmgfw.efi
}
```

---

## 3. Review BIOS / UEFI Settings

- Ensure **UEFI Boot** is enabled.
- Verify boot priority order: set Ubuntu or Windows Boot Manager as appropriate.
- **Secure Boot**: Temporarily disable Secure Boot in UEFI firmware if signature verification fails.

---

## 4. Check NTFS File System Health

Boot into Windows or a live environment and verify the NTFS filesystem integrity:

```cmd
chkdsk /f C:
```

---

## 5. Reinstall GRUB to the EFI System Partition

If GRUB is missing or corrupted:

```bash
sudo grub-install /dev/sda
sudo update-grub
```

_(Replace `/dev/sda` with your system disk)._
