https://github.com/settings/keys

https://docs.github.com/en/authentication/connecting-to-github-with-ssh/about-ssh

https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent

About SSH
--

Using the SSH protocol, you can connect and authenticate to remote servers and services. With SSH keys, you can connect to GitHub without supplying your username and personal access token at each visit. You can also use an SSH key to sign commits.

When you set up SSH, you will need to generate a new private SSH key and add it to the SSH agent. You must also add the public SSH key to your account on GitHub before you use the key to authenticate or sign commits.

Generating a new SSH key
--

You can generate a new SSH key on your local machine. After you generate the key, you can add the public key to your account on GitHub.com to enable authentication for Git operations over SSH.

Note: GitHub improved security by dropping older, insecure key types on March 15, 2022.

As of that date, DSA keys (ssh-dss) are no longer supported. You cannot add new DSA keys to your personal account on GitHub.com.

RSA keys (ssh-rsa) with a valid_after before November 2, 2021 may continue to use any signature algorithm. RSA keys generated after that date must use a SHA-2 signature algorithm. Some older clients may need to be upgraded in order to use SHA-2 signatures.

    Open Terminal.

    Paste the text below, replacing the email used in the example with your GitHub email address.

    ssh-keygen -t ed25519 -C "your_email@example.com"

    Note: If you are using a legacy system that doesn't support the Ed25519 algorithm, use:

     ssh-keygen -t rsa -b 4096 -C "your_email@example.com"

    This creates a new SSH key, using the provided email as a label.

    > Generating public/private ALGORITHM key pair.

    When you're prompted to "Enter a file in which to save the key", you can press Enter to accept the default file location. Please note that if you created SSH keys previously, ssh-keygen may ask you to rewrite another key, in which case we recommend creating a custom-named SSH key. To do so, type the default file location and replace id_ALGORITHM with your custom key name.

    > Enter a file in which to save the key (/home/YOU/.ssh/id_ALGORITHM):[Press enter]

    At the prompt, type a secure passphrase. For more information, see "Working with SSH key passphrases."

    > Enter passphrase (empty for no passphrase): [Type a passphrase]
    > Enter same passphrase again: [Type passphrase again]

Adding your SSH key to the ssh-agent
--

Before adding a new SSH key to the ssh-agent to manage your keys, you should have checked for existing SSH keys and generated a new SSH key.

    Start the ssh-agent in the background.

    $ eval "$(ssh-agent -s)"
    > Agent pid 59566

    Depending on your environment, you may need to use a different command. For example, you may need to use root access by running sudo -s -H before starting the ssh-agent, or you may need to use exec ssh-agent bash or exec ssh-agent zsh to run the ssh-agent.

    Add your SSH private key to the ssh-agent.

    If you created your key with a different name, or if you are adding an existing key that has a different name, replace id_ed25519 in the command with the name of your private key file.

    ssh-add ~/.ssh/id_ed25519

    Add the SSH public key to your account on GitHub. For more information, see "Adding a new SSH key to your GitHub account."

https://docs.github.com/en/authentication/connecting-to-github-with-ssh/adding-a-new-ssh-key-to-your-github-account

Testing your SSH connection
--

https://docs.github.com/en/authentication/connecting-to-github-with-ssh/testing-your-ssh-connection

After you've set up your SSH key and added it to your account on GitHub.com, you can test your connection.
Platform navigation

Before testing your SSH connection, you should have:

    Checked for existing SSH keys
    Generated a new SSH key
    Added a new SSH key to your GitHub account

When you test your connection, you'll need to authenticate this action using your password, which is the SSH key passphrase you created earlier. For more information on working with SSH key passphrases, see "Working with SSH key passphrases."

    Open Terminal.

    Enter the following:

    $ ssh -T git@github.com
    # Attempts to ssh to GitHub

    You may see a warning like this:

    > The authenticity of host 'github.com (IP ADDRESS)' can't be established.
    > ED25519 key fingerprint is SHA256:+DiY3wvvV6TuJJhbpZisF/zLDA0zPMSvHdkr4UvCOqU.
    > Are you sure you want to continue connecting (yes/no)?

    Verify that the fingerprint in the message you see matches GitHub's public key fingerprint. If it does, then type yes:

https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/githubs-ssh-key-fingerprints

These are GitHub's public key fingerprints:

    SHA256:uNiVztksCsDhcc0u9e8BujQXVUpKZIDTMczCvj3tD2s (RSA)
    SHA256:br9IjFspm1vxR3iA35FWE+4VTyz1hYVLIE2t1/CeyWQ (DSA - deprecated)
    SHA256:p2QAMXNIC1TJYWeIOttrVc98/R1BUFWu3/LiyKgUfQM (ECDSA)
    SHA256:+DiY3wvvV6TuJJhbpZisF/zLDA0zPMSvHdkr4UvCOqU (Ed25519)

