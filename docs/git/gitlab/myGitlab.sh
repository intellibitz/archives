# https://docs.gitlab.com/
#Command line instructions
#You can also upload existing files from your computer using the instructions below.

#Git global setup
git config --global user.name "Muthu Ramadoss"
git config --global user.email "muthu.ramadoss@gmail.com"

#Create a new repository
git clone https://gitlab.com/intellibitz/intellibitz.git
cd intellibitz
git switch --create main
touch README.md
git add README.md
git commit -m "add README"
git push --set-upstream origin main

#Push an existing folder
cd existing_folder
git init --initial-branch=main
git remote add origin https://gitlab.com/intellibitz/intellibitz.git
git add .
git commit -m "Initial commit"
git push --set-upstream origin main

#Push an existing Git repository
cd existing_repo
git remote rename origin old-origin
git remote add origin https://gitlab.com/intellibitz/intellibitz.git
git push --set-upstream origin --all
git push --set-upstream origin --tags

# https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html
#Clone repository using personal access token
#To clone a repository when SSH is disabled, clone it using a personal access token by running the following command:
git clone https://<username>:<personal_token>@gitlab.com/gitlab-org/gitlab.git
#This method saves your personal access token in your bash history. To avoid this, run the following command:
git clone https://<username>@gitlab.com/gitlab-org/gitlab.git
#When asked for your password for https://gitlab.com, enter your personal access token.
