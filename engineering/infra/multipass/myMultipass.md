https://multipass.run
https://multipass.run/docs
https://github.com/canonical/multipass

    $ multipass set client.primary-name=first
    $ multipass start
        Launched: first
    Mounted '/home/ubuntu' into 'first:Home'
    $ multipass stop
    $ multipass launch eoan
        Launched: calm-chimaera
    $ multipass set client.primary-name=calm-chimaera
    $ multipass suspend
    $ multipass set client.primary-name=chopin
    $ multipass start
        Launched: chopin
        Mounted '/home/ubuntu' into 'chopin:Home'
    $ multipass list
    Name                    State             IPv4             Image
    chopin                  Running           10.122.139.63    Ubuntu 18.04 LTS
    calm-chimaera           Suspended         --               Ubuntu 19.04
    first                   Stopped           --               Ubuntu 18.04 LTS

To install Multipass, simply execute:
--

    $ snap install multipass

For architectures other than amd64, you’ll need the beta channel at the moment.

You can also use the edge channel to get the latest development build:

    $ snap install multipass --edge

Make sure you’re part of the group that Multipass gives write access to its socket (sudo in this case, but it may also be adm or admin, depending on your distribution):

    $ ls -l /var/snap/multipass/common/multipass_socket
    srw-rw---- 1 root sudo 0 Dec 19 09:47 /var/snap/multipass/common/multipass_socket

    $ groups | grep sudo
    adm cdrom sudo dip plugdev lpadmin

You can check some details about the snap with the snap info command:

    $ snap info multipass
    name:      multipass
    summary:   Instant Ubuntu VMs
    publisher: Canonical✓
    store-url: https://snapcraft.io/multipass
    contact:   https://github.com/CanonicalLtd/multipass/issues/new
    license:   GPL-3.0
    description: |
    Multipass is a tool to launch and manage VMs on Windows, Mac and Linux that simulates a cloud
    environment with support for cloud-init. Get Ubuntu on-demand with clean integration to your IDE
    and version control on your native platform.
    ...
    commands:
    - multipass.gui
    - multipass
    services:
    multipass.multipassd: simple, enabled, active
    snap-id:      mA11087v6dR3IEcQLgICQVjuvhUUBUKM
    tracking:     latest/candidate
    refresh-date: 5 days ago, at 10:13 CEST
    channels:
    latest/stable:    1.3.0                 2020-06-17 (2205) 228MB -
    latest/candidate: 1.3.0                 2020-06-17 (2205) 228MB -
    latest/beta:      1.3.0-dev.17+gf89e1db 2020-04-28 (2019) 214MB -
    latest/edge:      1.4.0-dev.83+g149f10a 2020-06-17 (2216) 228MB -
    installed:          1.3.0                            (2205) 228MB -

As the installation happened via snap, you don’t need to worry about upgrading—it will be done automatically.

To uninstall Multipass, simply run:

    snap remove multipass

Run
--

You’ve installed Multipass. Time to run your first commands! Use multipass version to check your version or multipass launch to create your first instance.

https://multipass.run/docs/authenticate-command
The Windows host uses a TCP socket listening on port 50051 for client connections.

To ease the burden of having to authenticate the client, the user who installs the updated version of Multipass will automatically have their clients authenticated with the service. Any other users connecting to the service will have to use authenticate using the previously set local.passphrase.

Linux and macOS hosts currently use a Unix domain socket for client and daemon communication. Upon first use, this socket only allows a client to connect via a user who belongs to the particular group the socket is owned by. For example, this group could be sudo, admin, or wheel and the user needs to belong to this group or else permission will be denied when connecting.

After the first client connects with a user belonging to the socket’s admin group, the client’s OpenSSL certificate will be accepted by the daemon and the socket will be then be open for all users to connect. Any other user trying to connect to the Multipass service will need to authenticate with the service using the previously set local.passphrase.