You can add the following ssh key entries to your ~/.ssh/known_hosts file to avoid manually verifying GitHub hosts:

github.com ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIOMqqnkVzrm0SdG6UOoqKLsabgH5C9okWi0dh2l9GKJl
github.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBEmKSENjQEezOmxkZMy7opKgwFB9nkt5YRrYMjNuG5N87uRgg6CLrbo5wAdT/y6v0mKV0U2w0WZ2YB/++Tpockg=
github.com ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCj7ndNxQowgcQnjshcLrqPEiiphnt+VTTvDP6mHBL9j1aNUkY4Ue1gvwnGLVlOhGeYrnZaMgRK6+PKCUXaDbC7qtbW8gIkhL7aGCsOr/C56SJMy/BCZfxd1nWzAOxSDPgVsmerOBYfNqltV9/hWCqBywINIR+5dIg6JTJ72pcEpEjcYgXkE2YEFXV1JHnsKgbLWNlhScqb2UmyRkQyytRLtL+38TGxkxCflmO+5Z8CSSNY7GidjMIZ7Q4zMjA2n1nGrlTDkzwDCsw+wqFPGQA179cnfGWOWRVruj16z6XyvxvjJwbz0wQZ75XK5tKSb7FNyeIEs4TT4jk+S4dhPeAUC5y+bDYirYgM4GC7uEnztnZyaVWQ7B381AK4Qdrwt51ZqExKbQpTUNn+EjqoTwvqNj4kqx5QUCI0ThS/YkOxJCXmPUWZbhjpCg56i+2aB6CmK2JGhn57K5mj0MNdBXA4/WnwH6XoPWJzK5Nyu2zB3nAZp+S5hpQs+p1vN1/wsjk=

    > Hi USERNAME! You've successfully authenticated, but GitHub does not
    > provide shell access.

    You may see this error message:

    ...
    Agent admitted failure to sign using the key.
    debug1: No more authentication methods to try.
    Permission denied (publickey).

    This is a known problem with certain Linux distributions. For more information, see "Error: Agent admitted failure to sign."

    Note: The remote command should exit with code 1.

    Verify that the resulting message contains your username. If you receive a "permission denied" message, see "Error: Permission denied (publickey)."

http://www.unixwiz.net/techtips/ssh-agent-forwarding.html

Using SSH agent forwarding
--

https://docs.github.com/en/authentication/connecting-to-github-with-ssh/using-ssh-agent-forwarding

To simplify deploying to a server, you can set up SSH agent forwarding to securely use local SSH keys.

SSH agent forwarding can be used to make deploying to a server simple. It allows you to use your local SSH keys instead of leaving keys (without passphrases!) sitting on your server.

If you've already set up an SSH key to interact with GitHub, you're probably familiar with ssh-agent. It's a program that runs in the background and keeps your key loaded into memory, so that you don't need to enter your passphrase every time you need to use the key. The nifty thing is, you can choose to let servers access your local ssh-agent as if they were already running on the server. This is sort of like asking a friend to enter their password so that you can use their computer.

Setting up SSH agent forwarding
--

Ensure that your own SSH key is set up and working. You can use our guide on generating SSH keys if you've not done this yet.

You can test that your local key works by entering ssh -T git@github.com in the terminal:

$ ssh -T git@github.com

# Attempt to SSH in to github

> Hi USERNAME! You've successfully authenticated, but GitHub does not provide
> shell access.

We're off to a great start. Let's set up SSH to allow agent forwarding to your server.

    Using your favorite text editor, open up the file at ~/.ssh/config. If this file doesn't exist, you can create it by entering touch ~/.ssh/config in the terminal.

    Enter the following text into the file, replacing example.com with your server's domain name or IP:

     Host example.com
       ForwardAgent yes

Warning: You may be tempted to use a wildcard like Host * to just apply this setting to all SSH connections. That's not really a good idea, as you'd be sharing your local SSH keys with every server you SSH into. They won't have direct access to the keys, but they will be able to use them as you while the connection is established. You should only add servers you trust and that you intend to use with agent forwarding.

Testing SSH agent forwarding

To test that agent forwarding is working with your server, you can SSH into your server and run ssh -T git@github.com once more. If all is well, you'll get back the same prompt as you did locally.

If you're unsure if your local key is being used, you can also inspect the SSH_AUTH_SOCK variable on your server:

$ echo "$SSH_AUTH_SOCK"

# Print out the SSH_AUTH_SOCK variable

> /tmp/ssh-4hNGMk8AZX/agent.79453

If the variable is not set, it means that agent forwarding is not working:

$ echo "$SSH_AUTH_SOCK"

# Print out the SSH_AUTH_SOCK variable

> [No output]
> $ ssh -T git@github.com

