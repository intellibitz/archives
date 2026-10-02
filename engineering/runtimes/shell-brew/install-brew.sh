# https://brew.sh/
# https://www.how2shout.com/linux/how-to-install-brew-ubuntu-20-04-lts-linux/
#
# If you want to remove Homebrew, then here is the brew uninstallation script, also available on GitHub
# UnInstall
#
#/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/uninstall.sh)"
#
# Homebrew installs packages to their own directory and then symlinks their files into /usr/local.
# Homebrew won’t install files outside its prefix and you can place a Homebrew installation wherever you like.
# Install HomeBrew - Paste that in a Linux shell prompt.
#
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
#
# To run the brew command after installation, we need to add it in our system path…
#
echo 'eval "$(/home/linuxbrew/.linuxbrew/bin/brew shellenv)"' >> /home/muthuselvam/.profile
eval "$(/home/linuxbrew/.linuxbrew/bin/brew shellenv)"

man brew

brew -v

brew

brew doctor

#==> Next steps:
#- Add Homebrew to your PATH in /home/muthuselvam/.profile:
#    echo 'eval "$(/home/linuxbrew/.linuxbrew/bin/brew shellenv)"' >> /home/muthuselvam/.profile
#    eval "$(/home/linuxbrew/.linuxbrew/bin/brew shellenv)"
#- Run `brew help` to get started
#- Further documentation:
#    https://docs.brew.sh
#- Install the Homebrew dependencies if you have sudo access:
#    sudo apt-get install build-essential
#    See https://docs.brew.sh/linux for more information
#- We recommend that you install GCC:
#    brew install gcc