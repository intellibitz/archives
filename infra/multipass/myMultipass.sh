#https://multipass.run
#https://multipass.run/docs
#https://github.com/canonical/multipass

#installs multipass
sudo snap install multipass
ls -l /var/snap/multipass/common/multipass_socket
groups | grep sudo
snap info multipass

#configures multipass external storage
sudo snap stop multipass
sudo snap connect multipass:removable-media
sudo mkdir /etc/systemd/system/snap.multipass.multipassd.service.d/
sudo tee /etc/systemd/system/snap.multipass.multipassd.service.d/override.conf <<EOF
[Service]
# Environment=MULTIPASS_STORAGE=<path>
EOF
sudo systemctl daemon-reload
# sudo cp -r /var/snap/multipass/common/data/multipassd <path>/data
# sudo cp -r /var/snap/multipass/common/cache/multipassd <path>/cache
sudo snap start multipass

#setup graphical interface
multipass shell
sudo apt install ubuntu-desktop 
# sudo apt install kubuntu-desktop 
# sudo apt install xfce4 xfce-goodies
sudo apt install xrdp
sudo passwd ubuntu
multipass list #get the ip addr
ip addr
# remmina -c rdp://<ipaddr>

multipass help
# multipass help <command>

multipass find
multipass find --only-blueprints

multipass ls #lists multipass VMs and their IPs
multipass list
# multipass info <name>

# multipass launch <remote>:<image>
multipass launch #--cpus 1 --memory 1G --disk 5G
multipass launch ubuntu
# multipass launch <blueprint-name>
multipass launch 23.10 --name xrdp-mantic --cpus 4 --memory 8G --disk 60G

# multipass launch docker --name <name> #creates new docker VMs
multipass launch docker --name docker --cpus 4 --memory 8G --disk 50G --bridged
multipass exec docker docker

# multipass shell <name> #login to a multipass VM
exit #logout
# multipass exec <name> -- lsb_release -a
# multipass exec <name> -- pwd

# multipass exec <name> -- bash -c "echo `cat ~/.ssh/id_rsa.pub` >> ~/.ssh/authorized_keys"
# ssh -X ubuntu@<ipaddr>

# multipass start <name1> <name2> <name3>
multipass start --all
# multipass start <name>

# multipass suspend <name>
# multipass stop <name>
# multipass restart <name>
# multipass delete <name>
multipass delete --all #moves all instances to trash
multipass purge #deletes all instances in trash
# multipass delete --purge <name> #permanently remove instance

multipass launch docker --name node1
multipass shell node1

multipass launch --cpus 4 --disk 20G --memory 8G
# multipass set <name>.cpus=4
# multipass set <name>.disk=40G
# multipass set <name>.memory=12G

# multipass set client.primary-name=<instance-name>
multipass get --keys

sudo apt update && sudo apt install -y samba-common

# multipass mount $HOME <name>
# multipass mount $HOME <name>:</path>
# multipass launch --mount </path>:</path>
# multipass transfer <name>:</path/file> </path> #copies file around
# multipass umount <name>

multipass alias
multipass unalias
multipass unalias --all
multipass aliases

# multipass set local.driver=qemu(default)|lxd|libvirt
sudo apt install libvirt-daemon-system
sudo snap connect multipass:libvirt #connects the libvirt interface/plug
multipass stop --all
# multipass set local.driver=libvirt
multipass stop --all
# multipass set local.driver=qemu

multipassd.exe /svc --verbosity debug #windows
C:\ProgramData\Multipass #shared dir

# multipass set local.driver=hyperv(default)|virtualbox
multipassd --help #multipass service daemon
# multipassd --verbosity <error|warning|info|debug|trace>
multipass -v #increase verbosity by repeating v, max -vvvv

multipass set local.privileged-mounts=on|yes|1|true
multipass set local.passphrase=admin
multipass authenticate admin
multipass auth admin

multipass set client.apps.windows-terminal.profiles=primary #integrates with terminal
multipass set client.apps.windows-terminal.profiles=none #reverts