https://multipass.run/docs/set-up-the-driver
By default, Multipass on Linux uses the qemu or lxd driver (depending on the architecture). However, if you want more control over your VMs after they are launched, you can also use the experimental libvirt driver.

By default, Multipass on Windows uses the hyperv driver. However, if you want to (or have to), you can change the hypervisor that Multipass uses to VirtualBox.

https://multipass.run/docs/authenticating-clients
A passphrase needs to be set by the administrator in order for clients to authenticate with the Multipass service. The client setting the passphrase will need to already be authenticated. There are two ways to set the passphrase.

A client that is not authorized to connect to the Multipass service will fail when running multipass commands. An error will be displayed when this happens.

In case client cannot authorize and the passphrase cannot be set
--

It is possible that another client that is privileged to connect to the Multipass socket will connect first and make it seemingly impossible to set the local.passphrase and also authorize the client with the service. One will see something like the following:

    $ multipass list
    list failed: The client is not authenticated with the Multipass service.
    Please use 'multipass authenticate' before proceeding.
    $ multipass authenticate
    Please enter passphrase:
    authenticate failed: Passphrase is not set. Please `multipass set local.passphrase` with a trusted client.
    $ multipass set local.passphrase
    Please enter passphrase:
    Please re-enter passphrase:
    set failed: The client is not authenticated with the Multipass service.
    Please use 'multipass authenticate' before proceeding.

and then it seems impossible to authorize the client to connect to the service. This may not even work when using sudo.

The following workaround should help get out of this situation:

    $ cat ~/snap/multipass/current/data/multipass-client-certificate/multipass_cert.pem | sudo tee -a /var/snap/multipass/common/data/multipassd/authenticated-certs/multipass_client_certs.pem > /dev/null
    $ snap restart multipass

You may need sudo with this last command: sudo snap restart multipass.

At this point, your client should be authenticated with the Multipass service.

https://multipass.run/docs/configure-multipass-storage
First, open an administrator privileged PowerShell prompt.

Stop the Multipass daemon:

    PS> Stop-Service Multipass

Create and set the new storage location, replacing <path> with the absolute path of your choice:

    PS> New-Item -ItemType Directory -Path "<path>"
    PS> Set-ItemProperty -Path "HKLM:System\CurrentControlSet\Control\Session Manager\Environment" -Name MULTIPASS_STORAGE -Value "<path>"

Now you can transfer the data from its original location to the new location:

    PS> Copy-Item -Path "C:\ProgramData\Multipass\*" -Destination "<path>" -Recurse

It is important to copy any existing data to the new location. This avoids unauthenticated client issues, permission issues, and in general, to have any previously created instances available.

Finally, start the Multipass daemon:

    PS> Start-Service Multipass

You can delete the original data at your discretion, to free up space:

    PS> Remove-Item -Path "C:\ProgramData\Multipass\*" -Recurse

Remove the setting for the custom storage location:

    PS> Remove-ItemProperty -Path "HKLM:System\CurrentControlSet\Control\Session Manager\Environment" -Name MULTIPASS_STORAGE

Now you can transfer the data back to its original location:

    PS> Copy-Item -Path "<path>\*" -Destination "C:\ProgramData\Multipass" -Recurse

Finally, start the Multipass daemon:

    PS> Start-Service Multipass

You can delete the data from the custom location at your discretion, to free up space:

    PS> Remove-Item -Path "<path>" -Recurse

https://multipass.run/docs/how-to-use-multipass-remotely-a-preview
You can systemctl edit snap.multipass.multipassd.service and place content along these lines (replace <hostname> with the hostname or the IP you want it to listen on) in:

    [Service]
    ExecStart=
    ExecStart=/usr/bin/snap run multipass.multipassd --address <hostname>:51005
    Restart the service then:

    $ snap stop multipass
    $ snap start multipass

The client accepts the MULTIPASS_SERVER_ADDRESS environment variable that overrides the default:

    $ MULTIPASS_SERVER_ADDRESS=<hostname>:51001 multipass find
    Image                       Aliases           Version          Description
    ...
    21.10                       impish            20220118         Ubuntu 21.10

