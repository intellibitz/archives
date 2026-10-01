https://help.ubuntu.com/stable/ubuntu-help/index.html

https://ubuntu.com/server/docs

SSH
--

SSH ("Secure SHell") is a protocol for securely accessing one computer from another. Despite the name, SSH allows you to run command line and graphical programs, transfer files, and even create secure virtual private networks over the Internet.

To use SSH, you will need to install an SSH client on the computer you connect from, and an SSH server on the computer you connect to. The most popular Linux SSH client and Linux SSH server are maintained by the OpenSSH project.

The OpenSSH client is included in Ubuntu by default. For information on connecting to an SSH server, see Connecting to an OpenSSH Server.

To install the OpenSSH server, install the following package: openssh-server.

Using the command-line
--

All modern Unix-like systems (Linux, OS X, BSDs, and others) include a command-line ssh client. To login to your computer from a Unix-like machine, go to a command-line and type:

    ssh <username>@<computer name or IP address>
    For example:

    ssh joe@laptop
    or:

    ssh mike@192.168.1.1

You should get the usual password prompt (or be told you can't log in, if passwords are disabled).

See ssh keys if you want to authenticate using keys instead of passwords.

SSH Keys
--

SSH allow authentication between two hosts without the need of a password. SSH key authentication uses a private key and a public key.

To generate the keys, from a terminal prompt enter:

    ssh-keygen -t rsa

This will generate the keys using the RSA Algorithm. At the time of this writing, the generated keys will have 3072 bits. You can modify the number of bits by using the -b option. For example, to generate keys with 4096 bits, you can do:

    ssh-keygen -t rsa -b 4096

During the process you will be prompted for a password. Simply hit Enter when prompted to create the key.

By default the public key is saved in the file ~/.ssh/id_rsa.pub, while ~/.ssh/id_rsa is the private key. Now copy the id_rsa.pub file to the remote host and append it to ~/.ssh/authorized_keys by entering:

    ssh-copy-id username@remotehost

Finally, double check the permissions on the authorized_keys file, only the authenticated user should have read and write permissions. If the permissions are not correct change them by:

    chmod 600 .ssh/authorized_keys

You should now be able to SSH to the host without being prompted for a password.

Import keys from public keyservers
--

These days many users have already ssh keys registered with services like launchpad or github. Those can be easily imported with:

    ssh-import-id <username-on-remote-service>

The prefix lp: is implied and means fetching from launchpad, the alternative gh: will make the tool fetch from github instead.

https://help.ubuntu.com/community/SSH/OpenSSH/Keys

With public key authentication, the authenticating entity has a public key and a private key. Each key is a large number with special mathematical properties. The private key is kept on the computer you log in from, while the public key is stored on the .ssh/authorized_keys file on all the computers you want to log in to. When you log in to a computer, the SSH server uses the public key to "lock" messages in a way that can only be "unlocked" by your private key - this means that even the most resourceful attacker can't snoop on, or interfere with, your session. As an extra security measure, most SSH programs store the private key in a passphrase-protected format, so that if your computer is stolen or broken in to, you should have enough time to disable your old public key before they break the passphrase and start using your key.

SSH programs generate public keys in a similar format:

    <ssh-rsa or ssh-dss> <really long string of nonsense> <username>@<host>

SSH can use either "RSA" (Rivest-Shamir-Adleman) or "DSA" ("Digital Signature Algorithm") keys.
RSA is the only recommended choice for new keys.

Key-based authentication uses two keys, one "public" key that anyone is allowed to see, and another "private" key that only the owner is allowed to see. To securely communicate using key-based authentication, one needs to create a key pair, securely store the private key on the computer one wants to log in from, and store the public key on the computer one wants to log in to.

Generating RSA Keys
--

The first step involves creating a set of RSA keys for use in authentication.

This should be done on the client.

To create your public and private SSH keys on the command-line:

    mkdir ~/.ssh
    chmod 700 ~/.ssh
    ssh-keygen -t rsa

You will be prompted for a location to save the keys, and a passphrase for the keys. This passphrase will protect your private key while it's stored on the hard drive:

Generating public/private rsa key pair.

    Enter file in which to save the key (/home/b/.ssh/id_rsa):
    Enter passphrase (empty for no passphrase):
    Enter same passphrase again:
    Your identification has been saved in /home/b/.ssh/id_rsa.
    Your public key has been saved in /home/b/.ssh/id_rsa.pub.
    Your public key is now available as .ssh/id_rsa.pub in your home folder.

Congratulations! You now have a set of keys. Now it's time to make your systems allow you to login with them.

Choosing a good passphrase
--

You need to change all your locks if your RSA key is stolen. Otherwise the thief could impersonate you wherever you authenticate with that key.

An SSH key passphrase is a secondary form of security that gives you a little time when your keys are stolen. If your RSA key has a strong passphrase, it might take your attacker a few hours to guess by brute force. That extra time should be enough to log in to any computers you have an account on, delete your old key from the .ssh/authorized_keys file, and add a new key.

Your SSH key passphrase is only used to protect your private key from thieves. It's never transmitted over the Internet, and the strength of your key has nothing to do with the strength of your passphrase.

The decision to protect your key with a passphrase involves convenience x security. Note that if you protect your key with a passphrase, then when you type the passphrase to unlock it, your local computer will generally leave the key unlocked for a time. So if you use the key multiple times without logging out of your local account in the meantime, you will probably only have to type the passphrase once.

If you do adopt a passphrase, pick a strong one and store it securely in a password manager. You may also write it down on a piece of paper and keep it in a secure place. If you choose not to protect the key with a passphrase, then just press the return when ssh-keygen asks.

Key Encryption Level
--

Note: The default is a 2048 bit key. You can increase this to 4096 bits with the -b flag (Increasing the bits makes it harder to crack the key by brute force methods).

    ssh-keygen -t rsa -b 4096

Transfer Client Key to Host
--

The key you need to transfer to the host is the public one. If you can log in to a computer over SSH using a password, you can transfer your RSA key by doing the following from your own computer:

    ssh-copy-id <username>@<host>

Where <username> and <host> should be replaced by your username and the name of the computer you're transferring your key to.

Another alternative is to copy the public key file to the server and concatenate it onto the authorized_keys file manually. It is wise to back that up first:

    cp authorized_keys authorized_keys_Backup
    cat id_rsa.pub >> authorized_keys

You can make sure this worked by doing:

    ssh <username>@<host>

You should be prompted for the passphrase for your key:

    Enter passphrase for key '/home/<user>/.ssh/id_rsa':

Enter your passphrase, and provided host is configured to allow key-based logins, you should then be logged in as usual.

To run Windows programs on Ubuntu, you can use a compatibility layer called Wine. Here are the steps to install and use Wine:

Install Wine: Most Linux distributions come with Wine in their package repository. You can install Wine on Ubuntu by running the following commands in the terminal12:
sudo dpkg --add-architecture i386
sudo apt update
sudo apt install wine

Run Windows Application: Once Wine is installed, you can download the Windows application’s installer (EXE or MSI file). To run the installer, use the following command in the terminal2:
wine /path/to/application.exe
If you have an MSI file instead, use the following command to install it2:
wine msiexec /i /path/to/installer.msi

Use Wine: After installation, you can run the Windows application from the terminal or find its shortcuts in your applications menu2.
Please note that Wine is a work in progress, so it might not run every application perfectly2. If you encounter any issues, you can check the Wine Application Database for solutions and tweaks
