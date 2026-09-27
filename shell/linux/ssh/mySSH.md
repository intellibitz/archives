http://www.unixwiz.net/techtips/ssh-agent-forwarding.html

Ordinary Password Authentication
--

SSH supports access with a username and password, and this is little more than an encrypted telnet. Access is, in fact, just like telnet, with the normal username/password exchange.

We'll note that this exchange, and all others in this paper, assume that an initial exchange of host keys has been completed successfully. Though an important part of session security, host validation is not material to the discussion of agent key forwarding.

All examples start from a user on homepc (perhaps a Windows workstation) connecting with PuTTY to a server running OpenSSH. The particular details (program names, mainly) vary from implementation to implementation, but the underlying protocol has been proven to be highly interoperable.

Public Key Access
--

Note - older versions of OpenSSH stored the v2 keys in authorized_keys2 to distinguish them from v1 keys, but newer versions use either file.

To counteract the shortcomings of password authentication, ssh supports public key access. A user creates a pair of public and private keys, and installs the public key in his $HOME/.ssh/authorized_keys file on the target server. This is nonsensitive information which need not be guarded, but the other half — the private key — is protected on the local machine by a (hopefully) strong passphrase.

A public key is a long string of bits encoded in ASCII, and it's stored on one long line (though represented here on three continued lines for readability). It includes a type (ssh-rsa, or others), the key itself, and a comment:

    $HOME/.ssh/authorized_keys

    ssh-rsa AzAAB3NzaC1yc2EaaaabiWaaaieaX9AyNR7xWnW0eI3x2NGXrJ4gkQpK/EqpkveGCvvbM \
    oH84zqu3Us8jSaQD392JZAEAhGSoe0dWMBFm9Y41VGZYmncwkfTQPFH1P07vDw49aTAa2RJNFyV \
    QANZCbSocDeuT0Q7usuUj/v8h27+PqsUUl9XVQSDIhXBkWV+bJawc1c= Steve's key

This key must be installed on the target system — one time — where it is used for subsequent remote access by the holder of the private key.

Public Key Access with Agent support
--

Now that we've taken the leap into public key access, we'll take the next step to enable agent support. In the previous section, the user's private key was unlocked at every connection request: this is not functionally different from typing a password, and though it's the same passphrase every time (which makes it habitual), it nevertheless gets tedious in the same manner.

Fortunately, the ssh suite provides a broker known as a "key agent" which can hold and manage private keys on your workstations, and responding to requests from remote systems to verify your keys. Agents provide a tremendous productivity benefit, because once you've unlocked your private key (one time when you launch the agent), subsequent access works with the agent without prompting.

Public Key Access with Agent Forwarding
--

With our Key Agent in place, agent forwarding.. allows a chain of ssh connections to forward key challenges back to the original agent, obviating the need for passwords or private keys on any intermediate machines.

http://www.OpenSSH.com

Secure shell (SSH) is a protocol for creating an encrypted communications channel between two networked hosts. SSH protects data passing between two machines so that other people cannot eavesdrop on it.

OpenSSH is the most widely deployed implementation of the SSH protocol.

An SSH server listens on the network for incoming SSH requests, authenticates those requests, and provides a system command prompt (or another service that you configure). The most popular SSH server is OpenSSH’s sshd.

Use an SSH client to connect to your remote server or network device. The most popular SSH client for Windows systems is PuTTY. The standard SSH client for Unix-like systems is ssh(1), from OpenSSH.

The SSH protocol comes in two versions, SSH-1 (version 1) and SSH-2 (version 2). Always use SSH-2. All modern SSH software defaults to version 2. You will find old embedded devices that still rely on SSH version 1, but SSH-1 is barely more secure than unencrypted telnet.

SSH-2 is the modern standard. The protocol is designed so that
vulnerabilities can be quickly addressed as they are discovered. Our constantly-increasing computing power makes today’s strong encryption tomorrow’s security risk, so SSH-2 is designed so that its algorithms and protocols can be upgraded in place. Protocols such as SCP and SFTP are built atop SSH.

All system-wide OpenSSH configuration files reside in /etc/ssh by default.
Default settings for the ssh(1) client appear in ssh_config.
The files starting with ssh_host and ending in _key are the server’s private keys. The middle of each file name gives the encryption algorithm—for example, ssh_host_ecdsa_key contains the host key that uses the ECDSA algorithm. These files should only be readable by root.
Each private key has a corresponding file with the same name but an added .pub at the end. This is the public key for that file. The server will offer the content of these files to any client.
Finally, sshd_config contains the server configuration. While you can tweak sshd with command-line options, permanent configuration is handled in the configuration file.

SSH
normally runs on TCP port 22. Use netcat(1) to see if you can access the daemon.

    $ nc -v devio.us 22
    Connection to devio.us 22 port [tcp/ssh] succeeded!
    SSH-2.0-OpenSSH_7.0
    ^C

From the server, check and see if the sshd process is running.

    $ ps ax | grep sshd
    626 - Is 0:00.03 /usr/sbin/sshd
    31960 - Is 0:00.38 sshd: mwlucas [priv] (sshd)
    44387 - S 0:05.75 sshd: mwlucas@pts/0 (sshd)
