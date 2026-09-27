REM https://learn.microsoft.com/en-us/windows/wsl/basic-commands
cmd
wsl --status
wsl --list --all
wsl --list --running
wsl --list --online
wsl -l --all
wsl -l -v

wsl --install
wsl --install -d Ubuntu-22.04

wsl -d <distro> & REM run this distro
wsl -d <distro> -u <user>
wsl -d Ubuntu

wsl --set-default <distro>
cd \\wsl$\<distro>
wsl ~ & REM wsl to start in the user's home directory

wsl hostname -i

wsl --system -d <distribution-name> df -h /mnt/wslg/distro

wsl --terminate <distro> & REM stop distro and all of its running processes (requires admin privilege)
wsl --shutdown & REM Immediately terminates all running distributions and the WSL 2 lightweight utility virtual machine
wsl --unregister <distro> & REM removes distro and any data within it

wsl ps -aux & REM runs unix commands on windows shell
wsl
docker.exe & REM runs windows commonds on unix shell

/etc/wsl.conf & REM per-distro local settings

sudo umount /mnt/e
sudo mount -t drvfs E: /mnt/e


