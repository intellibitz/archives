#!/usr/bin/env sh
# https://anaconda.org/
# https://docs.anaconda.com/
# https://conda-forge.org/
# https://docs.anaconda.com/anaconda/user-guide/getting-started/
# https://docs.anaconda.com/free/anaconda/install/linux/

apt-get install libgl1-mesa-glx libegl1-mesa libxrandr2 libxrandr2 libxss1 libxcursor1 libxcomposite1 libasound2 libxi6 libxtst6

# https://repo.anaconda.com/archive/
# https://repo.anaconda.com/archive/Anaconda3-2024.02-1-Linux-x86_64.sh
# curl -O https://repo.anaconda.com/archive/Anaconda3-<INSTALLER_VERSION>-Linux-x86_64.sh
curl -O https://repo.anaconda.com/archive/Anaconda3-2024.02-1-Linux-x86_64.sh

bash ~/Downloads/Anaconda3-2024.02-1-Linux-x86_64.sh

# Do you wish to update your shell profile to automatically initialize conda?
# This will activate conda on startup and change the command prompt when activated.
# If you'd prefer that conda's base environment not be activated on startup,
#    run the following command when conda is activated:

conda config --set auto_activate_base false

# You can undo this by running 

conda init --reverse $SHELL

conda create -n myenv Python=3.11.7
conda activate myenv

conda upgrade --all

conda list