Caveats:
--

Because mounts are executed as privileged users, it is recommended to use client authentication, so you can explicitly allow clients access with a shared passphrase.
Alternatively, you can multipass set local.privileged-mounts=false to disable the mounts feature altogether.
Additionally, the mount command takes a target filepath which is resolved daemon-side, meaning that directories are mounted from the system that the daemon is running on, not the client.
Some commands (shell, exec, mount) currently rely on direct networking between the client and the instance, for those to work you’ll need to ensure routing between them is possible.

https://discourse.ubuntu.com/t/how-to-improve-mounts-performance-in-multipass/26638
$ sudo apt update && sudo apt install -y samba-common
Then, editing the file /etc/samba/smb.conf lets us add shares, adding entries like the following:

[test_smb_mount]
comment = smb mount test
path = /my_path/
read only = no
browsable = yes
kernel oplocks = yes

Mount a folder shared with SMB on an instance
--

Once the host operating system is sharing the folder, we need to mount it on the instance. For this, the package cifs-tools is needed, which can be installed with

$ sudo apt update && sudo apt install -y cifs-utils
Mounting is finally done in the command line by

$ sudo mount -t cifs //hostname/my_path_or_share_name mount_folder/ -o user=my_name,uid=1000
Where my_name is the user sharing the folder on the host. The password will be asked in the terminal and after entering it, we are done.

Optionally, we can add a line to /etc/fstab to make the operating system automatically mount the folder at boot, or at least not having the need to specify the mount name and options. The line should read:

//hostname/my_path_or_share_name mount_folder/ cifs user=my_name,uid=1000 0 0
Entries in this file are space or tab separated. The first one is the share name or path, the second one is the directory where to mount the folder, the third one is the mount type, the fourth one is the comma-separated list of options (add noauto to avoid mounting at boot) and the two last options better remain as zero.

In case noauto is specified in the options, the folder should be mounted with

$ sudo mount mount_folder/

virtio-fs mounts
--

If using the LXD backend on Linux, we can benefit from a performant file system mount, at the expense of not being able to mount it while the instance is running. A folder is mounted on an instance with the command

$ lxc --project multipass config device add lxdinstance mount_lxd disk source=/my_path path=//mount_folder
where lxdinstance is the name of the instance, mount_lxd is an arbitrary device name for the mount, source specifies the path to share and path specifies the directory where the source is to be mounted. With only this command, LXD takes care of everything: no need to run commands on the instance.

$ sudo apt update && sudo apt install -y nfs-kernel-server
To share a folder, we use the following command:

$ sudo exportfs *:/my_path
where the * means “export to any host”; we can specify a host name or IP address there.

Mount a folder shared with NFS on an instance
--

We would first need to install the NFS client in the instance, with the commands

$ sudo apt update && sudo apt install -y nfs-common
then, we can mount the shared folder with

$ sudo mount -t nfs HOST_IP:/my_path /mount_folder -o user=host_user,uid=instance_uid,gid=instance_gid

Configuring a new storage location
--

Caveats:

Multipass will not migrate your existing data, but this article explains how to do it manually. If you do not transfer the data, you will have to re-download any Ubuntu images and reinitialize any instances that you need.
Subsequently, when uninstalling Multipass, the uninstaller will not remove data stored in custom locations, which must be deleted manually.

Linux
First, stop the Multipass daemon:

    $ sudo snap stop multipass

Depending on where the new storage directory is located you will need to connect the respective interface to the Multipass snap. Because of snap confinement, this directory needs to located in either /home or one of the removable mounts points:

    $ sudo snap connect multipass:removable-media # for /mnt or /media
    $ sudo snap connect multipass:all-home # for /home/*

Then, create the new directory in which Multipass will store its data:

    $ mkdir -p <path>
    $ sudo chown root <path>