# Try to SSH to github

> Permission denied (publickey).

About commit signature verification
--

https://docs.github.com/en/authentication/managing-commit-signature-verification/about-commit-signature-verification

You can sign commits and tags locally, to give other people confidence about the origin of a change you have made. If a commit or tag has a GPG, SSH, or S/MIME signature that is cryptographically verifiable, GitHub marks the commit or tag "Verified" or "Partially verified."

For most individual users, GPG or SSH will be the best choice for signing commits. S/MIME signatures are usually required in the context of a larger organization. SSH signatures are the simplest to generate. You can even upload your existing authentication key to GitHub to also use as a signing key. Generating a GPG signing key is more involved than generating an SSH key, but GPG has features that SSH does not. A GPG key can expire or be revoked when no longer used. GitHub shows commits that were signed with such a key as "Verified" unless the key was marked as compromised. SSH keys don't have this capability.

GPG commit signature verification
--

You can use GPG to sign commits with a GPG key that you generate yourself.

GitHub uses OpenPGP libraries to confirm that your locally signed commits and tags are cryptographically verifiable against a public key you have added to your account on GitHub.com.

Checking for existing GPG keys
--

https://docs.github.com/en/authentication/managing-commit-signature-verification/checking-for-existing-gpg-keys

Before you generate a GPG key, you can check to see if you have any existing GPG keys.

    gpg --list-secret-keys --keyid-format=long

Check the command output to see if you have a GPG key pair.

If there are no GPG key pairs or you don't want to use any that are available for signing commits and tags, then generate a new GPG key.

If there's an existing GPG key pair and you want to use it to sign commits and tags, you can display the public key using the following command, substituting in the GPG key ID you'd like to use. In this example, the GPG key ID is 3AA5C34371567BD2:

    $ gpg --armor --export 3AA5C34371567BD2
    # Prints the GPG key ID, in ASCII armor format

You can then add your GPG key to your GitHub account.

Generating a GPG key
--

Note: Before generating a new GPG key, make sure you've verified your email address. If you haven't verified your email address, you won't be able to sign commits and tags with GPG. For more information, see "Verifying your email address."

Generate a GPG key pair. Since there are multiple versions of GPG, you may need to consult the relevant man page to find the appropriate key generation command.

If you are on version 2.1.17 or greater, paste the text below to generate a GPG key pair.
Shell

    gpg --full-generate-key

If you are not on version 2.1.17 or greater, the gpg --full-generate-key command doesn't work. Paste the text below and skip to step 6.
Shell

    gpg --default-new-key-algo rsa4096 --gen-key

Telling Git about your signing key
--

To sign commits locally, you need to inform Git that there's a GPG, SSH, or X.509 key you'd like to use.

If you have multiple GPG keys, you need to tell Git which one to use.

    Open Terminal.

    If you have previously configured Git to use a different key format when signing with --gpg-sign, unset this configuration so the default format of openpgp will be used.

    git config --global --unset gpg.format

    Use the gpg --list-secret-keys --keyid-format=long command to list the long form of the GPG keys for which you have both a public and private key. A private key is required for signing commits or tags.
    Shell

gpg --list-secret-keys --keyid-format=long

Note: Some GPG installations on Linux may require you to use gpg2 --list-keys --keyid-format LONG to view a list of your existing keys instead. In this case you will also need to configure Git to use gpg2 by running git config --global gpg.program gpg2.

From the list of GPG keys, copy the long form of the GPG key ID you'd like to use. In this example, the GPG key ID is 8E1E773D7DFADC8C:
Shell

    $ gpg --list-secret-keys --keyid-format=long
    /home/zbook/.gnupg/pubring.kbx
    ------------------------------
    sec   rsa4096/8E1E773D7DFADC8C 2024-03-13 [SC]
        7E9935707672CE253E32CF408E1E773D7DFADC8C
    uid                 [ultimate] Muthu Ramadoss (IntelliBitz) <muthu.ramadoss@gmail.com>
    ssb   rsa4096/D93CED216D2BDA0B 2024-03-13 [E]

To set your primary GPG signing key in Git, paste the text below, substituting in the GPG primary key ID you'd like to use. In this example, the GPG key ID is 8E1E773D7DFADC8C:

git config --global user.signingkey 8E1E773D7DFADC8C

Alternatively, when setting a subkey include the ! suffix. In this example, the GPG subkey ID is D93CED216D2BDA0B:

git config --global user.signingkey D93CED216D2BDA0B!

Optionally, to configure Git to sign all commits by default, enter the following command:

git config --global commit.gpgsign true

For more information, see "Signing commits."

To add your GPG key to your .bashrc startup file, run the following command:

[ -f ~/.bashrc ] && echo -e '\nexport GPG_TTY=$(tty)' >> ~/.bashrc