After that, create the override config file, replacing <path> with the absolute path of the directory created above.

    $ sudo mkdir /etc/systemd/system/snap.multipass.multipassd.service.d/
    $ sudo tee /etc/systemd/system/snap.multipass.multipassd.service.d/override.conf <<EOF
    [Service]
    Environment=MULTIPASS_STORAGE=<path>
    EOF
    $ sudo systemctl daemon-reload

Now you can transfer the data from its original location to the new location:

    $ sudo cp -r /var/snap/multipass/common/data/multipassd <path>/data
    $ sudo cp -r /var/snap/multipass/common/cache/multipassd <path>/cache

Finally, start the Multipass daemon:

    $ sudo snap start multipass

You can delete the original data at your discretion, to free up space:

    $ sudo rm -rf /var/snap/multipass/common/data/multipassd
    $ sudo rm -rf /var/snap/multipass/common/cache/multipassd

First, stop the Multipass daemon:

    $ sudo snap stop multipass

Depending on where the new storage directory is located you will need to connect the respective interface to the Multipass snap. Because of snap confinement, this directory needs to located in either /home or one of the removable mounts points:

    $ sudo snap connect multipass:removable-media # for /mnt or /media
    $ sudo snap connect multipass:all-home # for /home/*

Then, create the new directory in which Multipass will store its data:

    $ mkdir -p <path>
    $ sudo chown root <path>

After that, create the override config file, replacing <path> with the absolute path of the directory created above.

    $ sudo mkdir /etc/systemd/system/snap.multipass.multipassd.service.d/
    $ sudo tee /etc/systemd/system/snap.multipass.multipassd.service.d/override.conf <<EOF
    [Service]
    Environment=MULTIPASS_STORAGE=<path>
    EOF
    $ sudo systemctl daemon-reload

Now you can transfer the data from its original location to the new location:

    $ sudo cp -r /var/snap/multipass/common/data/multipassd <path>/data
    $ sudo cp -r /var/snap/multipass/common/cache/multipassd <path>/cache

Finally, start the Multipass daemon:

    $ sudo snap start multipass

You can delete the original data at your discretion, to free up space:

    $ sudo rm -rf /var/snap/multipass/common/data/multipassd
    $ sudo rm -rf /var/snap/multipass/common/cache/multipassd

Stop the Multipass daemon:

    $ sudo snap stop multipass

Although not required, to make sure that Multipass does not have access to directories that it shouldn’t, disconnect the respective interface depending on where the custom storage location was set:

    $ sudo snap disconnect multipass:removable-media # for /mnt or /media
    $ sudo snap disconnect multipass:all-home # for /home/*

Then, remove the override config file:

    $ sudo rm /etc/systemd/system/snap.multipass.multipassd.service.d/override.conf
    $ sudo systemctl daemon-reload

Now you can transfer your data from the custom location back to its original location:

    $ sudo cp -r <path>/data /var/snap/multipass/common/data/multipassd
    $ sudo cp -r <path>/cache /var/snap/multipass/common/cache/multipassd

Finally, start the Multipass daemon:

    $ sudo snap start multipass

You can delete the data from the custom location at your discretion, to free up space:

    $ sudo rm -rf <path>

Get involved!
--

Here's a set of steps to build and run your own build of Multipass. Please note that the following instructions are for building Multipass for Linux only. These instructions do not support building packages for macOS or Windows systems.

Build Dependencies
--

    cd <multipass>
    apt install devscripts equivs
    mk-build-deps -s sudo -i
    Building
    cd <multipass>
    git submodule update --init --recursive
    mkdir build
    cd build
    cmake ../
    make

Running Multipass daemon and client
--

First, install multipass's runtime dependencies. On amd64 architecture, you can achieve that with:

    sudo apt update
    sudo apt install libgl1 libpng16-16 libqt6core6 libqt6gui6 \
        libqt6network6 libqt6widgets6 libxml2 libvirt0 dnsmasq-base \
        dnsmasq-utils qemu-system-x86 qemu-utils libslang2 iproute2 \
        iptables iputils-ping libatm1 libxtables12 xterm

Then run multipass's daemon:

    sudo <multipass>/build/bin/multipassd &

Copy the desktop file multipass clients expect to find in your home:

    mkdir -p ~/.local/share/multipass/
    cp <multipass>/data/multipass.gui.autostart.desktop ~/.local/share/multipass/

Optionally, enable auto-complete in bash:

    source <multipass>/completions/bash/multipass

Finally, use multipass's clients:

    <multipass>/build/bin/multipass launch --name foo  # CLI client
    <multipass>/build/bin/multipass.gui                # GUI client

Install Multipass
--

On Linux it's available as a snap:

sudo snap install multipass
For macOS, you can download the installers from GitHub or use Homebrew:

#Note, this may require you to enter your password for some sudo operations during install
On Windows, download the installer from GitHub.

Usage
--

Find available images

    $ multipass find
    Image                       Aliases           Version          Description
    core                        core16            20200213         Ubuntu Core 16
    core18                                        20200210         Ubuntu Core 18
    16.04                       xenial            20200721         Ubuntu 16.04 LTS
    18.04                       bionic,lts        20200717         Ubuntu 18.04 LTS
    20.04                       focal             20200720         Ubuntu 20.04 LTS
    daily:20.10                 devel,groovy      20200721         Ubuntu 20.10

Launch a fresh instance of the current Ubuntu LTS

    $ multipass launch ubuntu
    Launching dancing-chipmunk...
    Downloading Ubuntu 18.04 LTS..........
    Launched: dancing chipmunk

Check out the running instances

    $ multipass list
    Name                    State             IPv4             Release
    dancing-chipmunk        RUNNING           10.125.174.247   Ubuntu 18.04 LTS
    live-naiad              RUNNING           10.125.174.243   Ubuntu 18.04 LTS
    snapcraft-asciinema     STOPPED           --               Ubuntu Snapcraft builder for Core 18

Learn more about the VM instance you just launched

    $ multipass info dancing-chipmunk
    Name:           dancing-chipmunk
    State:          RUNNING
    IPv4:           10.125.174.247
    Release:        Ubuntu 18.04.1 LTS
    Image hash:     19e9853d8267 (Ubuntu 18.04 LTS)
    CPU(s):         1
    Load:           0.97 0.30 0.10
    Disk usage:     1.1G out of 4.7G
    Memory usage:   85.1M out of 985.4M

Connect to a running instance

    $ multipass shell dancing-chipmunk
    Welcome to Ubuntu 18.04.1 LTS (GNU/Linux 4.15.0-42-generic x86_64)

    ...
    Don't forget to logout (or Ctrl-D) or you may find yourself heading all the way down the Inception levels... ;)

Run commands inside an instance from outside

    $ multipass exec dancing-chipmunk -- lsb_release -a
    No LSB modules are available.
    Distributor ID:  Ubuntu
    Description:     Ubuntu 18.04.1 LTS
    Release:         18.04
    Codename:        bionic

Stop an instance to save resources

    $ multipass stop dancing-chipmunk

Delete the instance

    $ multipass delete dancing-chipmunk

It will now show up as deleted:

    Name                    State             IPv4             Release
    snapcraft-asciinema     STOPPED           --               Ubuntu Snapcraft builder for Core 18
    dancing-chipmunk        DELETED           --               Not Available

And when you want to completely get rid of it:

    $ multipass purge

Get help

    multipass help
    multipass help <command>

Using RDP
--

The images used by Multipass do not come with a graphical desktop installed. For this reason, a desktop environment must be installed (we use ubuntu-desktop but there are as many other options as flavors of Ubuntu exist), along with the RDP server (we will use here xrdp but there are also other options such as freerdp). For this, we must log in to the running Multipass instance first:

    $ multipass shell headbanging-squid

and, once inside the instance,

    $ sudo apt update
    $ sudo apt install ubuntu-desktop xrdp

Then, we need a user with a password in order to log in. One possibility is to set a password to the default ubuntu user.

    $ sudo passwd ubuntu

We will be asked to enter and re-enter a password. And we are done on the server side.

We then quit the Ubuntu shell on the instance with the logout command and find out in the host the IP address to connect to:

    $ multipass list

    Name                    State             IPv4             Image
    headbanging-squid       Running           10.49.93.209     Ubuntu 22.04 LTS

Thus, we will use the IP address 10.49.93.209 to connect to the RDP server on the instance.

If the IP address of the instance is not displayed in the output of multipass list, it can be obtained directly from the instance, with the command ip addr.

On Linux
--

On Linux, there are applications such as Remmina to visualize the desktop (make sure the package remmina-plugin-rdp is installed in your host along with remmina).

To directly launch the client, run the following:

    $ remmina -c rdp://10.49.93.209

The system will ask for username (ubuntu) and the password set above, and then the Ubuntu desktop on the instance will be displayed.

Logging in to the RDP server with Remmina

On Windows
--

On Windows, we can connect to the RDP server with the “Remote Desktop Connection” application. There, we enter the virtual machine’s IP address, set the session to XOrg and enter the username and password we created on the previuos step. And we are done… a graphical desktop!

Using X11 forwarding
--

It might be the case that we only want Multipass to launch one application and to see only that window, without having the need for a complete desktop. It turns out that this setup is simpler than the RDP approach, because we do not need the Multipass instance to deploy a full desktop. Instead, we can use X11 to connect the applications in the instance with the graphical capabilities of the host.

On Linux
--

Linux runs X by default, so no extra software in the host is needed. We have the possibility here to be a bit more secure than on Windows, by using authentication in X forwarding. However, we will forward through ssh in order to avoid struggling with xauth stuff. We will allow our user in the host to log in to the Multipass instance through ssh, so that we can pass extra parameters to it. We can achieve that by copying our public key, in file ~/.ssh/id_rsa.pub to the list of authorized keys of the instance, in file ~/.ssh/authorized_keys (replace the example instance name with yours):

    $ multipass exec rocking-squirrel -- bash -c "echo `cat ~/.ssh/id_rsa.pub` >> ~/.ssh/authorized_keys"

If the file ~/.ssh/id_rsa.pub does not exist, it means that the SSH keys must be created. Use ssh-keygen to create them and retry the copy.

Then, check the IP address of the instance, using multipass info rocking-squirrel. Finally, we can log in to the instance using X forwarding doing

#replace `xx.xx.xx.xx` with the IP address obtained above

    $ ssh -X ubuntu@xx.xx.xx.xx

And test the setting running on the instance some program:

    $ sudo apt -y install x11-apps
    $ xlogo &

xlogo on Linux

A small window containing the X logo must show up. Done!

On Windows
--

Windows knows nothing about X, therefore we need to install an X server. Here we will use VcXsrv. Other options would be Xming (however, newest versions are paid but older versions can still be downloaded for free from their SourceForge site) or installing an X server in Cygwin.

The first step would be thus to install VcXsrv and run the X server through the newly created start menu entry “XLaunch”. Some options will be displayed. In the first screen, we should choose “Multiple windows” and set the display number; leaving it in -1 is a safe option. The “Next” button brings us to the “Client startup” window, on which we should choose “Start no client”. “Next” will show us the “Extra settings”, and there we should activate the option “Disable access control”. Pressing “Next” will give us then the option to save the settings, and finally we can start the X server. An icon will show up in the dock: we are done with the X server.

To configure the client (that is, the Multipass instance) we will need the host IP address, which can be obtained with the console command ipconfig. Then start the instance and set the DISPLAY environment variable to the server display on the host IP:

#replace `xx.xx.xx.xx` with the IP address obtained above

    $ export DISPLAY=xx.xx.xx.xx:0.0

We are done, and we can test forwarding using xlogo as in the Windows section.
