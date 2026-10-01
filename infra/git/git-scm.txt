# https://git-scm.com
# https://git-scm.com/docs/gitcredentials
# https://git-scm.com/doc/credential-helpers
# https://git-scm.com/book/en/v2/Git-Basics-Undoing-Things
# https://git-scm.com/book/en/v2/Getting-Started-First-Time-Git-Setup
# https://git-scm.com/book/en/v2/Git-Basics-Recording-Changes-to-the-Repository

Debian-Ubuntu
For- the latest stable version for your release of Debian-Ubuntu
apt-get install git
For- Ubuntu, this PPA provides the latest stable upstream Git version
add-apt-repository ppa:git-core/ppa
apt update; apt install git

git config credential.https://example.com.username myusername
git config credential.helper "[helper] [options]"
#If you want to check your configuration settings, you can use the git config --list command to
# list all the settings Git can find at that point:
$ git config --list
#You can view all of your settings and where they are coming from using:
$ git config --list --show-origin
git config --global --list
git config --global user.name=["name"]
git config --global user.email=["email"]
git config --global --add safe.directory dir-path
#If you want to use a different text editor, such as Emacs, you can do the following:
$ git config --global core.editor emacs
#To set main as the default branch name do:
$ git config --global init.defaultBranch main
#You can also check what Git thinks a specific key’s value is by typing git config <key>:
$ git config user.name

#--
#Start a new Git repository for an existing code base
$ cd /path/to/my/codebase
#Initial branch configuration: git init also sets up an initial branch /traditionally main/. However, this
# branch doesn't have any commits until you make your first commit.
$ git init                        #/1/ Create a /path/to/my/codebase/.git directory.
$ git add .                       #/2/ Add all existing files to the index.
$ git commit -m "commit_message"  #/3/ Record the pristine state as the first commit in the history.
git init -b ["main"] #creates or re-initializes local git repository as main branch

#You clone a repository with git clone <url>. For example, if you want to clone the
# Git linkable library called lib-git2, you can do so like this:
git clone URL #clones remote git repository
$ git clone https://github.com/libgit2/libgit2
#If you want to clone the repository into a directory named something other than lib-git2, you can
# specify the new directory name as an additional argument:
git clone URL directory_name #clones a remote repository
$ git clone https://github.com/libgit2/libgit2 mylibgit
#Git has a number of different transfer protocols you can use. The previous example uses the https:// protocol, but
# you may also see git:// or user@server:path/to/repo.git, which uses the SSH transfer protocol.

#The .git folder manages your project's repo. Git doesn't run as a server*. Instead the .git folder acts as your
# local 'server' that all the git commands communicate with. Basically, running a git command edits the contents of the .git folder.

git clone vs. git init
#--
#While git init is used to create a new repository, git clone is used to copy an existing repository. If you're
# starting a new project from scratch use git init. If you're contributing to an existing project or want to create a
# local copy of a project, you'll use git clone followed by the repository's URL.

#The git clone command automatically sets up the necessary config for your repo to connect back to a remote.
# But you can also manually configure a repo set up with git init to connect to a remote.

Best practices for Using git init
#--
#One project per repository: It's a best practice to keep each Git repository limited to a single project or
# logical grouping of files to maintain clarity and organization.
#Immediate .gitignore creation: After initializing your repository, create a .gitignore file to specify
# intentionally untracked files that Git should ignore. Common examples include compiled code, system files, and editor configuration.

git status #shows modified, unmodified files

git remote #List the remote repository connections stored in the local repository by shortname
git remote -v #List the remote repository connections in the local repository with shortnames and URLs
git remote add shortname URL #adds a connection to a remote repository named <shortname> at <URL>
#remove the old 'origin' remote and add new origin remote
git remote remove origin
git remote add origin new_repo_path.git

git add filename #adds files to staging
git add -A #adds all files from working directory to staging, tracking on
git restore filename #discards changes in working directory
git restore --staged filename #unstage, restores a file to another version of the file in the staging area

git commit -m ["init commit"] #commits all files in staging 

git log #shows commit history of current branch
git cat-file -p ["commit-hash"] #first 7 digits of hash is ok, view parent of commit 

git branch #shows all local branches
git branch branch_name #creates local branch
git branch -d branch_name #deletes a local branch

git checkout ["commit-hash"] #checks out a commit
git checkout branch_name #switches to branch
git checkout -b new-branch-name #creates new branch and switches on to it
git switch -c new-branch-name #creates new branch and switches on to it
git switch branch_name #switches to branch

git merge branch_name #integrates changes from branch to target[current] branch
git merge --abort #stops the merge process and go back to the state before the merge

git rebase branch_name #Reapply commits on top of another branch
git rebase --continue #continues with the rebase process after having resolved merge conflicts
git rebase --abort #stops the rebase process and go back to the state before the rebase

git reset --hard HEAD~1
git switch -

#--
git log --all #shows commit history for all branches in local repository
git log --oneline --decorate --graph --all
git branch --all #lists local branches and remote-tracking branches
git branch -vv #lists the local branches and their upstream branches, if they have any
git branch -u shortname/branch_name #defines an upstream branch for the current local branch

git push --set-upstream shortname branch_name
git push shortname branch_name #uploads content from <branch_name> to the <shortname> remote repository
git push shortname -d branch_name #deletes a remote branch and the associated remote-tracking branch

git fetch shortname #downloads data from the <shortname> remote repository
git fetch #downloads data from the remote repository with shortname origin
git fetch -p #removes remote-tracking branches that correspond to deleted remote branches and download data from the remote repository

#git fetch + [git merge OR git rebase] = git pull
#--
git pull #if an upstream branch is defined for the current branch, fetch and integrate changes from the defined upstream branch
git pull shortname branch_name #fetches and integrates changes from the <shortname> remote repository for the specified <branch_name>
git pull -p #removes remote-tracking branches that correspond to deleted remote branches and fetches and integrates changes from the <shortname> remote repository for the specified <branch_name>
git pull --allow-unrelated-histories origin main

#master is now named main
#The default branch has been renamed!
#If you have a local clone, you can update it by running the following commands.
git branch -m master main
git fetch origin
git branch -u origin/main main
git remote set-head origin -a

# https://git-scm.com/docs/git-init
#Start a new Git repository for an existing code base
cd /path/to/my/codebase || exit

## Git commands
git init      #/1/ Create a /path/to/my/codebase/.git directory.
git add .     #/2/ Add all existing files to the index.
git commit    #/3/ Record the pristine state as the first commit in the history.
git clone
git add
git commit -m
git status
git pull
git push
git branch -a

#The .git directory structure
HEAD:
#--
#The HEAD file is a reference to the current branch that's checked out. By default, it points to the master or
# main branch, but it won't actually refer to a valid branch until you make your first commit. It can also point to a
# commit if you're in a 'detached HEAD' state.
config:
#--
#This file contains repository-specific configuration settings. These settings can include user information, remote
# repository URLs, and branch configurations. The settings here override the global Git configuration settings for
# this specific repository.
objects/:
#--
#The objects directory stores all the data for your commits, including files and the structure of the commit tree. This
# data is stored in a compressed format, making Git very efficient. The objects are identified by a SHA-1 hash of their content.
refs/:
#--
#The refs directory contains references to commit objects in the repository, organized into subdirectories such as
# heads/ for branch heads and tags/ for tag objects. These references are updated as you commit and branch within your repository.
hooks/:
#--
#This directory contains client-side or server-side scripts that are invoked at different phases of the Git workflow, such as
# before a commit is finalized /pre-commit/ or before pushing to a remote repository /pre-push/. By default, Git populates this
# directory with example scripts. These scripts are not active until renamed /removing the .sample extension/.
info/:
#--
#Inside, you'll find the exclude file, which works like a .gitignore file but is specific to this repository. The patterns listed in
# the exclude file will be ignored by Git, similar to how .gitignore works, but without the need to commit this file into the repository.
description:
#--
#This file is only used by the GitWeb program, which is a Git web interface. By default, it contains a placeholder text
# /"Unnamed repository; edit this file 'description' to name the repository."/, which can be changed to provide a
# meaningful description of your repository for viewers on GitWeb.
index:
#--
#The index file /not present immediately after git init but created upon first adding files to the staging area/
# acts as the staging area /"index"/ for Git. It tracks which files will be included in the next commit.

cat .gitignore
#*.[oa]
#*/

https://git-scm.com/book/en/v2/Getting-Started-About-Version-Control
In a DVCS /such as Git, Mercurial or Darcs/, clients dont just check out the latest snapshot of the files, rather, they
 fully mirror the repository, including its full history. Thus, if any server dies, and these systems were
  collaborating via that server, any of the client repositories can be copied back up to the server to
   restore it. Every clone is really a full backup of all the data.

https://git-scm.com/book/en/v2/Getting-Started-What-is-Git%3F
The Three States - Pay attention now —  here is the main thing to remember about Git if you want the rest of your
 learning process to go smoothly. Git has three main states that your files can reside in:
modified: Modified means that you have changed the file but have not committed it to your database yet.
staged: Staged means that you have marked a modified file in its current version to go into your next commit snapshot. ,and
committed: Committed means that the data is safely stored in your local database.
This leads us to the three main sections of a Git project: the
 "working tree" /Working Directory/, the
"staging area" /Index/, and the
"Git directory" /Repository/.
The Git directory is where Git stores the metadata and object database for your project. This is the
 most important part of Git, and it is what is copied when you clone a repository from another computer.
The basic Git workflow goes something like this:
 You modify files in your working tree.
You selectively stage just those changes you want to be part of your next commit, which adds only those changes to the staging area.
You /do a commit, which takes the files as they are in the staging area and stores that snapshot permanently to
 your Git directory. If a particular version of a file is in the Git directory, its considered committed. If it has
been modified and was added to the staging area, it is staged. And if it was changed since it was checked out but has
not been staged, it is modified. In Git Basics, you will learn more about these states and how you can
either take advantage of them or skip the staged part entirely.

https://git-scm.com/book/en/v2/Getting-Started-First-Time-Git-Setup
Git comes with a tool called git config that lets you get and set configuration variables that control all aspects of
 how Git looks and operates. These variables can be stored in three different places:
/etc/gitconfig file: Contains values applied to every user on the system and all their repositories. If you
 pass the option --system to git config, it reads and writes from this file specifically. Because this is a
system configuration file, you would need administrative or superuser privilege to make changes to it.
//.gitconfig or //.config/git/config file: Values specific personally to you, the user. You can make Git read and write to
 this file specifically by passing the --global option, and this affects all of the repositories you work with on your system.
config file in the Git directory /that is, .git/config/ of whatever repository youre currently using: Specific to
 that single repository. You can force Git to read from and write to this file with the --local option, but that is in
fact the default. Unsurprisingly, you need to be located somewhere in a Git repository for this option to work properly.
Each level overrides values in the previous level, so values in .git/config trump those in [path]/etc/gitconfig.
On Windows systems, Git looks for the .gitconfig file in the "$HOME" directory /C:/Users\$USER for most people/. It also
 still looks for [path]/etc/gitconfig, although its relative to the MSys root, which is wherever you decide to
install Git on your Windows system when you run the installer. If you are using version 2.x or later of Git for Windows,
there is also a system-level config file at C:/Documents and Settings/All Users/Application Data/Git/config on Windows XP, and
in C:/ProgramData/Git/config on Windows Vista and newer. This config /file can only be changed by git config -f <file> as an admin.
You can view all of your settings and where they are coming from using:
$ git config --list --show-origin

$ git config --global user.name "John Doe"
$ git config --global user.email johndoe@example.com
$ git config --global core.editor emacs
$ git config --global init.defaultBranch main

https://git-scm.com/book/en/v2/Getting-Started-Getting-Help
$ git help /verb/
$ git <verb> --help
$ man git-/verb/
/For example, you can get the manpage help for the git config command by running this:

$ git help config

In addition, if you dont need the full-blown manpage help, but just need a quick refresher on the available options for a
Git command, you can ask for the more concise help output with the -h option, as in:
$ git add -h
/If the manpages and this book arent enough and you need in-person help, you can try the
 #git, #github, or #gitlab channels on the Libera Chat IRC server, which can be found at https://libera.chat/.

https://git-scm.com/book/en/v2/Git-Basics-Getting-a-Git-Repository
Getting a Git Repository
You typically obtain a Git repository in one of two ways:
You can take a local directory that is currently not under version control, and turn it into a Git repository, or
You can clone an existing Git repository from elsewhere.
In either case, you end up with a Git repository on your local machine, ready for work.

Initializing a Repository in an Existing Directory
/If you have a project directory that is currently not under version control and you want to start controlling it
 with Git, you first need to go to that projects directory. If youve never /done this, it looks a
little different depending on which system youre running:
/for Linux:
$ cd /home/user/my_project
and type:

$ git init

This creates a new subdirectory named .git that contains all of your necessary repository files — a Git repository skeleton.
 At this point, nothing in your project is tracked yet. See Git Internals for more information about exactly what
files are contained in the .git directory you just created.
/If you want to start version-controlling existing files /as opposed to an empty directory/, you should probably begin
 tracking those files and /do an initial commit. You can accomplish that with a few git add commands that specify the
files you want to track, followed by a git commit:
$ git add "*.c"
$ git add LICENSE
$ git commit -m 'Initial project version'
We will go over what these commands /do in just a minute. At this point, you have a Git repository with tracked files and an initial commit.

Cloning an Existing Repository
/If you want to get a copy of an existing Git repository — for example, a project youd like to
 contribute to — the command you need is git clone. If youre familiar with other VCSs such as Subversion, youll notice that the
command is "clone" and not "checkout". This is an important distinction — instead of getting just a working copy, Git receives a
full copy of nearly all data that the server has. Every version of every file for the history of the project is pulled down
by default when you run git clone. In fact, if your server disk gets corrupted, you can often use nearly any of the
clones on any client to set the server back to the state it was in when it was cloned /you may lose some server-side hooks and such,
but all the versioned data would be there — see Getting Git on a Server for more details/.
You clone a repository with git clone <url>. For example, if you want to clone the Git linkable library called libgit2, you can /do so like this:

$ git clone https://github.com/libgit2/libgit2

That creates a directory named libgit2, initializes a .git directory inside it, pulls down all the data for that repository,
 and checks out a working copy of the latest version. If you go into the new libgit2 directory that was
just created, youll see the project files in there, ready to be worked on or used.
/If you want to clone the repository into a directory named something other than libgit2, you can specify the
 new directory name as an additional argument:

$ git clone https://github.com/libgit2/libgit2 mylibgit

That command does the same thing as the previous one, but the target directory is called mylibgit.
Git has a number of different transfer protocols you can use. The previous example uses the https:// protocol, but you may also
 see git:// or user@server:path/to/repo.git, which uses the SSH transfer protocol. Getting Git on a Server will introduce all of the
available options the server can set up to access your Git repository and the pros and cons of each.

https://git-scm.com/book/en/v2/Git-Basics-Recording-Changes-to-the-Repository
Recording Changes to the Repository
At this point, you should have a bona fide Git repository on your local machine, and a checkout or working copy of all of its
 files in front of you. Typically, youll want to start making changes and committing snapshots of those changes into your
repository each time the project reaches a state you want to record.
Remember that each file in your working directory can be in one of two states:
 tracked or untracked.
Tracked files are files that were in the last snapshot, as well as any newly staged files;
 they can be unmodified, modified, or staged.
In short, tracked files are files that Git knows about.
Untracked files are everything else — any files in your working directory that were not in
 your last snapshot and are not in your staging area. When you first clone a repository, all of your files will be
tracked and unmodified because Git just checked them out and you havent edited anything.
As you edit files, Git sees them as modified, because youve changed them since your last commit. As you work, you
 selectively stage these modified files and /then commit all those staged changes, and the cycle repeats.

Checking the Status of Your Files
The main tool you use to determine which files are in which state is the git status command. If you run this command
 directly after a clone, you should see something like this:
$ git status

Tracking New Files
In order to begin tracking a new file, you use the command git add. To begin tracking the README file, you can run this:
$ git add README

To stage it, you run the git add command. git add is a multipurpose command — you use it to begin tracking new files,
 to stage files, and to /do other things like marking merge-conflicted files as resolved. It may be helpful to think of it
  more as /add precisely this content to the next commit/ rather than /add this file to the project/.

Short Status
/While the git status output is pretty comprehensive, its also quite wordy. Git also has a short status flag so you can
 see your changes in a more compact way. If you run git status -s or git status --short you get a far more simplified output from the command:
$ git status -s

Ignoring Files
https://github.com/github/gitignore
youll have a class of files that you dont want Git to automatically add or even show you as being untracked. These are
 generally automatically generated files such as log files or files produced by your build system. In such cases, you can
create a file listing patterns to match them named .gitignore. Here is an example .gitignore file:
$ cat .gitignore
#*.[oa]
#*/
Here is another example
.gitignore file:
# ignore all .a files
#*.a
# but do track lib.a, even though you're ignoring .a files above
#!lib.a
# only ignore the TODO file in the current directory, not subdir/TODO
#/TODO
# ignore all files in any directory named build
#build/
# ignore doc/notes.txt, but not doc/server/arch.txt
#doc/*.txt
# ignore all .pdf files in the doc/ directory and any of its subdirectories
#doc/**/*.pdf
In the simple case, a repository might have a single .gitignore file in its root directory, which applies recursively to the
 entire repository. However, it is also possible to have additional .gitignore files in subdirectories. The rules in these
nested .gitignore files apply only to the files under the directory where they are located. The Linux kernel source repository has 206 .gitignore files.

Viewing Your Staged and Unstaged Changes
/If the git status command is too vague for you — you want to know exactly what you changed, not just which files were
 changed — you can use the git diff command.
$ git diff
/If you want to see what youve staged that will go into your next commit, you can use git diff --staged. This command compares your
 staged changes to your last commit:
$ git diff --staged
Its important to note that git diff by itself doesnt show all changes made since your last commit — only changes that
 are still unstaged. If youve staged all of your changes, git diff will give you no output.
you can use
 /git diff/ to see what is still unstaged and
 /git diff --cached/ to see what youve staged so far /--staged and --cached are synonyms/

Committing Your Changes
Now that your staging area is set up the way you want it, you can commit your changes. Remember that anything that is
 still unstaged — any files you have created or modified that you havent run git add on since you edited them — wont go
into this commit. They will stay as modified files on your disk. In this case, lets say that the last time you ran
 git status, you saw that everything was staged, so youre ready to commit your changes. The simplest way to commit is to type git commit:
$ git commit
Alternatively- you can type your commit message inline with the commit command by specifying it after a -m flag, like this:
$ git commit -m "Story 182: fix benchmarks for speed"

Skipping the Staging Area
Although it can be amazingly useful for crafting commits exactly how you want them, the staging area is sometimes a
 bit more complex than you need in your workflow. If you want to skip the staging area, Git provides a simple shortcut.
Adding the -a option to the git commit command makes Git automatically stage every file that is already tracked before doing the
 commit- letting you skip the git add part:
$ git commit -a -m 'Add new benchmarks'

Removing Files
To remove a file from Git, you have to remove it from your tracked files /more accurately, remove it from your staging area/ and /then commit.
 The git rm command does that, and also removes the file from your working directory so you dont see it as an
untracked file the next time around. If you simply remove the file from your working directory, it shows up under the
 'Changes not staged for commit' /that is, unstaged/ area of your git status output:
$ rm PROJECTS.md
$ git rm PROJECTS.md
$ git rm log/\*.log
$ git rm \*/
Another useful thing you may want to /do is to keep the file in your working tree but remove it from your staging area.
 In other words, you may want to keep the file on your hard drive but not have Git track it anymore. This is
particularly useful if you forgot to add something to your .gitignore file and accidentally staged it, like a
large log file or a bunch of .a compiled files. To /do this, use the --cached option:
$ git rm --cached README

Moving Files
Unlike many other VCSs, Git doesnt explicitly track file movement. If you rename a file in Git, no metadata is stored in Git that
 tells it you renamed the file. However, Git is pretty smart about figuring that out after the fact — well deal with
detecting file movement a bit later.
Thus its a bit confusing that Git has a mv command. If you want to rename a file in Git, you can run something like:
$ git mv file_from file_to
and it works fine. In fact, if you run something like this and look at the status, youll see that Git considers it a renamed file:
$ git mv README.md README
$ git status
On branch master
Your branch is up-to-date with 'origin/master'.
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)
    renamed:    README.md -> README
However- this is equivalent to running something like this:
$ mv README.md README
$ git rm README.md
$ git add README
Git figures out that its a rename implicitly, so it doesnt matter if you rename a file that way or with the mv command.
 The only real difference is that git mv is one command instead of three — its a convenience function.
More importantly, you can use any tool you like to rename a file, and address the add/rm later, before you commit.

https://git-scm.com/book/en/v2/Git-Basics-Viewing-the-Commit-History
Viewing the Commit History
After you have created several commits, or if you have cloned a repository with an existing commit history, youll probably want to
 look back to see what has happened. The most basic and powerful tool to /do this is the git log command.
$ git log
One of the more helpful options is -p or --patch, which shows the difference /the patch output/ introduced in each commit.
 You can also limit the number of log entries displayed, such as using -2 to show only the last two entries.
$ git log -p -2
/if you want to see some abbreviated stats for each commit, you can use the --stat option:
$ git log --stat
$ git log --pretty=oneline
$ git log --pretty=format:"%h - %an, %ar : %s"
$ git log --pretty=format:"%h %s" --graph
$ git log --since=2.weeks
$ git log -S function_name
$ git log -- path/to/file
Preventing the display of merge commits
Depending on the workflow used in your repository, its possible that a sizable percentage of the commits in your
 log history are just merge commits, which typically arent very informative. To prevent the display of merge commits
cluttering up your log history, simply add the log option --no-merges.
/For example, if you want to see which commits modifying test files in the Git source code history were committed by
 Junio Hamano in the month of October 2008 and are not merge commits, you can run something like this:
$ git log --pretty="%h - %s" --author='Junio C Hamano' --since="2008-10-01" \
   --before="2008-11-01" --no-merges -- t/

https://git-scm.com/book/en/v2/Git-Basics-Undoing-Things
One of the common undos takes place when you commit too early and possibly forget to add some files, or you mess up your
 commit message. If you want to redo that commit, make the additional changes you forgot, stage them, and commit again using the --amend option:
$ git commit --amend
As an example, if you commit and /then realize you forgot to stage the changes in a file you wanted to add to this commit,
 you can /do something like this:
$ git commit -m 'Initial commit'
$ git add forgotten_file
$ git commit --amend
You end up with a single commit — the second commit replaces the results of the first.
Only amend commits that are still local and have not been pushed somewhere. Amending previously pushed commits and
 force pushing the branch will cause problems for your collaborators. For more on what happens when you /do this and
how to recover if youre on the receiving end read The Perils of Rebasing.
https://git-scm.com/book/en/v2/ch00/_rebase_peril
Its important to understand that when youre amending your last commit, youre not so much fixing it as replacing it
 entirely with a new, improved commit that pushes the old commit out of the way and puts the new commit in its place.
Effectively- its as if the previous commit never happened, and it wont show up in your repository history.
The obvious value to amending commits is to make minor improvements to your last commit, without cluttering your
 repository history with commit messages of the form, 'Oops, forgot to add a file' or 'Darn, fixing a typo in last commit'.

Unstaging a Staged File
$ git reset HEAD CONTRIBUTING.md
https://git-scm.com/book/en/v2/ch00/_git_reset
Its true that git reset can be a dangerous command, especially if you provide the --hard flag. However, in the
 scenario described above, the file in your working directory is not touched, so its relatively safe.

Unmodifying a Modified File
$ git checkout -- CONTRIBUTING.md
Its important to understand that git checkout -- <file> is a dangerous command. Any local changes you made to that
 file are gone — Git just replaced that file with the last staged or committed version. Dont ever use this command unless you
absolutely know that you dont want those unsaved local changes
/If you would like to keep the changes youve made to that file but still need to get it out of the way for now, well go
 over stashing and branching in Git Branching; these are generally better ways to go.
https://git-scm.com/book/en/v2/ch00/ch03-git-branching
Remember- anything that is committed in Git can almost always be recovered. Even commits that were on branches that
 were deleted or commits that were overwritten with an --amend commit can be recovered /see Data Recovery for data recovery/.
https://git-scm.com/book/en/v2/ch00/_data_recovery
However- anything you lose that was never committed is likely never to be seen again.

Undoing things with git restore
Git version 2.23.0 introduced a new command: git restore. Its basically an alternative to git reset which we just covered.
 From Git version 2.23.0 onwards, Git will use git restore instead of git reset for many undo operations.
Lets retrace our steps, and undo things with git restore instead of git reset.
Unstaging a Staged File with git restore
$ git restore --staged CONTRIBUTING.md
Unmodifying a Modified File with git restore
$ git restore CONTRIBUTING.md
Its important to understand that git restore /file/ is a dangerous command. Any local changes you made to that file are
 gone — Git just replaced that file with the last staged or committed version. Dont ever use this command unless you
absolutely know that you dont want those unsaved local changes.

https://git-scm.com/book/en/v2/Git-Basics-Working-with-Remotes
Working with Remotes
To be able to collaborate on any Git project, you need to know how to manage your remote repositories. Remote repositories are
 versions of your project that are hosted on the Internet or network somewhere. You can have several of them, each of which
generally is either read-only or read/write for you. Collaborating with others involves managing these remote repositories and
pushing and pulling data to and from them when you need to share work. Managing remote repositories includes knowing how to
add remote repositories, remove remotes that are no longer valid, manage various remote branches and define them as
being tracked or not, and more.
Remote repositories can be on your local machine.
It is entirely possible that you can be working with a 'remote' repository that is, in fact, on the same host you are.
 The word 'remote' does not necessarily imply that the repository is somewhere else on the network or Internet, only that
it is elsewhere. Working with such a remote repository would still involve all the standard pushing, pulling and
fetching operations as with any other remote.

Showing Your Remotes
To see which remote servers you have configured, you can run the git remote command. It lists the shortnames of each
 remote handle youve specified. If youve cloned your repository, you should at least see origin — that is the
default name Git gives to the server you cloned from:
$ git remote
$ git remote -v

Adding Remote Repositories
Weve mentioned and given some demonstrations of how the git clone command implicitly adds the origin remote for you.
 Heres how to add a new remote explicitly. To add a new remote Git repository as a shortname you can reference easily, run
git remote add /shortname/ /url/
$ git remote
origin
$ git remote add pb https://github.com/paulboone/ticgit
$ git remote -v
Now you can use the string pb on the command line instead of the whole URL.
$ git fetch pb

Fetching and Pulling from Your Remotes
As you just saw, to get data from your remote projects, you can run:
$ git fetch /remote/
The command goes out to that remote project and pulls down all the data from that remote project that you dont have yet.
 After you /do this, you should have references to all the branches from that remote, which you can merge in or inspect at any time.
/If you clone a repository, the command automatically adds that remote repository under the name 'origin'. So,
 git fetch origin fetches any new work that has been pushed to that server since you cloned /or last fetched from/ it.
Its important to note that the git fetch command only downloads the data to your local repository — it doesnt automatically
 merge it with any of your work or modify what youre currently working on. You have to merge it manually into your work when youre ready.
/If your current branch is set up to track a remote branch /see the next section and Git Branching for more information/,
 you can use the git pull command to automatically fetch and /then merge that remote branch into your current branch.
This may be an easier or more comfortable workflow for you; and by default, the git clone command automatically sets up
 your local master branch to track the remote master branch /or whatever the default branch is called/ on the
server you cloned from. Running git pull generally fetches data from the server you originally cloned from and
automatically tries to merge it into the code youre currently working on.
Note
From Git version 2.27 onward, git pull will give a warning if the pull.rebase variable is not set.
Git will keep warning you until you set the variable.
/If you want the default behavior of Git /fast-forward if possible, else create a merge commit/:
git config --global pull.rebase "false"
/If you want to rebase when pulling:
git config --global pull.rebase "true"

Pushing to Your Remotes
When you have your project at a point that you want to share, you have to push it upstream. The command for this is simple:
 git push /remote/ /branch/. If you want to push your master branch to your origin server /again, cloning generally sets up
both of those names for you automatically/, /then you can run this to push any commits youve /done back up to the server:
$ git push origin master
This command works only if you cloned from a server to which you have write access and if nobody has pushed in the meantime.
/If you and someone else clone at the same time and they push upstream and /then you push upstream, your push will
rightly be rejected. Youll have to fetch their work first and incorporate it into yours before youll be allowed to push.
 See Git Branching for more detailed information on how to push to remote servers.

Inspecting a Remote
/If you want to see more information about a particular remote, you can use the git remote show /remote/ command.
/If you run this command with a particular shortname, such as origin, you get something like this:
$ git remote show origin

Renaming and Removing Remotes
You can run git remote rename to change a remotes shortname. For instance, if you want to rename pb to paul, you can /do so with git remote rename:
$ git remote rename pb paul
$ git remote

/If you want to remove a remote for some reason — youve moved the server or are no longer using a particular mirror, or
 perhaps a contributor isnt contributing anymore — you can either use git remote remove or git remote rm:
$ git remote remove paul
$ git remote
origin
Once you delete the reference to a remote this way, all remote-tracking branches and configuration settings associated with that remote are also deleted.

https://git-scm.com/book/en/v2/Git-Basics-Tagging
Listing Your Tags - Listing the existing tags in Git is straightforward. Just type git tag /with optional -l or --list/:
$ git tag
Listing tag wildcards requires -l or --list option
/If you want just the entire list of tags, running the command git tag implicitly assumes you want a listing and
 provides one; the use of -l or --list in this case is optional.
/If- however, youre supplying a wildcard pattern to match tag names, the use of -l or --list is mandatory.

Creating Tags
Git supports two types of tags: lightweight and annotated.
A lightweight tag is very much like a branch that doesnt change — its just a pointer to a specific commit.
Annotated tags, however, are stored as full objects in the Git database. Theyre checksummed; contain the
 tagger name, email, and date; have a tagging message; and can be signed and verified with GNU Privacy Guard /GPG/.
Its generally recommended that you create annotated tags so you can have all this information; but if you want a
 temporary tag or for some reason dont want to keep the other information, lightweight tags are available too.

Annotated Tags
Creating an annotated tag in Git is simple. The easiest way is to specify -a when you run the tag command:
$ git tag -a v1.4 -m "my version 1.4"
You can see the tag data along with the commit that was tagged by using the git show command:
$ git show v1.4

Lightweight Tags
Another way to tag commits is with a lightweight tag. This is basically the commit checksum stored in a file — no other
 information is kept. To create a lightweight tag, dont supply any of the -a, -s, or -m options, just provide a tag name:
$ git tag v1.4-lw
This time, if you run git show on the tag, you dont see the extra tag information. The command just shows the commit:
$ git show v1.4-lw

Tagging Later
You can also tag commits after youve moved past them.
$ git tag -a v1.2 9fceb02

Sharing Tags
By default, the git push command doesnt transfer tags to remote servers. You will have to explicitly push tags to a
 shared server after you have created them. This process is just like sharing remote branches — you can run git push origin <tagname>.
$ git push origin v1.5
/If you have a lot of tags that you want to push up at once, you can also use the --tags option to the git push command.
 This will transfer all of your tags to the remote server that are not already there.
$ git push origin --tags
git push pushes both types of tags
git push /remote/ --tags will push both lightweight and annotated tags. There is currently no option to push only
 lightweight tags, but if you use git push <remote> --follow-tags only annotated tags will be pushed to the remote.

Deleting Tags
To delete a tag on your local repository, you can use git tag -d <tagname>. For example, we could remove our lightweight tag above as follows:
$ git tag -d v1.4-lw
Note that this does not remove the tag from any remote servers. There are two common variations for deleting a tag from a remote server.
The first variation is git push /remote/ :refs/tags/<tagname>:
$ git push origin :refs/tags/v1.4-lw
The way to interpret the above is to read it as the null value before the colon is being pushed to the
 remote tag name, effectively deleting it.
The second /and more intuitive/ way to delete a remote tag is with:
$ git push origin --delete /tagname/

Checking out Tags
/If you want to view the versions of files a tag is pointing to, you can /do a git checkout of that tag, although this
 puts your repository in 'detached HEAD' state, which has some ill side effects:
$ git checkout v2.0.0
In 'detached HEAD' state, if you make changes and /then create a commit, the tag will stay the same, but your
 new commit wont belong to any branch and will be unreachable, except by the exact commit hash. Thus, if you need to
make changes — say youre fixing a bug on an older version, for instance — you will generally want to create a branch:
$ git checkout -b version2 v2.0.0
Switched to a new branch 'version2'
/If you /do this and make a commit, your version2 branch will be slightly different than your v2.0.0 tag since it will
 move forward with your new changes, so /do be careful.

https://git-scm.com/book/en/v2/Git-Basics-Git-Aliases
Git doesnt automatically infer your command if you type it in partially. If you dont want to type the entire text of each of the
 Git commands, you can easily set up an alias for each command using git config. Here are a couple of examples you may want to set up:
$ git config --global alias.co checkout
$ git config --global alias.br branch
$ git config --global alias.ci commit
$ git config --global alias.st status
This means that, for example, instead of typing git commit, you just need to type git ci. As you go on using Git, youll probably
 use other commands frequently as well; dont hesitate to create new aliases.
This technique can also be very useful in creating commands that you think should exist. For example, to correct the
 usability problem you encountered with unstaging a file, you can add your own unstage alias to Git:
$ git config --global alias.unstage 'reset HEAD --'
This makes the following two commands equivalent:
$ git unstage fileA
$ git reset HEAD -- fileA
This seems a bit clearer. Its also common to add a last command, like this:
$ git config --global alias.last 'log -1 HEAD'
This way, you can see the last commit easily:
$ git last
As you can tell, Git simply replaces the new command with whatever you alias it for. However, maybe you want to run an
 external command, rather than a Git subcommand. In that case, you start the command with a ! character. This is useful if you
write your own tools that work with a Git repository. We can demonstrate by aliasing git visual to run gitk:
$ git config --global alias.visual '!gitk'

https://git-scm.com/book/en/v2/Git-Branching-Branches-in-a-Nutshell
Git doesnt store data as a series of changesets or differences, but instead as a series of snapshots.
When you make a commit, Git stores a commit object that contains a pointer to the snapshot of the content you staged.
 This object also contains the authors name and email address, the message that you typed, and pointers to the commit or
commits that directly came before this commit /its parent or parents/: zero parents for the initial commit, one parent for a
 normal commit, and multiple parents for a commit that results from a merge of two or more branches.
To visualize this, lets assume that you have a directory containing three files, and you stage them all and commit.
 Staging the files computes a checksum for each one, stores that version of the file in the
Git repository /Git refers to them as blobs/, and adds that checksum to the staging area:
$ git add README test.rb LICENSE
$ git commit -m 'Initial commit'
When you create the commit by running git commit, Git checksums each subdirectory /in this case, just the root project directory/ and
 stores them as a tree object in the Git repository. Git /then creates a commit object that has the metadata and a
pointer to the root project tree so it can re-create that snapshot when needed.
Your Git repository now contains five objects: three blobs /each representing the contents of one of the three files/,
 one tree that lists the contents of the directory and specifies which file names are stored as which blobs, and
one commit with the pointer to that root tree and all the commit metadata.
/If you make some changes and commit again, the next commit stores a pointer to the commit that came immediately before it.
A branch in Git is simply a lightweight movable pointer to one of these commits. The default branch name in Git is master.
 As you start making commits, youre given a master branch that points to the last commit you made. Every time you commit, the
master branch pointer moves forward automatically.
Note - The 'master' branch in Git is not a special branch. It is exactly like any other branch. The
 only reason nearly every repository has one is that the git init command creates it by default and most people dont bother to change it.

Creating a New Branch
What happens when you create a new branch? Well, doing so creates a new pointer for you to move around. Lets say you want to
 create a new branch called testing. You /do this with the 'git branch' command:
$ git branch testing
This creates a new pointer to the same commit youre currently on.
How does Git know what branch youre currently on? It keeps a special pointer called 'HEAD'. Note that this is a
 lot different than the concept of HEAD in other VCSs you may be used to, such as Subversion or CVS. In Git, this is a
pointer to the local branch youre currently on. In this case, youre still on master. The git branch command only created a
 new branch — it didnt switch to that branch.

Switching Branches
To switch to an existing branch, you run the 'git checkout' command. Lets switch to the new testing branch:
$ git checkout testing
This moves HEAD to point to the testing branch.
Note- git log doesnt show all the branches all the time
/If you were to run git log right now, you might wonder where the "testing" branch you just created went, as it would not appear in the output.
The branch hasnt disappeared; Git just doesnt know that youre interested in that branch and it is trying to
 show you what it thinks youre interested in. In other words, by default, git log will only show commit history below the branch youve checked out.
To show commit history for the desired branch you have to explicitly specify it:
 git log testing.
To show all of the branches, add --all to your git log command.
$ git checkout master /HEAD moves when you checkout/
That command did two things. It moved the HEAD pointer back to point to the master branch, and it reverted the files in
 your working directory back to the snapshot that master points to. This also means the changes you make from this
point forward will diverge from an older version of the project. It essentially rewinds the work youve /done in your
 testing branch so you can go in a different direction.
Note- Switching branches changes files in your working directory
Its important to note that when you switch branches in Git, files in your working directory will change. If you switch to
 an older branch, your working directory will be reverted to look like it did the last time you committed on that branch.
/If Git cannot /do it cleanly, it will not let you switch at all.

Because a branch in Git is actually a simple file that contains the 40 character SHA-1 checksum of the commit it points to,
 branches are cheap to create and destroy. Creating a new branch is as quick and simple as writing 41 bytes to a file /40 characters and a newline/.
This is in sharp contrast to the way most older VCS tools branch, which involves copying all of the projects files into a
 second directory. This can take several seconds or even minutes, depending on the size of the project, whereas in Git the
process is always instantaneous. Also, because were recording the parents when we commit, finding a proper merge base for
 merging is automatically /done for us and is generally very easy to do. These features help encourage developers to create and use branches often.
Lets see why you should /do so.
Note- Creating a new branch and switching to it at the same time
Its typical to create a new branch and want to switch to that new branch at the same time — this can be /done in one operation with
 git checkout -b <newbranchname>.
Note- From Git version 2.23 onwards you can use git switch instead of git checkout to: Switch to an existing branch:
 git switch testing-branch.
Create a new branch and switch to it:
 git switch -c new-branch.
The -c flag stands for create, you can also use the full flag: --create. Return to your previously checked out branch:
 git switch -.

https://git-scm.com/book/en/v2/Git-Branching-Basic-Branching-and-Merging
$ git checkout -b iss53
Switched to a new branch "iss53"
This is shorthand for:
$ git branch iss53
$ git checkout iss53
You work on your website and /do some commits. Doing so moves the iss53 branch forward, because you have it checked out /that is, your HEAD is pointing to it/:
$ vim index.html
$ git commit -a -m 'Create new footer [issue 53]'
$ git checkout master
Switched to branch 'master'
$ git checkout -b hotfix
Switched to a new branch 'hotfix'
$ vim index.html
$ git commit -a -m 'Fix broken email address'
You can run your tests, make sure the hotfix is what you want, and finally merge the hotfix branch back into your
master branch to deploy to production. You /do this with the git merge command:
$ git checkout master
$ git merge hotfix
Updating f42c576..3a0874c
Fast-forward
Youll notice the phrase 'fast-forward' in that merge. Because the commit C4 pointed to by the branch hotfix you
 merged in was directly ahead of the commit C2 youre on, Git simply moves the pointer forward. To phrase that
another way, when you try to merge one commit with a commit that can be reached by following the first commits history,
 Git simplifies things by moving the pointer forward because there is no divergent work
to merge together — this is called a 'fast-forward.'
After your super-important fix is deployed, youre ready to switch back to the work you were doing before you
 were interrupted. However, first youll delete the hotfix branch, because you no longer need it — the
master branch points at the same place. You can delete it with the -d option to git branch:
$ git branch -d hotfix
Now you can switch back to your work-in-progress branch on issue #53 and continue working on it.
$ git checkout iss53
Switched to branch "iss53"
$ vim index.html
$ git commit -a -m 'Finish the new footer [issue 53]'
$ git checkout master
Switched to branch 'master'
$ git merge iss53
Merge made by the 'recursive' strategy.
This looks a bit different than the hotfix merge you did earlier. In this case, your development history has diverged from
 some older point. Because the commit on the branch youre on isnt a direct ancestor of the branch youre merging in,
Git has to /do some work. In this case, Git does a simple three-way merge, using the two snapshots pointed to by the
 branch tips and the common ancestor of the two.
$ git branch -d iss53

Basic Merge Conflicts
Occasionally- this process doesnt go smoothly. If you changed the same part of the same file differently in the
 two branches youre merging, Git wont be able to merge them cleanly.
$ git merge iss53
Auto-merging index.html
#CONFLICT (content): Merge conflict in index.html
Automatic merge failed; fix conflicts and /then commit the result.
Git hasnt automatically created a new merge commit. It has paused the process while you resolve the conflict. If you want to
 see which files are unmerged at any point after a merge conflict, you can run git status:
$ git status
Anything that has merge conflicts and hasnt been resolved is listed as unmerged. Git adds standard conflict-resolution markers to the
 files that have conflicts, so you can open them manually and resolve those conflicts. Your file contains a section that looks something like this:
#<<<<<<< HEAD:index.html
#<div id="footer">contact : email.support@github.com</div>
#=======
#<div id="footer">
# please contact us at support@github.com
#</div>
#>>>>>>> iss53:index.html
This means the version in HEAD /your master branch, because that was what you had checked out when you ran your merge command/ is the
 top part of that block /everything above the =======/, while the version in your iss53 branch looks like everything in the
bottom part. In order to resolve the conflict, you have to either choose one side or the other or merge the contents yourself.
This resolution has a little of each section, and the
# <<<<<<<, =======, and >>>>>>>
lines have been completely removed. After youve resolved each of these sections in each conflicted file, run git add on
 each file to mark it as resolved. Staging the file marks it as resolved in Git.
/If you want to use a graphical tool to resolve these issues, you can run git mergetool, which fires up an
 appropriate visual merge tool and walks you through the conflicts:
$ git mergetool
Note- If you need more advanced tools for resolving tricky merge conflicts, we cover more on merging in Advanced Merging.
https://git-scm.com/book/en/v2/ch00/_advanced_merging
After you exit the merge tool, Git asks you if the merge was successful. If you tell the script that it was, it stages the
 file to mark it as resolved for you. You can run git status again to verify that all conflicts have been resolved:
$ git status
/If youre happy with that, and you verify that everything that had conflicts has been staged, you can type git commit to finalize the merge commit.

https://git-scm.com/book/en/v2/Git-Branching-Branch-Management
The git branch command does more than just create and delete branches. If you run it with no arguments, you get a simple listing of your current branches:
$ git branch
Notice the \* character that prefixes the master branch: it indicates the branch that you currently have checked out /i.e., the
 branch that HEAD points to/. This means that if you commit at this point, the master branch will be
moved forward with your new work. To see the last commit on each branch, you can run git branch -v:
$ git branch -v
The useful --merged and --no-merged options can filter this list to branches that you have or have not yet merged into the
 branch youre currently on. To see which branches are already merged into the branch youre on, you can run git branch --merged:
$ git branch --merged
Branches on this list without the \* in front of them are generally fine to delete with git branch -d; youve already
 incorporated their work into another branch, so youre not going to lose anything.
To see all the branches that contain work you havent yet merged in, you can run git branch --no-merged:
$ git branch --no-merged
Tip- The options described above, --merged and --no-merged will, if not given a commit or branch name as an argument,
 show you what is, respectively, merged or not merged into your current branch.
You can always provide an additional argument to ask about the merge state with respect to some other branch without checking that
 other branch out first, as in, what is not merged into the master branch?
$ git checkout testing
$ git branch --no-merged master

Changing a branch name
Caution- Do not rename branches that are still in use by other collaborators. Do not rename a branch like
 master/main/mainline without having read the section Changing the master branch name.
Suppose you have a branch that is called bad-branch-name and you want to change it to corrected-branch-name, while keeping
 all history. You also want to change the branch name on the remote /GitHub, GitLab, other server/. How /do you /do this?
Rename the branch locally with the git branch --move command:
$ git branch --move bad-branch-name corrected-branch-name
This replaces your bad-branch-name with corrected-branch-name, but this change is only local for now.
 To let others see the corrected branch on the remote, push it:
$ git push --set-upstream origin corrected-branch-name
Now well take a brief look at where we are now:
$ git branch --all
\* corrected-branch-name
  main
  remotes/origin/bad-branch-name
  remotes/origin/corrected-branch-name
  remotes/origin/main
Notice that youre on the branch corrected-branch-name and its available on the remote. However, the branch with the
 bad name is also still present there but you can delete it by executing the following command:
$ git push origin --delete bad-branch-name
Now the bad branch name is fully replaced with the corrected branch name.

Changing the master branch name
Warning- Changing the name of a branch like master/main/mainline/default will break the integrations, services, helper utilities and
 build/release scripts that your repository uses. Before you /do this, make sure you consult with your collaborators.
Also- make sure you /do a thorough search through your repo and update any references to the old branch name in your code and scripts.
Rename your local master branch into main with the following command:
$ git branch --move master main
Theres no local master branch anymore, because its renamed to the main branch.
To let others see the new main branch, you need to push it to the remote. This makes the renamed branch available on the remote.
$ git push --set-upstream origin main
Now we end up with the following state:
$ git branch --all
\* main
  remotes/origin/HEAD -> origin/master
  remotes/origin/main
  remotes/origin/master
Your local master branch is gone, as its replaced with the main branch. The main branch is present on the remote.
 However- the old master branch is still present on the remote. Other collaborators will continue to use the master branch as the
base of their work, until you make some further changes.
Now you have a few more tasks in front of you to complete the transition:
Any projects that depend on this one will need to update their code and/or configuration.
Update any test-runner configuration files.
Adjust build and release scripts.
Redirect settings on your repo host for things like the repos default branch, merge rules, and other things that match branch names.
Update references to the old branch in documentation.
Close or merge any pull requests that target the old branch.
After youve /done all these tasks, and are certain the main branch performs just as the master branch, you can delete the master branch:
$ git push origin --delete master

https://git-scm.com/book/en/v2/Git-Branching-Branching-Workflows
Long-Running Branches
Because Git uses a simple three-way merge, merging from one branch into another multiple times over a long period is
 generally easy to do. This means you can have several branches that are always open and that you use for different stages of
your development cycle; you can merge regularly from some of them into others.
Again- having multiple long-running branches isnt necessary, but its often helpful, especially when youre dealing with very large or complex projects.

Topic Branches
Topic branches, however, are useful in projects of any size. A topic branch is a short-lived branch that you create and
 use for a single particular feature or related work. This is something youve likely never /done with a VCS before because its
generally too expensive to create and merge branches. But in Git its common to create, work on, merge, and delete branches several times a day.

We will go into more detail about the various possible workflows for your Git project in Distributed Git, so before you
 decide which branching scheme your next project will use, be sure to read that chapter.
https://git-scm.com/book/en/v2/ch00/ch05-distributed-git
Its important to remember when youre doing all this that these branches are completely local. When youre branching and
 merging- everything is being /done only in your Git repository — there is no communication with the server.

https://git-scm.com/book/en/v2/Git-Branching-Remote-Branches
Remote Branches
Remote references are references /pointers/ in your remote repositories, including branches, tags, and so on. You can get a
 full list of remote references explicitly with
git ls-remote /remote/, or
git remote show /remote/ for remote branches as well as more information. Nevertheless, a more common way is to take advantage of remote-tracking branches.
Remote-tracking branches are references to the state of remote branches. Theyre local references that you cant move;
 Git moves them for you whenever you /do any network communication, to make sure they accurately represent the state of the
remote repository. Think of them as bookmarks, to remind you where the branches in your remote repositories were the last time you connected to them.
Lets say you have a Git server on your network at git.ourcompany.com. If you clone from this, Git clone command automatically names it
 'origin' for you, pulls down all its data, creates a pointer to where its master branch is, and names it origin/master locally.
Git also gives you your own local master branch starting at the same place as origins master branch, so you have something to work from.
Note- 'origin' is not special. Just like the branch name 'master' does not have any special meaning in Git, neither does 'origin'. While 'master' is the
 default name for a starting branch when you run git init which is the only reason its widely used, 'origin' is the default name for a
remote when you run git clone. If you run git clone -o booyah instead, /then you will have booyah/master as your default remote branch.
To synchronize your work with a given remote, you run a
 git fetch /remote/ command /in our case,
 git fetch origin/. This command looks up which server 'origin' is /in this case, its git.ourcompany.com/, fetches any
data from it that you dont yet have, and updates your local database, moving your origin/master pointer to its new, more up-to-date position.

To demonstrate having multiple remote servers and what remote branches for those remote projects look like, lets assume you have
 another internal Git server that is used only for development by one of your sprint teams. This server is at git.team1.ourcompany.com.
You can add it as a new remote reference to the project youre currently working on by running the git remote add command as we
 covered in Git Basics. Name this remote teamone, which will be your shortname for that whole URL.
https://git-scm.com/book/en/v2/ch00/ch02-git-basics-chapter
Now- you can run git fetch teamone to fetch everything the remote teamone server has that you dont have yet. Because that
 server has a subset of the data your origin server has right now, Git fetches no data but sets a remote-tracking branch called
teamone/master to point to the commit that teamone has as its master branch.

Pushing
When you want to share a branch with the world, you need to push it up to a remote to which you have write access. Your local
 branches arent automatically synchronized to the remotes you write to — you have to explicitly push the branches you want to share.
That way, you can use private branches for work you dont want to share, and push up only the topic branches you want to collaborate on.
/If you have a branch named serverfix that you want to work on with others, you can push it up the same way you pushed your first branch. Run
 git push /remote/ /branch/:
$ git push origin serverfix
This is a bit of a shortcut. Git automatically expands the serverfix branchname out to refs/heads/serverfix:refs/heads/serverfix, which means,
 /Take my serverfix local branch and push it to update the remotes serverfix branch./ Well go over the refs/heads/ part in detail in Git Internals,
but you can generally leave it off. You can also /do git push origin serverfix:serverfix, which does the same thing — it says,
 /Take my serverfix and make it the remotes serverfix./ You can use this format to push a local branch into a remote branch that is
named differently. If you didnt want it to be called serverfix on the remote, you could instead run
 git push origin serverfix:awesomebranch to push your local serverfix branch to the awesomebranch branch on the remote project.
Note- Dont type your password every time
/If youre using an HTTPS URL to push over, the Git server will ask you for your username and password for authentication.
 By default it will prompt you on the terminal for this information so the server can tell if youre allowed to push.
/If you dont want to type it every single time you push, you can set up a 'credential cache'. The simplest is just to
 keep it in memory for a few minutes, which you can easily set up by running
git config --global credential.helper cache.
/For more information on the various credential caching options available, see Credential Storage.
https://git-scm.com/book/en/v2/ch00/_credential_caching
The next time one of your collaborators fetches from the server, they will get a reference to where the servers version of
 serverfix is under the remote branch origin/serverfix:
$ git fetch origin
From https://github.com/schacon/simplegit
# * [new branch]      serverfix    -> origin/serverfix
Its important to note that when you /do a fetch that brings down new remote-tracking branches, you dont automatically have local,
 editable copies of them. In other words, in this case, you dont have a new serverfix branch — you have only an
origin/serverfix pointer that you cant modify.
To merge this work into your current working branch, you can run
 git merge origin/serverfix. If you want your own serverfix branch that you can work on, you can base it off your remote-tracking branch:
$ git checkout -b serverfix origin/serverfix
 Branch serverfix set up to track remote branch serverfix from origin.
 Switched to a new branch 'serverfix'
This gives you a local branch that you can work on that starts where origin/serverfix is.

Tracking Branches
Checking out a local branch from a remote-tracking branch automatically creates what is called a 'tracking branch'
 /and the branch it tracks is called an 'upstream branch'/. Tracking branches are local branches that have a
direct relationship to a remote branch. If youre on a tracking branch and type
 git pull, Git automatically knows which server to fetch from and which branch to merge in.
When you clone a repository, it generally automatically creates a master branch that tracks origin/master.
 However- you can set up other tracking branches if you wish — ones that track branches on other remotes, or
dont track the master branch. The simple case is the example you just saw, running
 git checkout -b /branch/ /remote///branch/. This is a common enough operation that Git provides the --track shorthand:
$ git checkout --track origin/serverfix
In fact, this is so common that theres even a shortcut for that shortcut. If the branch name youre trying to checkout
 a- doesnt exist and b- exactly matches a name on only one remote, Git will create a tracking branch for you:
$ git checkout serverfix
To set up a local branch with a different name than the remote branch, you can easily use the first version with a different local branch name:
$ git checkout -b sf origin/serverfix
/If you already have a local branch and want to set it to a remote branch you just pulled down, or want to change the
 upstream branch youre tracking, you can use the -u or --set-upstream-to option to git branch to explicitly set it at any time.
$ git branch -u origin/serverfix
Branch serverfix set up to track remote branch serverfix from origin.
Note- Upstream shorthand
When you have a tracking branch set up, you can reference its upstream branch with the
# @{upstream} or @{u} shorthand.
 So if youre on the master branch and its tracking origin/master, you can say something like
#git merge @{u} instead of
git merge origin/master if you wish.
/If you want to see what tracking branches you have set up, you can use the -vv option to git branch.
 This will list out your local branches with more information including what each branch is tracking and if your local branch is ahead, behind or both.
$ git branch -vv
Its important to note that these numbers are only since the last time you fetched from each server. This command does not
 reach out to the servers, its telling you about what it has cached from these servers locally. If you want totally up to date
ahead and behind numbers, youll need to fetch from all your remotes right before running this. You could /do that like this:
$ git fetch --all; git branch -vv

Pulling
/While the
 git fetch command will fetch all the changes on the server that you dont have yet, it will not modify your
working directory at all. It will simply get the data for you and let you merge it yourself. However, there is a command called
 git pull which is essentially a
 git fetch immediately followed by a
 git merge in most cases. If you have a tracking branch set up as demonstrated in the last section, either by
explicitly setting it or by having it created for you by the clone or checkout commands,
 git pull will look up what server and branch your current branch is tracking, fetch from that server and /then try to merge in that remote branch.
Generally its better to simply use the fetch and merge commands explicitly as the magic of git pull can often be confusing.

Deleting Remote Branches
Suppose youre /done with a remote branch — say you and your collaborators are finished with a feature and have
 merged it into your remotes master branch /or whatever branch your stable codeline is in/. You can delete a
remote branch using the --delete option to git push. If you want to delete your serverfix branch from the server, you run the following:
$ git push origin --delete serverfix
Basically all this does is to remove the pointer from the server. The Git server will generally keep the data there for a while until a
 garbage collection runs, so if it was accidentally deleted, its often easy to recover.

https://git-scm.com/book/en/v2/Git-Branching-Rebasing
Rebasing
In Git, there are two main ways to integrate changes from one branch into another: the merge and the rebase.
The Basic Rebase
/If you go back to an earlier example from Basic Merging, you can see that you diverged your work and made commits on two different branches.
The easiest way to integrate the branches, as weve already covered, is the merge command. It performs a three-way merge between the
 two latest branch snapshots /C3 and C4/ and the most recent common ancestor of the two /C2/, creating a new snapshot /and commit/.
However- there is another way: you can take the patch of the change that was introduced in C4 and reapply it on top of C3.
 In Git, this is called rebasing. With the rebase command, you can take all the changes that were committed on one branch and replay them on a different branch.
/For this example, you would check out the experiment branch, and /then rebase it onto the master branch as follows:
$ git checkout experiment
$ git rebase master
This operation works by going to the common ancestor of the two branches /the one youre on and the one youre rebasing onto/,
 getting the diff introduced by each commit of the branch youre on, saving those diffs to temporary files, resetting the
current branch to the same commit as the branch you are rebasing onto, and finally applying each change in turn.
At this point, you can go back to the master branch and /do a fast-forward merge.
$ git checkout master
$ git merge experiment
Now- the snapshot pointed to by C4/ is exactly the same as the one that was pointed to by C5 in the merge example.
 There is no difference in the end product of the integration, but rebasing makes for a cleaner history. If you examine the
log of a rebased branch, it looks like a linear history: it appears that all the work happened in series, even when it originally happened in parallel.
Often- youll /do this to make sure your commits apply cleanly on a remote branch — perhaps in a project to
 which youre trying to contribute but that you dont maintain. In this case, youd /do your work in a branch and /then
rebase your work onto origin/master when you were ready to submit your patches to the main project. That way, the
 maintainer doesnt have to /do any integration work — just a fast-forward or a clean apply.
Note that the snapshot pointed to by the final commit you end up with, whether its the last of the rebased commits for a
 rebase or the final merge commit after a merge, is the same snapshot — its only the history that is different.
Rebasing replays changes from one line of work onto another in the order they were introduced,
 whereas merging takes the endpoints and merges them together.

More Interesting Rebases
You can also have your rebase replay on something other than the rebase target branch. Take a history like A history with a
 topic branch off another topic branch, for example. You branched a topic branch /server/ to add some server-side functionality to
your project, and made a commit. Then, you branched off that to make the client-side changes /client/ and committed a
 few times. Finally, you went back to your server branch and did a few more commits.
Suppose you decide that you want to merge your client-side changes into your mainline for a release, but you want to
 hold off on the server-side changes until its tested further. You can take the changes on client that arent on server
/C8 and C9/ and replay them on your master branch by using the --onto option of git rebase:
$ git rebase --onto master server client
This basically says, /Take the client branch, figure out the patches since it diverged from the server branch, and
 replay these patches in the client branch as if it was based directly off the master branch instead./ Its a bit complex, but the result is pretty cool.
Now you can fast-forward your master branch /see Fast-forwarding your master branch to include the client branch changes/:
$ git checkout master
$ git merge client
Lets say you decide to pull in your server branch as well. You can rebase the server branch onto the master branch without having to
 check it out first by running
git rebase /basebranch/ /topicbranch/ — which checks out the topic branch /in this case, server/ for you and replays it
 onto the base branch /master/:
$ git rebase master server
This replays your server work on top of your master work, as shown in Rebasing your server branch on top of your master branch.
Then- you can fast-forward the base branch /master/:
$ git checkout master
$ git merge server
You can remove the client and server branches because all the work is integrated and you dont need them anymore,
 leaving your history for this entire process looking like Final commit history:
$ git branch -d client
$ git branch -d server

The Perils of Rebasing
Ahh- but the bliss of rebasing isnt without its drawbacks, which can be summed up in a single line:
/Do not rebase commits that exist outside your repository and that people may have based work on.
/If you follow that guideline, youll be fine. If you dont, people will hate you, and youll be scorned by friends and family.
When you rebase stuff, youre abandoning existing commits and creating new ones that are similar but different.
 /If you push commits somewhere and others pull them down and base work on them, and /then you rewrite those commits with
git rebase and push them up again, your collaborators will have to re-merge their work and things will get messy when
 you try to pull their work back into yours.

Rebase When You Rebase
/If you /do find yourself in a situation like this, Git has some further magic that might help you out. If someone on your
 team force pushes changes that overwrite work that youve based work on, your challenge is to figure out what is yours and what theyve rewritten.
It turns out that in addition to the commit SHA-1 checksum, Git also calculates a checksum that is based just on the
 patch introduced with the commit. This is called a 'patch-id'.
/If you pull down work that was rewritten and rebase it on top of the new commits from your partner, Git can often
 successfully figure out what is uniquely yours and apply them back on top of the new branch.
You can also simplify this by running a
 git pull --rebase instead of a normal git pull. Or you could /do it manually with a git fetch followed by a
 git rebase teamone/master in this case.
/If you are using git pull and want to make --rebase the default, you can set the pull.rebase config value with something like
 git config --global pull.rebase true.
/If you only ever rebase commits that have never left your own computer, youll be just fine. If you rebase commits that
 have been pushed, but that no one else has based commits from, youll also be fine. If you rebase commits that have
already been pushed publicly, and people may have based work on those commits, /then you may be in for some frustrating trouble, and the scorn of your teammates.
/If you or a partner does find it necessary at some point, make sure everyone knows to run
 git pull --rebase to try to make the pain after it happens a little bit simpler.

Rebase vs. Merge
Now that youve seen rebasing and merging in action, you may be wondering which one is better. Before we can answer this,
 lets step back a bit and talk about what history means.
One point of view on this is that your repositorys commit history is a record of what actually happened. Its a
 historical document, valuable in its own right, and shouldnt be tampered with. From this angle, changing the commit history is
almost blasphemous; youre lying about what actually transpired. So what if there was a messy series of merge commits?
 Thats how it happened, and the repository should preserve that for posterity.
The opposing point of view is that the commit history is the story of how your project was made. You wouldnt publish the
 first draft of a book, so why show your messy work? When youre working on a project, you may need a record of all your
missteps and dead-end paths, but when its time to show your work to the world, you may want to tell a more coherent story of
 how to get from A to B. People in this camp use tools like rebase and filter-branch to rewrite their commits before theyre
merged into the mainline branch. They use tools like rebase and filter-branch, to tell the story in the way thats best for future readers.
Now- to the question of whether merging or rebasing is better: hopefully youll see that its not that simple. Git is a
 powerful tool, and allows you to /do many things to and with your history, but every team and every project is different.
Now that you know how both of these things work, its up to you to decide which one is best for your particular situation.
You can get the best of both worlds: rebase local changes before pushing to clean up your work, but never rebase anything that youve pushed somewhere.

https://git-scm.com/book/en/v2/Git-on-the-Server-The-Protocols
Git on the Server - The Protocols
In order to /do any collaboration in Git, youll need to have a remote Git repository. Although you can technically
 push changes to and pull changes from individual repositories, doing so is discouraged because you can fairly easily
confuse what theyre working on if youre not careful. Furthermore, you want your collaborators to be able to access the
 repository even if your computer is offline — having a more reliable common repository is often useful.
Therefore- the preferred method for collaborating with someone is to set up an intermediate repository that you both have
 access to, and push to and pull from that.
Running a Git server is fairly straightforward. First, you choose which protocols you want your server to support.
A remote repository is generally a bare repository — a Git repository that has no working directory. Because the repository is
 only used as a collaboration point, there is no reason to have a snapshot checked out on disk; its just the Git data.
In the simplest terms, a bare repository is the contents of your projects .git directory and nothing else.

The Protocols
Git can use four distinct protocols to transfer data:
 Local- HTTP, Secure Shell /SSH/ and Git. Here well discuss what they are and in what basic circumstances you would want /or not want/ to use them.

https://git-scm.com/book/en/v2/Git-on-the-Server-Getting-Git-on-a-Server
In order to initially set up any Git server, you have to export an existing repository into a new bare repository — a repository that
 doesnt contain a working directory. This is generally straightforward to do. In order to clone your repository to create a
new bare repository, you run the clone command with the --bare option. By convention, bare repository directory names end with the suffix .git, like so:
$ git clone --bare my_project my_project.git
Cloning into bare repository 'my_project.git'...done.
You should now have a copy of the Git directory data in your my_project.git directory.
This is roughly equivalent to something like:
$ cp -Rf my_project/.git my_project.git
There are a couple of minor differences in the configuration file but, for your purpose, this is close to the same thing.
 It takes the Git repository by itself, without a working directory, and creates a directory specifically for it alone.

https://git-scm.com/book/en/v2/Distributed-Git-Distributed-Workflows
Distributed Workflows
In contrast with Centralized Version Control Systems, the distributed nature of Git allows you to be far more flexible in
 how developers collaborate on projects. In centralized systems, every developer is a node working more or less equally with a
central hub. In Git, however, every developer is potentially both a node and a hub; that is, every developer can both contribute code to
 other repositories and maintain a public repository on which others can base their work and which they can contribute to.
This presents a vast range of workflow possibilities for your project and/or your team

Centralized Workflow
In centralized systems, there is generally a single collaboration model — the centralized workflow. One central hub,
 or repository, can accept code, and everyone synchronizes their work with it. A number of developers are
nodes — consumers of that hub — and synchronize with that centralized location.
This means that if two developers clone from the hub and both make changes, the first developer to push their changes back up can /do so
 with no problems. The second developer must merge in the first ones work before pushing changes up, so as not to overwrite the
first developers changes. This concept is as true in Git as it is in Subversion or any CVCS, and this model works perfectly well in Git.
/If you are already comfortable with a centralized workflow in your company or team, you can easily continue using that
 workflow with Git. Simply set up a single repository, and give everyone on your team push access; Git wont let users overwrite each other.
Say John and Jessica both start working at the same time. John finishes his change and pushes it to the server. Then Jessica tries to
 push her changes, but the server rejects them. She is told that shes trying to push non-fast-forward changes and that
she wont be able to /do so until she fetches and merges. This workflow is attractive to a lot of people because its a
 paradigm that many are familiar and comfortable with.
This is also not limited to small teams. With Gits branching model, its possible for hundreds of developers to
 successfully work on a single project through dozens of branches simultaneously.

Integration-Manager Workflow
Because Git allows you to have multiple remote repositories, its possible to have a workflow where each developer has
 write access to their own public repository and read access to everyone elses. This scenario often includes a
canonical repository that represents the 'official' project. To contribute to that project, you create your own public clone of the
 project and push your changes to it. Then, you can send a request to the maintainer of the main project to pull in your changes.
The maintainer can /then add your repository as a remote, test your changes locally, merge them into their branch, and
 push back to their repository. The process works as follows:
The project maintainer pushes to their public repository.
A contributor clones that repository and makes changes.
The contributor pushes to their own public copy.
The contributor sends the maintainer an email asking them to pull changes.
The maintainer adds the contributors repository as a remote and merges locally.
The maintainer pushes merged changes to the main repository.
This is a very common workflow with hub-based tools like GitHub or GitLab, where its easy to fork a project and
 push your changes into your fork for everyone to see. One of the main advantages of this approach is that you can continue to
work- and the maintainer of the main repository can pull in your changes at any time. Contributors dont have to wait for the
 project to incorporate their changes — each party can work at their own pace.

Dictator and Lieutenants Workflow
This is a variant of a multiple-repository workflow. Its generally used by huge projects with hundreds of collaborators;
 one famous example is the Linux kernel. Various integration managers are in charge of certain parts of the repository;
theyre called lieutenants. All the lieutenants have one integration manager known as the benevolent dictator.
 The benevolent dictator pushes from their directory to a reference repository from which all the collaborators need to pull.
The process works like this:
Regular developers work on their topic branch and rebase their work on top of master. The master branch is that of the
 reference repository to which the dictator pushes.
Lieutenants merge the developers topic branches into their master branch.
The dictator merges the lieutenants master branches into the dictators master branch.
Finally- the dictator pushes that master branch to the reference repository so the other developers can rebase on it.
This kind of workflow isnt common, but can be useful in very big projects, or in highly hierarchical environments.
 It allows the project leader /the dictator/ to delegate much of the work and collect large subsets of code at multiple points before integrating them.

Patterns for Managing Source Code Branches
Note- Martin Fowler has made a guide 'Patterns for Managing Source Code Branches'. This guide covers all the
 common Git workflows, and explains how/when to use them. Theres also a section comparing high and low integration frequencies.
https://martinfowler.com/articles/branching-patterns.html

https://git-scm.com/book/en/v2/Distributed-Git-Contributing-to-a-Project
 Distributed Git - Contributing to a Project
Contributing to a Project
The main difficulty with describing how to contribute to a project are the numerous variations on how to /do that.
 Because Git is very flexible, people can and /do work together in many ways, and its problematic to describe how you
should contribute — every project is a bit different. Some of the variables involved are active contributor count,
 chosen workflow, your commit access, and possibly the external contribution method.
The first variable is active contributor count — how many users are actively contributing code to this project, and
 how often? In many instances, youll have two or three developers with a few commits a day, or possibly less for somewhat
dormant projects. For larger companies or projects, the number of developers could be in the thousands, with hundreds or
 thousands of commits coming in each day. This is important because with more and more developers, you run into more issues with
making sure your code applies cleanly or can be easily merged. Changes you submit may be rendered obsolete or severely broken by
 work that is merged in while you were working or while your changes were waiting to be approved or applied. How can you
keep your code consistently up to date and your commits valid?
The next variable is the workflow in use for the project. Is it centralized, with each developer having equal write access to the
 main codeline? Does the project have a maintainer or integration manager who checks all the patches? Are all the patches
peer-reviewed and approved? Are you involved in that process? Is a lieutenant system in place, and /do you have to submit your work to them first?
The next variable is your commit access. The workflow required in order to contribute to a project is much different if you have
 write access to the project than if you dont. If you dont have write access, how does the project prefer to accept contributed work?
Does it even have a policy? How much work are you contributing at a time? How often /do you contribute?
All these questions can affect how you contribute effectively to a project and what workflows are preferred or available to you.

Commit Guidelines
First- your submissions should not contain any whitespace errors. Git provides an easy way to check for this — before you commit, run
 git diff --check, which identifies possible whitespace errors and lists them for you.
https://git-scm.com/book/en/v2/ch00/_interactive_staging
https://git-scm.com/book/en/v2/ch00/_rewriting_history
https://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html

Private Small Team
The simplest setup youre likely to encounter is a private project with one or two other developers. 'Private,' in this context,
 means closed-source — not accessible to the outside world. You and the other developers all have push access to the repository.
In this environment, you can follow a workflow similar to what you might /do when using Subversion or another centralized system.
 You still get the advantages of things like offline committing and vastly simpler branching and merging, but the workflow can be
very similar; the main difference is that merges happen client-side rather than on the server at commit time.
$ git log --no-merges issue54..origin/master
The issue54..origin/master syntax is a log filter that asks Git to display only those commits that are on the latter branch
 /in this case origin/master/ and that are not on the first branch /in this case issue54/. Well go over this syntax in detail in Commit Ranges.
https://git-scm.com/book/en/v2/ch00/_commit_ranges

Private Managed Team
Youll learn how to work in an environment where small groups collaborate on features, after which those team-based contributions are integrated by another party.
$ git checkout -b featureA
$ git push -u origin featureA
$ git push -u origin featureB:featureBee
This is called a refspec. See The Refspec for a more detailed discussion of Git refspecs and different things you can /do with them.
 Also notice the -u flag; this is short for --set-upstream, which configures the branches for easier pushing and pulling later.
https://git-scm.com/book/en/v2/ch00/_refspec
$ git log featureA..origin/featureA

Forked Public Project
Contributing to public projects is a bit different. Because you dont have the permissions to directly update branches on the
 project- you have to get the work to the maintainers some other way. This first example describes contributing via forking on
Git hosts that support easy forking. Many hosting sites support this /including GitHub, BitBucket, repo.or.cz, and others/, and
 many project maintainers expect this style of contribution. The next section deals with projects that prefer to accept contributed patches via email.
First- youll probably want to clone the main repository, create a topic branch for the patch or patch series youre planning to
 contribute- and /do your work there. The sequence looks basically like this:
$ git clone /url/
$ cd project
$ git checkout -b featureA
#  ... work ...
$ git commit
#  ... work ...
$ git commit
Note- You may want to use rebase -i to squash your work down to a single commit, or rearrange the work in the commits to
 make the patch easier for the maintainer to review — see Rewriting History for more information about interactive rebasing.
https://git-scm.com/book/en/v2/ch00/_rewriting_history
When your branch work is finished and youre ready to contribute it back to the maintainers, go to the original project page and
 click the 'Fork' button, creating your own writable fork of the project. You /then need to add this repository URL as a
new remote of your local repository; in this example, lets call it myfork:
$ git remote add myfork /url/
You /then need to push your new work to this repository. Its easiest to push the topic branch youre working on to your
 forked repository, rather than merging that work into your master branch and pushing that. The reason is that if your
work isnt accepted or is cherry-picked, you dont have to rewind your master branch /the Git cherry-pick operation is covered in
 more detail in Rebasing and Cherry-Picking Workflows/. If the maintainers merge, rebase, or cherry-pick your work,
youll eventually get it back via pulling from their repository anyhow.
https://git-scm.com/book/en/v2/ch00/_rebase_cherry_pick
In any event, you can push your work with:
$ git push -u myfork featureA
Once your work has been pushed to your fork of the repository, you need to notify the maintainers of the original project that
 you have work youd like them to merge. This is often called a pull request, and you typically generate such a request either
via the website — GitHub has its own 'Pull Request' mechanism that well go over in GitHub — or you can run the
 git request-pull command and email the subsequent output to the project maintainer manually.
https://git-scm.com/book/en/v2/ch00/ch06-github
The git request-pull command takes the base branch into which you want your topic branch pulled and the Git repository URL you
 want them to pull from, and produces a summary of all the changes youre asking to be pulled. For instance, if Jessica wants to
send John a pull request, and shes /done two commits on the topic branch she just pushed, she can run this:
$ git request-pull origin/master myfork
This output can be sent to the maintainer — it tells them where the work was branched from, summarizes the commits,
 and identifies from where the new work is to be pulled.
On a project for which youre not the maintainer, its generally easier to have a branch like master always track origin/master and
 to /do your work in topic branches that you can easily discard if theyre rejected. Having work themes isolated into
topic branches also makes it easier for you to rebase your work if the tip of the main repository has moved in the
 meantime and your commits no longer apply cleanly.
/if you want to submit a second topic of work to the project, dont continue working on the topic branch you just
 pushed up — start over from the main repositorys master branch:
$ git checkout -b featureB origin/master
#  ... work ...
$ git commit
$ git push myfork featureB
$ git request-pull origin/master myfork
#  ... email generated request pull to maintainer ...
$ git fetch origin
Now- each of your topics is contained within a silo — similar to a patch queue — that you can rewrite, rebase, and
 modify without the topics interfering or interdepending on each other
Lets say the project maintainer has pulled in a bunch of other patches and tried your first branch, but it no longer cleanly merges.
 In this case, you can try to rebase that branch on top of origin/master, resolve the conflicts for the maintainer, and /then resubmit your changes:
$ git checkout featureA
$ git rebase origin/master
$ git push -f myfork featureA
This rewrites your history to now look like Commit history after featureA work.
Because you rebased the branch, you have to specify the -f to your push command in order to be able to replace the
 featureA branch on the server with a commit that isnt a descendant of it. An alternative would be to push this new work to a
different branch on the server /perhaps called featureAv2/.
Lets look at one more possible scenario: the maintainer has looked at work in your second branch and likes the concept but
 would like you to change an implementation detail. Youll also take this opportunity to move the work to be based off the
projects current master branch. You start a new branch based off the current origin/master branch, squash the
 featureB changes there, resolve any conflicts, make the implementation change, and /then push that as a new branch:
$ git checkout -b featureBv2 origin/master
$ git merge --squash featureB
#  ... change implementation ...
$ git commit
$ git push myfork featureBv2
The --squash option takes all the work on the merged branch and squashes it into one changeset producing the
 repository state as if a real merge happened, without actually making a merge commit. This means your future commit will
have one parent only and allows you to introduce all the changes from another branch and /then make more changes before
 recording the new commit. Also the --no-commit option can be useful to delay the merge commit in case of the default merge process.
At this point, you can notify the maintainer that youve made the requested changes, and that they can find those changes in your featureBv2 branch.

https://git-scm.com/book/en/v2/Distributed-Git-Maintaining-a-Project
Maintaining a Project
In addition to knowing how to contribute effectively to a project, youll likely need to know how to maintain one.
 This can consist of accepting and applying patches generated via format-patch and emailed to you, or integrating changes in
remote branches for repositories youve added as remotes to your project. Whether you maintain a canonical repository or
 want to help by verifying or approving patches, you need to know how to accept work in a way that is clearest for other
contributors and sustainable by you over the long run.

Working in Topic Branches
When youre thinking of integrating new work, its generally a good idea to try it out in a topic branch — a
 temporary branch specifically made to try out that new work. This way, its easy to tweak a patch individually and
leave it if its not working until you have time to come back to it. If you create a simple branch name based on the
 theme of the work youre going to try, such as ruby_client or something similarly descriptive, you can easily remember it if
you have to abandon it for a while and come back later. The maintainer of the Git project tends to namespace these
 branches as well — such as sc/ruby_client, where sc is short for the person who contributed the work.
As youll remember, you can create the branch based off your master branch like this:
$ git branch sc/ruby_client master
Or- if you want to also switch to it immediately, you can use the checkout -b option:
$ git checkout -b sc/ruby_client master
Now youre ready to add the contributed work that you received into this topic branch and determine if you want to
 merge it into your longer-term branches.

Checking Out Remote Branches
/If your contribution came from a Git user who set up their own repository, pushed a number of changes into it, and /then
  sent you the URL to the repository and the name of the remote branch the changes are in, you can add them as a remote and /do merges locally.
/For instance, if Jessica sends you an email saying that she has a great new feature in the ruby-client branch of her repository,
 you can test it by adding the remote and checking out that branch locally:
$ git remote add jessica https://github.com/jessica/myproject.git
$ git fetch jessica
$ git checkout -b rubyclient jessica/ruby-client
/If she emails you again later with another branch containing another great feature, you could directly fetch and
 checkout because you already have the remote setup.
/If you arent working with a person consistently but still want to pull from them in this way, you can provide the URL of the
 remote repository to the git pull command. This does a one-time pull and doesnt save the URL as a remote reference:
$ git pull https://github.com/onetimeguy/project

Determining What Is Introduced
Now you have a topic branch that contains contributed work. At this point, you can determine what youd like to /do with it.
Its often helpful to get a review of all the commits that are in this branch but that arent in your master branch.
 You can exclude commits in the master branch by adding the --not option before the branch name. This does the same thing as the
master..contrib format that we used earlier. For example, if your contributor sends you two patches and you create a
 branch called contrib and applied those patches there, you can run this:
$ git log contrib --not master
To see what changes each commit introduces, remember that you can pass the -p option to git log and it will append the diff introduced to each commit.
To see a full diff of what would happen if you were to merge this topic branch with another branch, you may have to
 use a weird trick to get the correct results. You may think to run this:
$ git diff master
This command gives you a diff, but it may be misleading. If your master branch has moved forward since you created the
 topic branch from it, /then youll get seemingly strange results. This happens because Git directly compares the snapshots of the
last commit of the topic branch youre on and the snapshot of the last commit on the master branch. For example, if youve added a
 line in a file on the master branch, a direct comparison of the snapshots will look like the topic branch is going to remove that line.
/If master is a direct ancestor of your topic branch, this isnt a problem; but if the two histories have diverged, the
 diff will look like youre adding all the new stuff in your topic branch and removing everything unique to the master branch.
What you really want to see are the changes added to the topic branch — the work youll introduce if you merge this
 branch with master. You /do that by having Git compare the last commit on your topic branch with the first common ancestor it has with the master branch.
Technically- you can /do that by explicitly figuring out the common ancestor and /then running your diff on it:
$ git merge-base contrib master
36c7dba2c95e6bbb78dfa822519ecfec6e1ca649
$ git diff 36c7db
or- more concisely:
$ git diff "$(git merge-base contrib master)"
However- neither of those is particularly convenient, so Git provides another shorthand for doing the same thing:
 the triple-dot syntax. In the context of the git diff command, you can put three periods after another branch to /do a
diff between the last commit of the branch youre on and its common ancestor with another branch:
$ git diff master...contrib
This command shows you only the work your current topic branch has introduced since its common ancestor with master.

Rebasing and Cherry-Picking Workflows
Other maintainers prefer to rebase or cherry-pick contributed work on top of their master branch, rather than merging it in,
 to keep a mostly linear history. When you have work in a topic branch and have determined that you want to integrate it,
you move to that branch and run the rebase command to rebuild the changes on top of your current master /or develop, and so on/ branch. If that
 works well, you can fast-forward your master branch, and youll end up with a linear project history.
The other way to move introduced work from one branch to another is to cherry-pick it. A cherry-pick in Git is like a
 rebase for a single commit. It takes the patch that was introduced in a commit and tries to reapply it on the branch youre currently on.
This is useful if you have a number of commits on a topic branch and you want to integrate only one of them, or if you
 only have one commit on a topic branch and youd prefer to cherry-pick it rather than run rebase.
/If you want to pull commit e43a6 into your master branch, you can run:
$ git cherry-pick e43a6
Finished one cherry-pick.
#[master]: created a0a41a9: "More friendly message when locking the index fails."
This pulls the same change introduced in e43a6, but you get a new commit SHA-1 value, because the date applied is different.

Rerere
/If youre doing lots of merging and rebasing, or youre maintaining a long-lived topic branch, Git has a feature called 'rerere' that can help.
Rerere stands for 'reuse recorded resolution' — its a way of shortcutting manual conflict resolution. When rerere is enabled,
 Git will keep a set of pre- and post-images from successful merges, and if it notices that theres a conflict that
looks exactly like one youve already fixed, itll just use the fix from last time, without bothering you with it.
This feature comes in two parts: a configuration setting and a command. The configuration setting is rerere.enabled,
 and its handy enough to put in your global config:
$ git config --global rerere.enabled true
Now- whenever you /do a merge that resolves conflicts, the resolution will be recorded in the cache in case you need it in the future.

Tagging Your Releases
When youve decided to cut a release, youll probably want to assign a tag so you can re-create that release at any
 point going forward. You can create a new tag as discussed in Git Basics. If you decide to sign the tag as the maintainer,
the tagging may look something like this:
$ git tag -s v1.5 -m 'my signed 1.5 tag'

Generating a Build Number
Because Git doesnt have monotonically increasing numbers like 'v123' or the equivalent to go with each commit, if you want to
 have a human-readable name to go with a commit, you can run git describe on that commit. In response, Git generates a
string consisting of the name of the most recent tag earlier than that commit, followed by the number of commits since that tag,
 followed finally by a partial SHA-1 value of the commit being described /prefixed with the letter "g" meaning Git/:
$ git describe master
v1.6.2-rc1-20-g8c5b85c

Preparing a Release
Now you want to release a build. One of the things youll want to /do is create an archive of the latest snapshot of your code for
 those poor souls who dont use Git. The command to /do this is git archive:
#$ git archive master --prefix='project/' | gzip > `git describe master`.tar.gz
/If someone opens that tarball, they get the latest snapshot of your project under a project directory. You can also
 create a zip archive in much the same way, but by passing the --format=zip option to git archive:
#$ git archive master --prefix='project/' --format=zip > `git describe master`.zip
You now have a nice tarball and a zip archive of your project release that you can upload to your website or email to people.

The Shortlog
Its time to email your mailing list of people who want to know whats happening in your project. A nice way of quickly
 getting a sort of changelog of what has been added to your project since your last release or email is to use the
git shortlog command. It summarizes all the commits in the range you give it, for example, the following gives you a
 summary of all the commits since your last release, if your last release was named v1.0.1:
$ git shortlog --no-merges master --not v1.0.1

The GitHub Flow
GitHub is designed around a particular collaboration workflow, centered on Pull Requests. This flow works whether youre
 collaborating with a tightly-knit team in a single shared repository, or a globally-distributed company or network of
strangers contributing to a project through dozens of forks. It is centered on the Topic Branches workflow covered in Git Branching.
Heres how it generally works:
Fork the project.
Create a topic branch from master.
Make some commits to improve the project.
Push this branch to your GitHub project.
Open a Pull Request on GitHub.
Discuss- and optionally continue committing.
The project owner merges or closes the Pull Request.
Sync the updated master back to your fork.
This is basically the Integration Manager workflow covered in Integration-Manager Workflow, but instead of using email to
 communicate and review changes, teams use GitHubs web based tools.
Lets walk through an example of proposing a change to an open source project hosted on GitHub using this flow.
Tip - You can use the official GitHub CLI tool instead of the GitHub web interface for most things. The tool can be
 used on Windows, macOS, and Linux systems. Go to the GitHub CLI homepage for installation instructions and the manual.
https://cli.github.com

https://git-scm.com/book/en/v2/Git-Tools-Revision-Selection
Revision Selection
Git allows you to refer to a single commit, set of commits, or range of commits in a number of ways.
 They arent necessarily obvious but are helpful to know.

Single Revisions
You can obviously refer to any single commit by its full, 40-character SHA-1 hash, but there are more human-friendly ways to
 refer to commits as well. This section outlines the various ways you can refer to any commit.
Short SHA-1
Git is smart enough to figure out what commit youre referring to if you provide the first few characters of the SHA-1 hash,
 as long as that partial hash is at least four characters long and unambiguous; that is, no other object in the object database can
have a hash that begins with the same prefix.
/For example, to examine a specific commit where you know you added certain functionality, you might first run the git log command to locate the commit:
$ git log
In this case, say youre interested in the commit whose hash begins with 1c002dd…​. You can inspect that commit with any of the
 following variations of git show /assuming the shorter versions are unambiguous/:
$ git show 1c002dd4b536e7479fe34593e72e6c6c1819e53b
$ git show 1c002dd4b536e7479f
$ git show 1c002d
Git can figure out a short, unique abbreviation for your SHA-1 values. If you pass --abbrev-commit to the git log command,
 the output will use shorter values but keep them unique; it defaults to using seven characters but makes them longer if necessary to keep the SHA-1 unambiguous:
$ git log --abbrev-commit --pretty=oneline
ca82a6d Change the version number
085bb3b Remove unnecessary test code
a11bef0 Initial commit

Branch References
One straightforward way to refer to a particular commit is if its the commit at the tip of a branch; in that case,
 you can simply use the branch name in any Git command that expects a reference to a commit. For instance, if you want to
examine the last commit object on a branch, the following commands are equivalent, assuming that the topic1 branch points to commit ca82a6d…​:
$ git show ca82a6dff817ec66f44342007202690a93763949
$ git show topic1
/If you want to see which specific SHA-1 a branch points to, or if you want to see what any of these examples boils down to in terms of SHA-1s,
 you can use a Git plumbing tool called rev-parse. You can see Git Internals for more information about plumbing tools;
basically- rev-parse exists for lower-level operations and isnt designed to be used in day-to-day operations. However, it can be
 helpful sometimes when you need to see whats really going on. Here you can run rev-parse on your branch.
$ git rev-parse topic1
ca82a6dff817ec66f44342007202690a93763949

RefLog Shortnames
One of the things Git does in the background while youre working away is keep a 'reflog' — a log of where your HEAD and
 branch references have been for the last few months.
You can see your reflog by using git reflog:
$ git reflog
Every time your branch tip is updated for any reason, Git stores that information for you in this temporary history.
 You can use your reflog data to refer to older commits as well. For example, if you want to see the fifth prior value of the
HEAD of your repository, you can use the "@{5}" reference that you see in the reflog output:
$ "git show HEAD@{5}"
You can also use this syntax to see where a branch was some specific amount of time ago. For instance, to see where
 your master branch was yesterday, you can type:
$ "git show master@{yesterday}"
That would show you where the tip of your master branch was yesterday. This technique only works for data thats still in
 your reflog, so you cant use it to look for commits older than a few months.
To see reflog information formatted like the git log output, you can run git log -g:
$ git log -g master
Its important to note that reflog information is strictly local — its a log only of what youve /done in your repository.
 The references wont be the same on someone elses copy of the repository; also- right after you initially clone a repository,
youll have an empty reflog, as no activity has occurred yet in your repository.

Ancestry References
The other main way to specify a commit is via its ancestry. If you place a ^ /caret/ at the end of a reference, Git resolves it to
 mean the parent of that commit. Suppose you look at the history of your project:
$ git log --pretty=format:'%h %s' --graph
Then- you can see the previous commit by specifying HEAD^, which means 'the parent of HEAD':
$ git show HEAD^
You can also specify a number after the ^ to identify which parent you want;
 /for example, d921970^2 means 'the second parent of d921970.' This syntax is useful only for merge commits, which have
more than one parent — the first parent of a merge commit is from the branch you were on when
 you merged /frequently master/, while the second parent of a merge commit is from the branch that was merged /say, topic/:
The other main ancestry specification is the / tilde. This also refers to the first parent, so HEAD~ and HEAD^ are equivalent.
 The difference becomes apparent when you specify a number. HEAD~2 means 'the first parent of the first parent,' or
'the grandparent' — it traverses the first parents the number of times you specify. For example, in the history listed earlier, HEAD~3 would be:
$ git show HEAD~3
This can also be written HEAD~~~, which again is the first parent of the first parent of the first parent:
$ git show HEAD~~~
You can also combine these syntaxes — you can get the second parent of the
 previous reference /assuming it was a merge commit/ by using HEAD~3^2, and so on.

Commit Ranges
Now that you can specify individual commits, lets see how to specify ranges of commits. This is particularly useful for
 managing your branches — if you have a lot of branches, you can use range specifications to answer questions such as,
'What work is on this branch that I havent yet merged into my main branch?'

Double Dot
The most common range specification is the double-dot syntax. This basically asks Git to resolve a range of commits that
 are reachable from one commit but arent reachable from another.
You can ask Git to show you a log of just those commits with master..experiment — that means 'all commits reachable from experiment that arent reachable from master.'
$ 'git log master..experiment'
If- on the other hand, you want to see the opposite — all commits in master that arent in experiment — you can reverse the
 branch names. experiment..master shows you everything in master not reachable from experiment:
$ 'git log experiment..master'
Another frequent use of this syntax is to see what youre about to push to a remote:
$ git log origin/master..HEAD
This command shows you any commits in your current branch that arent in the master branch on your origin remote. If you run a
 git push and your current branch is tracking origin/master, the commits listed by git log origin/master..HEAD are the
commits that will be transferred to the server. You can also leave off one side of the syntax to have Git assume HEAD. For example,
 you can get the same results as in the previous example by typing git log origin/master.. — Git substitutes HEAD if one side is missing.

Multiple Points
The double-dot syntax is useful as a shorthand, but perhaps you want to specify more than two branches to indicate your revision,
 such as seeing what commits are in any of several branches that arent in the branch youre currently on. Git allows you to do-
this by using either the ^ character or --not before any reference from which you dont want to see reachable commits. Thus, the
 following three commands are equivalent:
$ git log refA..refB
$ git log ^refA refB
$ git log refB --not refA
This is nice because with this syntax you can specify more than two references in your query, which you cannot do- with the
 double-dot syntax. For instance, if you want to see all commits that are reachable from refA or refB but not from refC, you can use either of:
$ git log refA refB ^refC
$ git log refA refB --not refC

Triple Dot
The last major range-selection syntax is the triple-dot syntax, which specifies all the commits that are reachable by either of
 two references but not by both of them. If you want to see what is in master or experiment but not any common references, you can run:
$ git log master...experiment
A common switch to use with the log command in this case is --left-right, which shows you which side of the
 range each commit is in. This helps make the output more useful:
$ git log --left-right master...experiment

https://git-scm.com/book/en/v2/Git-Tools-Interactive-Staging
Interactive Staging
Interactive Git commands that can help you craft your commits to include only certain combinations and parts of files.
 These tools are helpful if you modify a number of files extensively, then- decide that you want those changes to be
partitioned into several focused commits rather than one big messy commit. This way, you can make sure your commits are
 logically separate changesets and can be reviewed easily by the developers working with you.
/If you run git add with the -i or --interactive option, Git enters an interactive shell mode, displaying something like this:
$ git add -i

Staging Patches
Its also possible for Git to stage certain parts of files and not the rest.
You also dont need to be in interactive add mode to do- the partial-file staging — you can start the same script by using
 git add -p or git add --patch on the command line. Furthermore, you can use patch mode for partially resetting files with the
 git reset --patch command, for checking out parts of files with the
 git checkout --patch command and for stashing parts of files with the
 git stash save --patch command.
Well go into more details on each of these as we get to more advanced usages of these commands.

https://git-scm.com/book/en/v2/Git-Tools-Stashing-and-Cleaning
Stashing and Cleaning
Often- when youve been working on part of your project, things are in a messy state and you want to switch branches for a
 bit to work on something else. The problem is, you dont want to do- a commit of half-done work just so you can get back to
this point later. The answer to this issue is the git stash command.
Stashing takes the dirty state of your working directory — that is, your modified tracked files and
 staged changes — and saves it on a stack of unfinished changes that you can reapply at any time /even on a different branch/.
Note- Migrating to git stash push
As of late October 2017, there has been extensive discussion on the Git mailing list, wherein the command
 git stash save is being deprecated in favour of the existing alternative git stash push. The main reason for this is that
git stash push introduces the option of stashing selected pathspecs, something git stash save does not support.
git stash save is not going away any time soon, so dont worry about it suddenly disappearing. But you might want to
 start migrating over to the push alternative for the new functionality.

Stashing Your Work
To push a new stash onto your stack, run git stash or git stash push:
$ git stash
$ git stash list
$ git stash apply
$ git stash apply --index
$ git stash pop
$ "git stash drop stash@{0}"

Creative Stashing
There are a few stash variants that may also be helpful. The first option that is quite popular is the --keep-index option to the
 git stash command. This tells Git to not only include all staged content in the stash being created, but simultaneously leave it in the index.
$ git stash --keep-index
By default, git stash will stash only modified and staged tracked files. If you specify --include-untracked or -u, Git will
 include untracked files in the stash being created. However, including untracked files in the stash will still not
include explicitly ignored files; to additionally include ignored files, use --all , or just -a.
$ git stash -u
Finally- if you specify the --patch flag, Git will not stash everything that is modified but will instead prompt you
 interactively which of the changes you would like to stash and which you would like to keep in your working directory.
$ git stash --patch

Creating a Branch from a Stash
/If you stash some work, leave it there for a while, and continue on the branch from which you stashed the work, you may
 have a problem reapplying the work. If the apply tries to modify a file that youve since modified, youll get a
merge conflict and will have to try to resolve it. If you want an easier way to test the stashed changes again, you can run
 git stash branch new_branchname, which creates a new branch for you with your selected branch name, checks out the
commit you were on when you stashed your work, reapplies your work there, and then- drops the stash if it applies successfully:
$ git stash branch testchanges

Cleaning your Working Directory
Finally- you may not want to stash some work or files in your working directory, but simply get rid of them; thats what the
 git clean command is for.
Youll want to be pretty careful with this command, since its designed to remove files from your working directory that are
 not tracked. If you change your mind, there is often no retrieving the content of those files. A safer option is to run
git stash --all to remove everything but save it in a stash.
Assuming you do- want to remove cruft files or clean your working directory, you can do- so with git clean. To remove all the
 untracked files in your working directory, you can run
git clean -f -d, which removes any files and also any subdirectories that become empty as a result.
 The -f means 'force' or 'really do this,' and is required if the Git configuration variable clean.requireForce is not explicitly set to false.
/If you ever want to see what it would do, you can run the command with the --dry-run or -n option, which means 'do a dry run and tell me what you would have removed'.
$ git clean -d -n
By default, the git clean command will only remove untracked files that are not ignored. Any file that matches a pattern in your
 .gitignore or other ignore files will not be removed. If you want to remove those files too, such as to remove all .o files generated from a
build so you can do- a fully clean build, you can add a -x to the clean command.
$ git clean -n -d
$ git clean -n -d -x
/If you dont know what the git clean command is going to do, always run it with a -n first to double check before
 changing the -n to a -f and doing it for real. The other way you can be careful about the process is to run it with the -i or 'interactive' flag.
This will run the clean command in an interactive mode.
$ git clean -x -i
Note- There is a quirky situation where you might need to be extra forceful in asking Git to clean your working directory.
 /If you happen to be in a working directory under which youve copied or cloned other Git repositories , perhaps as submodules, even
git clean -fd will refuse to delete those directories. In cases like that, you need to add a second -f option for emphasis.

https://git-scm.com/book/en/v2/Git-Tools-Signing-Your-Work
Signing Your Work
Git is cryptographically secure, but its not foolproof. If youre taking work from others on the internet and want to verify that
 commits are actually from a trusted source, Git has a few ways to sign and verify work using GPG.

GPG Introduction
First of all, if you want to sign anything you need to get GPG configured and your personal key installed.
$ gpg --list-keys
/If you dont have a key installed, you can generate one with gpg --gen-key.
$ gpg --gen-key
Once you have a private key to sign with, you can configure Git to use it for signing things by setting the user.signingkey config setting.
$ git config --global user.signingkey 0A46826A!
Now Git will use your key by default to sign tags and commits if you want.

Signing Tags
/If you have a GPG private key set up, you can now use it to sign new tags. All you have to do- is use -s instead of -a:
$ git tag -s v1.5 -m 'my signed 1.5 tag'
/If you run git show on that tag, you can see your GPG signature attached to it:
$ git show v1.5

Verifying Tags
To verify a signed tag, you use git tag -v <tag-name>. This command uses GPG to verify the signature. You need the
 signers public key in your keyring for this to work properly:
$ git tag -v v1.4.2.1

Signing Commits
In more recent versions of Git /v1.7.9 and above/, you can now also sign individual commits. If youre interested in
 signing commits directly instead of just the tags, all you need to do- is add a -S to your git commit command.
$ git commit -a -S -m 'Signed commit'
To see and verify these signatures, there is also a --show-signature option to git log.
$ git log --show-signature -1
Additionally- you can configure git log to check any signatures it finds and list them in its output with the %G? format.
$ git log --pretty="format:%h %G? %aN  %s"
/In Git 1.8.3 and later, git merge and git pull can be told to inspect and reject when merging a commit that does not carry a
 trusted GPG signature with the --verify-signatures command.
/If you use this option when merging a branch and it contains commits that are not signed and valid, the merge will not work.
$ git merge --verify-signatures non-verify
/If the merge contains only valid signed commits, the merge command will show you all the signatures it has
 checked and then- move forward with the merge.
$ git merge --verify-signatures signed-branch
You can also use the -S option with the git merge command to sign the resulting merge commit itself. The following example both
 verifies that every commit in the branch to be merged is signed and furthermore signs the resulting merge commit.
$ git merge --verify-signatures -S  signed-branch

Everyone Must Sign
Signing tags and commits is great, but if you decide to use this in your normal workflow, youll have to make sure that
 everyone on your team understands how to do- so. This can be achieved by asking everyone working with the repository to run
git config --local commit.gpgsign true to automatically have all of their commits in the repository signed by default. If
 you dont, youll end up spending a lot of time helping people figure out how to rewrite their commits with signed versions.
Make sure you understand GPG and the benefits of signing things before adopting this as part of your standard workflow.

https://git-scm.com/book/en/v2/Git-Tools-Searching
Searching
With just about any size codebase, youll often need to find where a function is called or defined, or display the
 history of a method. Git provides a couple of useful tools for looking through the code and commits stored in its database quickly and easily.

Git Grep
Git ships with a command called grep that allows you to easily search through any committed tree, the working directory,
 or even the index for a string or regular expression. For the examples that follow, well search through the source code for Git itself.
By default, git grep will look through the files in your working directory. As a first variation, you can use either of the -n or --line-number
 options to print out the line numbers where Git has found matches:
$ git grep -n gmtime_r
Instead of printing all of the matches, you can ask git grep to summarize the output by showing you only which files contained the
 search string and how many matches there were in each file with the -c or --count option:
$ git grep --count gmtime_r
/If youre interested in the context of a search string, you can display the enclosing method or function for each matching
 string with either of the -p or --show-function options:
$ "git grep -p gmtime_r *.c"
You can also search for complex combinations of strings with the --and flag, which ensures that multiple matches must occur in the
 same line of text. For instance, lets look for any lines that define a constant whose name contains either of the
substrings 'LINK' or 'BUF_MAX', specifically in an older version of the Git codebase represented by the tag v1.8.0
 /well throw in the --break and --heading options which help split up the output into a more readable format/:
$ git grep --break --heading \
    -n -e '#define' --and \( -e LINK -e BUF_MAX \) v1.8.0
The git grep command has a few advantages over normal searching commands like grep and ack. The first is that its really fast,
 the second is that you can search through any tree in Git, not just the working directory. As we saw in the above example,
we looked for terms in an older version of the Git source code, not the version that was currently checked out.

Git Log Searching
Perhaps youre looking not for where a term exists, but when it existed or was introduced. The git log command has a number of powerful tools for
 finding specific commits by the content of their messages or even the content of the diff they introduce.
If- for example, we want to find out when the ZLIB_BUF_MAX constant was originally introduced, we can use the -S option
 /colloquially referred to as the Git 'pickaxe' option/ to tell Git to show us only those commits that
changed the number of occurrences of that string.
$ git log -S ZLIB_BUF_MAX --oneline
If- we look at the diff of those commits, we can see that in ef49a7a the constant was introduced and in e01503b it was modified.
If- you need to be more specific, you can provide a regular expression to search for with the -G option.

Line Log Search
Another fairly advanced log search that is insanely useful is the line history search. Simply run git log with the -L option,
 and it will show you the history of a function or line of code in your codebase.
For- example, if we wanted to see every change made to the function git_deflate_bound in the zlib.c file, we could run
 git log -L :git_deflate_bound:zlib.c. This will try to figure out what the bounds of that function are and then-
look through the history and show us every change that was made to the function as a series of patches back to when the function was first created.
$ git log -L :git_deflate_bound:zlib.c
If- Git cant figure out how to match a function or method in your programming language, you can also provide it with a
 regular expression /or regex/. For example, this would have done- the same thing as the example above:
git log -L '/unsigned long git_deflate_bound/','/^}/':zlib.c. You could also give it a range of lines or a
 single line number and youll get the same sort of output.

https://git-scm.com/book/en/v2/Git-Tools-Rewriting-History
Rewriting History
Many times, when working with Git, you may want to revise your local commit history. One of the great things about Git is that
 it allows you to make decisions at the last possible moment. You can decide what files go into which commits right before
you commit with the staging area, you can decide that you didnt mean to be working on something yet with
 git stash, and you can rewrite commits that already happened so they look like they happened in a different way.
This can involve changing the order of the commits, changing messages or modifying files in a commit, squashing together or
 splitting apart commits, or removing commits entirely — all before you share your work with others.
In this section, youll see how to accomplish these tasks so that you can make your commit history look the way you want before you share it with others.
Note- Dont push your work until youre happy with it
One of the cardinal rules of Git is that, since so much work is local within your clone, you have a great deal of freedom to
 rewrite your history locally. However, once you push your work, it is a different story entirely, and you should consider
pushed work as final unless you have good reason to change it. In short, you should avoid pushing your work until youre happy with it and
 ready to share it with the rest of the world.

Changing the Last Commit
Changing your most recent commit is probably the most common rewriting of history that youll do. Youll often want to do-
 two basic things to your last commit: simply change the commit message, or change the actual content of the commit by adding, removing and modifying files.
If- you simply want to modify your last commit message, thats easy:
$ git commit --amend
You need to be careful with this technique because amending changes the SHA-1 of the commit. Its like a very small
 rebase — dont amend your last commit if youve already pushed it.
Tip- An amended commit may /or may not/ need an amended commit message
When you amend a commit, you have the opportunity to change both the commit message and the content of the commit. If you
 amend the content of the commit substantially, you should almost certainly update the commit message to reflect that amended content.
On the other hand, if your amendments are suitably trivial /fixing a silly typo or adding a file you forgot to stage/ such that
 the earlier commit message is just fine, you can simply make the changes, stage them, and avoid the unnecessary editor session entirely with:
$ git commit --amend --no-edit

Changing Multiple Commit Messages
To modify a commit that is farther back in your history, you must move to more complex tools. Git doesnt have a
 modify-history tool, but you can use the rebase tool to rebase a series of commits onto the HEAD that they were
originally based on instead of moving them to another one. With the interactive rebase tool, you can then- stop after
 each commit you want to modify and change the message, add files, or do- whatever you wish. You can run rebase interactively by
adding the -i option to git rebase. You must indicate how far back you want to rewrite commits by telling the command which commit to rebase onto.
For- example, if you want to change the last three commit messages, or any of the commit messages in that group, you supply as an
 argument to git rebase -i the parent of the last commit you want to edit, which is HEAD~2^ or HEAD~3. It may be easier to
remember the /3 because youre trying to edit the last three commits, but keep in mind that youre actually designating
 four commits ago, the parent of the last commit you want to edit:
$ git rebase -i HEAD~3
Remember again that this is a rebasing command — every commit in the range HEAD~3..HEAD with a changed message and
 all of its descendants will be rewritten. Dont include any commit youve already pushed to a central server — doing so
will confuse other developers by providing an alternate version of the same change.
Running this command gives you a list of commits in your text editor that looks something like this:
pick f7f3f6d Change my name a bit
pick 310154e Update README formatting and add blame
pick a5f4a0d Add cat-file
Its important to note that these commits are listed in the opposite order than you normally see them using the log command.
 If- you run a log, you see something like this:
$ git log --pretty=format:"%h %s" HEAD~3..HEAD
a5f4a0d Add cat-file
310154e Update README formatting and add blame
f7f3f6d Change my name a bit
Notice the reverse order. The interactive rebase gives you a script that its going to run. It will start at the commit you
 specify on the command line HEAD~3 and replay the changes introduced in each of these commits from top to bottom. It lists the
oldest at the top, rather than the newest, because thats the first one it will replay.
You need to edit the script so that it stops at the commit you want to edit. To do- so, change the word 'pick' to the
 word 'edit' for each of the commits you want the script to stop after. For example, to modify only the third commit message,
you change the file to look like this:
edit f7f3f6d Change my name a bit
pick 310154e Update README formatting and add blame
pick a5f4a0d Add cat-file
When you save and exit the editor, Git rewinds you back to the last commit in that list and drops you on the command line with the following message:
$ git rebase -i HEAD~3
Stopped at f7f3f6d... Change my name a bit
You can amend the commit now, with
   git commit --amend
Once youre satisfied with your changes, run
   git rebase --continue
These instructions tell you exactly what to do. Type:
$ git commit --amend
Change the commit message, and exit the editor. Then, run:
$ git rebase --continue
This command will apply the other two commits automatically, and -then youre done. If you change pick to edit on more lines,
 you can repeat these steps for each commit you change to edit. Each time, Git will stop, let you amend the commit, and continue when youre finished.

Reordering Commits
You can also use interactive rebases to reorder or remove commits entirely. If you want to remove the 'Add cat-file' commit and
 change the order in which the other two commits are introduced, you can change the rebase script from this:
pick f7f3f6d Change my name a bit
pick 310154e Update README formatting and add blame
pick a5f4a0d Add cat-file
to this:
pick 310154e Update README formatting and add blame
pick f7f3f6d Change my name a bit
When you save and exit the editor, Git rewinds your branch to the parent of these commits, applies 310154e and then- f7f3f6d, and then- stops.
 You effectively change the order of those commits and remove the 'Add cat-file' commit completely.

Squashing Commits
Its also possible to take a series of commits and squash them down into a single commit with the interactive rebasing tool.
 The script puts helpful instructions in the rebase message:
If- instead of 'pick' or 'edit', you specify 'squash', Git applies both that change and the change directly before it and
 makes you merge the commit messages together. So, if you want to make a single commit from these three commits, you make the script look like this:
pick f7f3f6d Change my name a bit
squash 310154e Update README formatting and add blame
squash a5f4a0d Add cat-file
When you save and exit the editor, Git applies all three changes and -then puts you back into the editor to merge the three commit messages:
When you save that, you have a single commit that introduces the changes of all three previous commits.

Splitting a Commit
Splitting a commit undoes a commit and -then partially stages and commits as many times as commits you want to end up with. For example,
 suppose you want to split the middle commit of your three commits. Instead of 'Update README formatting and add blame', you want to
split it into two commits: 'Update README formatting' for the first, and 'Add blame' for the second. You can -do that in the
 rebase -i script by changing the instruction on the commit you want to split to 'edit':
pick f7f3f6d Change my name a bit
'edit' 310154e Update README formatting and add blame
pick a5f4a0d Add cat-file
Then- when the script drops you to the command line, you reset that commit, take the changes that have been reset, and
 create multiple commits out of them. When you save and exit the editor, Git rewinds to the parent of the first commit in your list,
applies the first commit f7f3f6d, applies the second 310154e, and drops you to the console. There, you can -do a
 mixed reset of that commit with git reset HEAD^, which effectively undoes that commit and leaves the modified files unstaged.
Now you can stage and commit files until you have several commits, and run git rebase --continue when youre done:
$ git reset HEAD^
$ git add README
$ git commit -m 'Update README formatting'
$ git add lib/simplegit.rb
$ git commit -m 'Add blame'
$ git rebase --continue
Git applies the last commit a5f4a0d in the script, and your history looks like this:
$ git log -4 --pretty=format:"%h %s"
1c002dd Add cat-file
9b29157 Add blame
35cfb2b Update README formatting
f7f3f6d Change my name a bit
This changes the SHA-1s of the three most recent commits in your list, so make sure no changed commit shows up in that
 list that youve already pushed to a shared repository. Notice that the last commit f7f3f6d in the list is unchanged.
Despite this commit being shown in the script, because it was marked as 'pick' and was applied prior to any rebase changes,
 Git leaves the commit unmodified.

Deleting a commit
If- you want to get rid of a commit, you can delete it using the rebase -i script. In the list of commits, put the
 word 'drop' before the commit you want to delete /or just delete that line from the rebase script/:
pick 461cb2a This commit is OK
'drop' 5aecc10 This commit is broken
Because of the way Git builds commit objects, deleting or altering a commit will cause the rewriting of all the commits that
 follow it. The further back in your repos history you go, the more commits will need to be recreated. This can cause lots of
merge conflicts if you have many commits later in the sequence that depend on the one you just deleted.
If- you get partway through a rebase like this and decide its not a good idea, you can always stop. Type
 git rebase --abort, and your repo will be returned to the state it was in before you started the rebase.
If- you finish a rebase and decide its not what you want, you can use git reflog to recover an earlier version of your branch.
 See Data Recovery for more information on the reflog command.
https://git-scm.com/book/en/v2/ch00/_data_recovery
Note- Drew DeVault made a practical hands-on guide with exercises to learn how to use git rebase.
 You can find it at: https://git-rebase.io/

The Nuclear Option: filter-branch
There is another history-rewriting option that you can use if you need to rewrite a larger number of commits in some
 scriptable way — for instance, changing your email address globally or removing a file from every commit. The
command is filter-branch, and it can rewrite huge swaths of your history, so you probably shouldnt use it unless your
 project isnt yet public and other people havent based work off the commits youre about to rewrite. However, it can be
very useful. Youll learn a few of the common uses so you can get an idea of some of the things its capable of.
Caution- git filter-branch has many pitfalls, and is no longer the recommended way to rewrite history. Instead, consider
 using git-filter-repo, which is a Python script that does a better job for most applications where you would normally
turn to filter-branch. Its documentation and source code can be found at https://github.com/newren/git-filter-repo.

Removing a File from Every Commit
This occurs fairly commonly. Someone accidentally commits a huge binary file with a thoughtless git add ., and you want to
 remove it everywhere. Perhaps you accidentally committed a file that contained a password, and you want to make your
project open source. filter-branch is the tool you probably want to use to scrub your entire history. To remove a
 file named passwords.txt from your entire history, you can use the --tree-filter option to filter-branch:
$ git filter-branch --tree-filter 'rm -f passwords.txt' HEAD
Rewrite 6b9b3cf04e7c5686a9cb838c3f36a8cb6a0fc2bd 21/21
Ref 'refs/heads/master' was rewritten
The --tree-filter option runs the specified command after each checkout of the project and -then recommits the results.
 In this case, you remove a file called passwords.txt from every snapshot, whether it exists or not. If you want to
remove all accidentally committed editor backup files, you can run something like
 git filter-branch --tree-filter 'rm -f */' HEAD.
Youll be able to watch Git rewriting trees and commits and -then move the branch pointer at the end. Its generally a
 good idea to -do this in a testing branch and -then hard-reset your master branch after youve determined the outcome is
what you really want. To run filter-branch on all your branches, you can pass --all to the command.

Making a Subdirectory the New Root
Suppose youve -done an import from another source control system and have subdirectories that make no sense /trunk, tags, and so on/.
 If- you want to make the trunk subdirectory be the new project root for every commit, filter-branch can help you -do that, too:
$ git filter-branch --subdirectory-filter trunk HEAD
Rewrite 856f0bf61e41a27326cdae8f09fe708d679f596f 12/12
Ref 'refs/heads/master' was rewritten
Now your new project root is what was in the trunk subdirectory each time. Git will also automatically remove commits that did not affect the subdirectory.

Changing Email Addresses Globally
Another common case is that you forgot to run git config to set your name and email address before you started working, or
 perhaps you want to open-source a project at work and change all your work email addresses to your personal address. In any case,
you can change email addresses in multiple commits in a batch with filter-branch as well. You need to be careful to
 change only the email addresses that are yours, so you use --commit-filter:
$ git filter-branch --commit-filter
#       'if [ "$GIT_AUTHOR_EMAIL" = "schacon@localhost" ];
#        then
#                GIT_AUTHOR_NAME="Scott Chacon";
#                GIT_AUTHOR_EMAIL="schacon@example.com";
#                git commit-tree "$@";
#        else
#                git commit-tree "$@";
#        fi'
         HEAD
This goes through and rewrites every commit to have your new address. Because commits contain the SHA-1 values of their parents,
 this command changes every commit SHA-1 in your history, not just those that have the matching email address.

https://git-scm.com/book/en/v2/Git-Tools-Reset-Demystified
Reset Demystified
Before moving on to more specialized tools, lets talk about the Git reset and checkout commands. These commands are two of the
 most confusing parts of Git when you first encounter them. They -do so many things that it seems hopeless to actually
understand them and employ them properly. For this, we recommend a simple metaphor.
The Three Trees
An easier way to think about reset and checkout is through the mental frame of Git being a content manager of three
 different trees. By 'tree' here, we really mean 'collection of files', not specifically the data structure.
There are a few cases where the index doesnt exactly act like a tree, but for our purposes it is easier to think about it this way for now.
Git as a system manages and manipulates three trees in its normal operation:
Tree	              Role
HEAD                Last commit snapshot, next parent
Index               Proposed next commit snapshot
Working Directory   Sandbox

The HEAD
HEAD is the pointer to the current branch reference, which is in turn a pointer to the last commit made on that branch.
 That means HEAD will be the parent of the next commit that is created. Its generally simplest to think of HEAD as the
snapshot of your last commit on that branch.
In fact, its pretty easy to see what that snapshot looks like. Here is an example of getting the actual directory listing and
 SHA-1 checksums for each file in the HEAD snapshot:
$ git cat-file -p HEAD
tree cfda3bf379e4f8dba8717dee55aab78aef7f4daf
author Scott Chacon  1301511835 -0700
committer Scott Chacon  1301511835 -0700
initial commit
$ git ls-tree -r HEAD
100644 blob a906cb2a4a904a152...   README
100644 blob 8f94139338f9404f2...   Rakefile
040000 tree 99f1a6d12cb4b6f19...   lib
The Git cat-file and ls-tree commands are 'plumbing' commands that are used for lower level things and not really used in
 day-to-day work, but they help us see whats going on here.

The Index
The index is your proposed next commit. Weve also been referring to this concept as Gits 'Staging Area' as this is what
 Git looks at when you run git commit.
Git populates this index with a list of all the file contents that were last checked out into your working directory and
 what they looked like when they were originally checked out. You -then replace some of those files with new versions of them,
and git commit converts that into the tree for a new commit.
$ git ls-files -s
100644 a906cb2a4a904a152e80877d4088654daad0c859 0	README
100644 8f94139338f9404f26296befa88755fc2598c289 0	Rakefile
100644 47c6340d6459e05787f644c2447d2595f5d3a54b 0	lib/simplegit.rb
Again- here were using git ls-files, which is more of a behind the scenes command that shows you what your index currently looks like.
The index is not technically a tree structure — its actually implemented as a flattened manifest — but for our purposes its close enough.

The Working Directory
Finally- you have your working directory /also commonly referred to as the 'working tree'/. The other two trees store their
 content in an efficient but inconvenient manner, inside the .git folder. The working directory unpacks them into actual files,
which makes it much easier for you to edit them. Think of the working directory as a sandbox, where you can try changes out before
 committing them to your staging area /index/ and -then to history.
$ tree
.
├── README
├── Rakefile
└── lib
    └── simplegit.rb
1 directory, 3 files

The Workflow
Gits typical workflow is to record snapshots of your project in successively better states, by manipulating these three trees.
HEAD                _-_ checkout to     _-_          Working Directory
Working Directory   _-_ stage files to  _-_``        Index
Index               _-_ commit to       _-_          HEAD
go into a new directory with a single file in it. Well call this v1 of the file. Now we run
 'git init' which will create a Git repository with a HEAD reference which points to the unborn master branch.
At this point, only the working directory tree has any content.
Now we want to commit this file, so we use
 'git add' to take content in the working directory and copy it to the index.
Then- we run
 'git commit' which takes the contents of the index and saves it as a permanent snapshot, creates a commit object which
points to that snapshot, and updates master to point to that commit.
If- we run
 'git status' well see no changes, because all three trees are the same.

Now we want to make a change to that file and commit it. Well go through the same process; first- we change the file in our
 working directory. Lets call this v2 of the file.
If- we run
 git status right now, well see the file as 'Changes not staged for commit', because that entry differs between the
index and the working directory. Next we run
 git add on it to stage it into our index.
At this point, if we run git status, we will see the file in green under 'Changes to be committed' because the
 index and HEAD differ — that is, our proposed next commit is now different from our last commit. Finally, we run
git commit to finalize the commit.
Now git status will give us no output, because all three trees are the same again.

Switching branches or cloning goes through a similar process. When you checkout a branch, it changes HEAD to point to the
 new branch ref, populates your index with the snapshot of that commit, then- copies the contents of the index into your working directory.

The Role of Reset
The reset command makes more sense when viewed in this context.

Step 1: Move HEAD
The first thing reset will do- is move what HEAD points to. This isnt the same as changing HEAD itself /which is what checkout does/;
 reset moves the branch that HEAD is pointing to. This means if- HEAD is set to the master branch /i.e. youre currently on the master branch/, running
git reset 9e5e6a4 will start by making master point to 9e5e6a4.
No matter what form of reset with a commit you invoke, this is the first thing it will always try to do. With reset --soft, it will simply stop there.
Now take a second to look and realize what happened: it essentially undid the last git commit command. When you run
 git commit, Git creates a new commit and moves the branch that HEAD points to up to it. When you reset back to HEAD~ /the parent of HEAD/,
you are moving the branch back to where it was, without changing the index or working directory. You could now update the index and run
 git commit again to accomplish what git commit --amend would have done- /see Changing the Last Commit/.

Step 2: Updating the Index /--mixed/
Note that if you run git status now youll see in green the difference between the index and what the new HEAD is.
The next thing reset will do- is to update the index with the contents of whatever snapshot HEAD now points to.
If- you specify the --mixed option, reset will stop at this point. This is also the default, so if you specify no option at all
 /just git reset HEAD~ in this case/, this is where the command will stop.
Now take another second to look and realize what happened: it still undid your last commit, but also unstaged everything.
 You rolled back to before you ran all your git add and git commit commands.

Step 3: Updating the Working Directory /--hard/
The third thing that reset will do- is to make the working directory look like the index. If you use the --hard option,
 it will continue to this stage.
So lets think about what just happened. You undid your last commit, the git add and git commit commands, and all the work you did in your working directory.
Its important to note that this flag /--hard/ is the only way to make the reset command dangerous, and one of the
 very few cases where Git will actually destroy data. Any other invocation of reset can be pretty easily undone, but
the --hard option cannot, since it forcibly overwrites files in the working directory. In this particular case, we still have the
 v3 version of our file in a commit in our Git DB, and we could get it back by looking at our reflog, but if we had not
committed it, Git still would have overwritten the file and it would be unrecoverable.

Recap
The reset command overwrites these three trees in a specific order, stopping when you tell it to:
Move the branch HEAD points to /stop here if --soft/.
Make the index look like HEAD /stop here unless --hard/.
Make the working directory look like the index.

Reset With a Path
That covers the behavior of reset in its basic form, but you can also provide it with a path to act upon. If you specify a path,
 reset will skip step 1, and limit the remainder of its actions to a specific file or set of files. This actually sort of
makes sense — HEAD is just a pointer, and you cant point to part of one commit and part of another. But the index and
 working directory can be partially updated, so reset proceeds with steps 2 and 3. So- assume we run
'git reset file.txt' This form /since you did not specify a commit SHA-1 or branch, and you didnt specify --soft or --hard/ is shorthand for
'git reset --mixed HEAD file.txt' which will:
Move the branch HEAD points to =skipped=.
Make the index look like HEAD =stop here=.
So it essentially just copies file.txt from HEAD to the index.
This has the practical effect of unstaging the file. If we look at what git add does, they are exact opposites.
This is why the output of the git status command suggests that you run this to unstage a file =see Unstaging a Staged File for more on this=.
We could just as easily not let Git assume we meant 'pull the data from HEAD' by specifying a specific commit to pull that
 file version from. We would just run something like
'git reset eb43bf file.txt'
This effectively does the same thing as if we had reverted the content of the file to v1 in the working directory, ran
 git add on it, then- reverted it back to v3 again =without actually going through all those steps=. If we run
git commit now, it will record a change that reverts that file back to v1, even though we never actually had it in our working directory again.
Its also interesting to note that like git add, the reset command will accept a --patch option to unstage content on a
 hunk-by-hunk basis. So you can selectively unstage or revert content.

Squashing
Lets look at how to do- something interesting with this newfound power — squashing commits.
#Say you have a series of commits with messages like “oops.”, “WIP” and “forgot this file”.
You can use reset to quickly and easily squash them into a single commit that makes you look really smart.
 Squashing Commits shows another way to do- this, but in this example its simpler to use reset.
Lets say you have a project where the first commit has one file, the second commit added a new file and changed the first, and
 the third commit changed the first file again. The second commit was a work in progress and you want to squash it down.
You can run
 'git reset --soft HEAD~2' to move the HEAD branch back to an older commit =the most recent commit you want to keep=:
And then- simply run
 'git commit' again:
Now you can see that your reachable history, the history you would push, now looks like you had one commit with file-a.txt v1, then- a
 second that both modified file-a.txt to v3 and added file-b.txt. The commit with the v2 version of the file is no longer in the history

Check It Out
Finally- you may wonder what the difference between checkout and reset is. Like reset, checkout manipulates the three trees, and
 it is a bit different depending on whether you give the command a file path or not.

Without Paths
Running 'git checkout [branch]' is pretty similar to running 'git reset --hard [branch]' in that it updates all three trees for you to
 look like [branch], but there are two important differences.
First- unlike reset --hard, checkout is working-directory safe; it will check to make sure its not blowing away files that
 have changes to them. Actually, its a bit smarter than that — it tries to do- a trivial merge in the working directory,
so all of the files you havent changed will be updated. reset --hard, on the other hand, will simply replace everything across the
 board without checking. The second important difference is how checkout updates HEAD. Whereas reset will move the
branch that HEAD points to, checkout will move HEAD itself to point to another branch.
For- instance, say we have master and develop branches which point at different commits, and
 were currently on develop /so HEAD points to it/. If- we run
 'git reset master' develop itself will now point to the same commit that master does. If we instead run
 'git checkout master' develop does not move, HEAD itself does. HEAD will now point to master. So, in
both cases were moving HEAD to point to commit A, but how we do- so is very different.
 'reset' will move the branch HEAD points to, 'checkout' moves HEAD itself.

With Paths
The other way to run checkout is with a file path, which, like reset, does not move HEAD. It is just like
 'git reset [branch] file' in that it updates the index with that file at that commit, but it also overwrites the file in the
working directory. It would be exactly like 'git reset --hard [branch] file' =if reset would let you run that= — is not
 working-directory safe, and it does not move HEAD.
Also- like 'git reset' and 'git add', 'checkout' will accept a --patch option to allow you to
 selectively revert file contents on a hunk-by-hunk basis.

Heres a cheat-sheet for which commands affect which trees.
 The HEAD column reads REF if that command moves the reference /branch/ that HEAD points to,
and HEAD if it moves HEAD itself. Pay especial attention to the 'WD Safe?' column — if it says NO, take a
 second to think before running that command.

Commit Level
reset --soft /commit/
HEAD	Index	Workdir	WD Safe?
REF   NO    NO      YES
reset /commit/
HEAD	Index	Workdir	WD Safe?
REF   YES   NO      YES
reset --hard /commit/
HEAD	Index	Workdir	WD Safe?
REF   YES   YES     NO
checkout /commit/
HEAD	Index	Workdir	WD Safe?
HEAD  YES   YES     YES

File Level
reset /commit/ =paths=
HEAD	Index	Workdir	WD Safe?
NO    YES   NO      YES
checkout /commit/ =paths=
HEAD	Index	Workdir	WD Safe?
NO    YES   YES     NO

https://git-scm.com/book/en/v2/Git-Tools-Advanced-Merging
Advanced Merging
Merging in Git is typically fairly easy. Since Git makes it easy to merge another branch multiple times, it means that
 you can have a very long lived branch but you can keep it up to date as you go, solving small conflicts often, rather than
be surprised by one enormous conflict at the end of the series.
However- sometimes tricky conflicts do- occur. Unlike some other version control systems, Git does not try to be
 overly clever about merge conflict resolution. Gits philosophy is to be smart about determining when a
merge resolution is unambiguous, but if there is a conflict, it does not try to be clever about automatically resolving it.
 Therefore- if you wait too long to merge two branches that diverge quickly, you can run into some issues.

Merge Conflicts
First of all, if at all possible, try to make sure your working directory is clean before doing a merge that may have conflicts. If
 you have work in progress, either commit it to a temporary branch or stash it. This makes it so that you can undo
anything you try here. If you have unsaved changes in your working directory when you try a merge, some of these tips may help you preserve that work.
$ git checkout -b whitespace
Switched to a new branch 'whitespace'
$ unix2dos hello.rb
unix2dos: converting file hello.rb to DOS format ...
$ git commit -am 'Convert hello.rb to DOS'
$ vim hello.rb
$ git diff -b
diff --git a/hello.rb b/hello.rb
index ac51efd..e85207e 100755
#--- a/hello.rb
#+++ b/hello.rb
@@ -1,7 +1,7 @@
 #! /usr/bin/env ruby
 def hello
#-  puts 'hello world'
#+  puts 'hello mundo'^M
 end
$ git commit -am 'Use Spanish instead of English'
Now we switch back to our master branch and add some documentation for the function.
$ git checkout master
Switched to branch 'master'
$ vim hello.rb
$ git diff
diff --git a/hello.rb b/hello.rb
index ac51efd..36c06c8 100755
#--- a/hello.rb
#+++ b/hello.rb
@@ -1,5 +1,6 @@
 #! /usr/bin/env ruby
#+# prints out a greeting
 def hello
   puts 'hello world'
 end
$ git commit -am 'Add comment documenting the function'
Now we try to merge in our whitespace branch and well get conflicts because of the whitespace changes.
$ git merge whitespace
Auto-merging hello.rb
Automatic merge failed; fix conflicts and then- commit the result.

Aborting a Merge
We now have a few options. First, lets cover how to get out of this situation. If you perhaps werent expecting conflicts and
 dont want to quite deal with the situation yet, you can simply back out of the merge with
'git merge --abort'
$ git status -sb
## master
UU hello.rb
$ git merge --abort
$ git status -sb
## master
The git merge --abort option tries to revert back to your state before you ran the merge. The only cases where it may not be
 able to do- this perfectly would be if you had unstashed, uncommitted changes in your working directory when you ran it,
otherwise it should work fine. If- for some reason you just want to start over, you can also run
 'git reset --hard HEAD' and your repository will be back to the last committed state. Remember that any uncommitted work
will be lost, so make sure you dont want any of your changes.

$ git merge -Xignore-space-change whitespace

Manual File Re-merging
Though Git handles whitespace pre-processing pretty well, there are other types of changes that perhaps Git cant handle automatically,
 but are scriptable fixes. As an example, lets pretend that Git could not handle the whitespace change and we needed to do- it by hand.
What we really need to do- is run the file were trying to merge in through a dos2unix program before trying the actual file merge. So how would we do- that?
First- we get into the merge conflict state. Then we want to get copies of our version of the file, their version =from the
 branch were merging in= and the common version =from where both sides branched off=. Then we want to
fix up either their side or our side and re-try the merge again for just this single file.
Getting the three file versions is actually pretty easy. Git stores all of these versions in the index under 'stages' which
 each have numbers associated with them. Stage 1 is the common ancestor, stage 2 is your version and stage 3 is from the MERGE_HEAD,
the version youre merging in =theirs=.
You can extract a copy of each of these versions of the conflicted file with the git show command and a special syntax.
$ 'git show :1:hello.rb' > hello.common.rb
$ git show :2:hello.rb > hello.ours.rb
$ git show :3:hello.rb > hello.theirs.rb
If- you want to get a little more hard core, you can also use the ls-files -u plumbing command to get the
 actual SHA-1s of the Git blobs for each of these files.
$ git ls-files -u
100755 ac51efdc3df4f4fd328d1a02ad05331d8e2c9111 1	hello.rb
100755 36c06c8752c78d2aff89571132f3bf7841a7b5c3 2	hello.rb
100755 e85207e04dfdd5eb0a1e9febbc67fd837c44a1cd 3	hello.rb
The :1:hello.rb is just a shorthand for looking up that blob SHA-1.
Now that we have the content of all three stages in our working directory, we can manually fix up theirs to fix the
 whitespace issue and re-merge the file with the little-known git merge-file command which does just that.
$ dos2unix hello.theirs.rb
dos2unix: converting file hello.theirs.rb to Unix format ...
$ 'git merge-file -p' \
    hello.ours.rb hello.common.rb hello.theirs.rb > hello.rb
$ 'git diff -b'
diff --cc hello.rb
index 36c06c8,e85207e..0000000
#--- a/hello.rb
+++ b/hello.rb
@@@ -1,8 -1,7 +1,8 @@@
  #! /usr/bin/env ruby
# +# prints out a greeting
  def hello
#-   puts 'hello world'
+   puts 'hello mundo'
  end
At this point we have nicely merged the file. In fact, this actually works better than the ignore-space-change option because this
 actually fixes the whitespace changes before merge instead of simply ignoring them. In the ignore-space-change merge, we
actually ended up with a few lines with DOS line endings, making things mixed.
If- you want to get an idea before finalizing this commit about what was actually changed between one side or the other,
 you can ask git diff to compare what is in your working directory that youre about to commit as the result of the merge to
any of these stages. Lets go through them all.
To compare your result to what you had in your branch before the merge, in other words, to see what the merge introduced, you can run git diff --ours:
 'git diff --ours'
#* Unmerged path hello.rb
diff --git a/hello.rb b/hello.rb
index 36c06c8..44d0a25 100755
#--- a/hello.rb
+++ b/hello.rb
@@ -2,7 +2,7 @@
 # prints out a greeting
 def hello
#-  puts 'hello world'
+  puts 'hello mundo'
 end
So here we can easily see that what happened in our branch, what were actually introducing to this file with this merge, is changing that single line.
If- we want to see how the result of the merge differed from what was on their side, you can run 'git diff --theirs'.
 In this and the following example, we have to use -b to strip out the whitespace because were comparing it to
what is in Git, not our cleaned up hello.theirs.rb file.
$ 'git diff --theirs -b'
#* Unmerged path hello.rb
diff --git a/hello.rb b/hello.rb
index e85207e..44d0a25 100755
#--- a/hello.rb
+++ b/hello.rb
@@ -1,5 +1,6 @@
 #! /usr/bin/env ruby
#+# prints out a greeting
 def hello
   puts 'hello mundo'
 end
Finally- you can see how the file has changed from both sides with 'git diff --base'.
$ 'git diff --base -b'
#* Unmerged path hello.rb
diff --git a/hello.rb b/hello.rb
index ac51efd..44d0a25 100755
#--- a/hello.rb
+++ b/hello.rb
@@ -1,7 +1,8 @@
 #! /usr/bin/env ruby
#+# prints out a greeting
 def hello
#-  puts 'hello world'
+  puts 'hello mundo'
 end
At this point we can use the 'git clean' command to clear out the extra files we created to do- the manual merge but no longer need.
$ 'git clean -f'
Removing hello.common.rb
Removing hello.ours.rb
Removing hello.theirs.rb

Checking Out Conflicts
Perhaps were not happy with the resolution at this point for some reason, or maybe manually editing one or both sides
 still didnt work well and we need more context.
One helpful tool is git checkout with the --conflict option. This will re-checkout the file again and replace the
 merge conflict markers. This can be useful if you want to reset the markers and try to resolve them again.
You can pass --conflict either diff3 or merge =which is the default=. If you pass it diff3, Git will use a
 slightly different version of conflict markers, not only giving you the =ours= and =theirs= versions, but also the =base=
version inline to give you more context.
$ 'git checkout --conflict=diff3 hello.rb'
If- you like this format, you can set it as the default for future merge conflicts by setting the merge.conflictstyle setting to diff3.
$ 'git config --global merge.conflictstyle diff3'
The git checkout command can also take --ours and --theirs options, which can be a really fast way of just choosing either
 one side or the other without merging things at all.
This can be particularly useful for conflicts of binary files where you can simply choose one side, or where you only want to
 merge certain files in from another branch — you can do- the merge and then- checkout certain files from one side or the other before committing.

Merge Log
To get a full list of all of the unique commits that were included in either branch involved in this merge,
 we can use the =triple dot= syntax that we learned in Triple Dot.
$ 'git log --oneline --left-right HEAD...MERGE_HEAD'
< f1270f7 Update README
< 9af9d3b Create README
< 694971d Update phrase to 'hola world'
> e3eb223 Add more tests
> 7cff591 Create initial testing script
> c3ffff1 Change text to 'hello mundo'
Thats a nice list of the six total commits involved, as well as which line of development each commit was on.
We can further simplify this though to give us much more specific context. If we add the --merge option to git log,
 it will only show the commits in either side of the merge that touch a file thats currently conflicted.
$ 'git log --oneline --left-right --merge'
< 694971d Update phrase to 'hola world'
> c3ffff1 Change text to 'hello mundo'
If- you run that with the -p option instead, you get just the diffs to the file that ended up in conflict. This can be
 really helpful in quickly giving you the context you need to help understand why something conflicts and how to more intelligently resolve it.

Combined Diff Format
Since Git stages any merge results that are successful, when you run git diff while in a conflicted merge state,
 you only get what is currently still in conflict. This can be helpful to see what you still have to resolve.
When you run git diff directly after a merge conflict, it will give you information in a rather unique diff output format.
You can also get this from the git log for any merge to see how something was resolved after the fact.
 Git will output this format if you run git show on a merge commit, or if you add a --cc option to a
git log -p =which by default only shows patches for non-merge commits=.
$ git log --cc -p -1

Undoing Merges
Now that you know how to create a merge commit, youll probably make some by mistake. One of the great things about
 working with Git is that its okay to make mistakes, because its possible =and in many cases easy= to fix them.

Fix the references
If- the unwanted merge commit only exists on your local repository, the easiest and best solution is to move the branches so that
 they point where you want them to. In most cases, if you follow the errant git merge with 'git reset --hard HEAD~', this will
reset the branch pointers: 'reset --hard' usually goes through three steps:
Move the branch HEAD points to. In this case, we want to move master to where it was before the merge commit =C6=.
Make the index look like HEAD.
Make the working directory look like the index.
The downside of this approach is that its rewriting history, which can be problematic with a shared repository.

Reverse the commit
If- moving the branch pointers around isnt going to work for you, Git gives you the option of making a new commit which
 undoes all the changes from an existing one. Git calls this operation a 'revert', and in this particular scenario, youd invoke it like this:
$ git revert -m 1 HEAD
The -m 1 flag indicates which parent is the =mainline= and should be kept. When you invoke a merge into HEAD 'git merge topic', the
 new commit has two parents: the first one is HEAD =C6=, and the second is the tip of the branch being merged in =C4=.
In this case, we want to undo all the changes introduced by merging in parent #2 (C4), while keeping all the content from parent #1 (C6).
The new commit ^M has exactly the same contents as C6, so starting from here its as if the merge never happened, except that
 the now-unmerged commits are still in HEADs history. Git will get confused if you try to merge topic into master again:
$ git merge topic
Already up-to-date.
Theres nothing in topic that isnt already reachable from master. Whats worse, if you add work to topic and merge again,
 Git will only bring in the changes since the reverted merge:
The best way around this is to un-revert the original merge, since now you want to bring in the changes that
 were reverted out, then- create a new merge commit:
$ git revert ^M
#[master 09f0126] Revert "Revert "Merge branch 'topic'""
$ git merge topic
In this example, M and ^M cancel out. ^^M effectively merges in the changes from C3 and C4, and C8 merges in the
 changes from C7, so now topic is fully merged.

Our or Theirs Preference
$ git merge -Xours mundo
This option can also be passed to the git merge-file command we saw earlier by running something like git merge-file --ours for individual file merges.
If- you want to do- something like this but not have Git even try to merge changes from the other side in, there is a
 more draconian option, which is the =ours= merge strategy. This is different from the =ours= recursive merge option.
This will basically do- a fake merge. It will record a new merge commit with both branches as parents, but it will not
 even look at the branch youre merging in. It will simply record as the result of the merge the exact code in your current branch.
$ git merge -s ours mundo
Merge made by the 'ours' strategy.
$ git diff HEAD HEAD~
$
You can see that there is no difference between the branch we were on and the result of the merge.
This can often be useful to basically trick Git into thinking that a branch is already merged when doing a
 merge later on. For example, say you branched off a release branch and have done- some work on it that you will want to
merge back into your master branch at some point. In the meantime some bugfix on master needs to be backported into your
 release branch. You can merge the bugfix branch into the release branch and also merge -s ours the same branch into your
master branch =even though the fix is already there= so when you later merge the release branch again, there are no conflicts from the bugfix.

Subtree Merging
The idea of the subtree merge is that you have two projects, and one of the projects maps to a subdirectory of the
 other one. When you specify a subtree merge, Git is often smart enough to figure out that one is a subtree of the other and merge appropriately.
First- well add the Rack application to our project. Well add the Rack project as a
 remote reference in our own project and then- check it out into its own branch:
$ 'git remote add rack_remote https://github.com/rack/rack'
$ 'git fetch rack_remote --no-tags'
$ 'git checkout -b rack_branch rack_remote/master'
Now we have the root of the Rack project in our rack_branch branch and our own project in the master branch. If you
 check out one and then- the other, you can see that they have different project roots:
This is sort of a strange concept. Not all the branches in your repository actually have to be branches of the same project.
 Its not common, because its rarely helpful, but its fairly easy to have branches contain completely different histories.
In this case, we want to pull the Rack project into our master project as a subdirectory. We can do- that in Git with
 git read-tree. Youll learn more about read-tree and its friends in Git Internals, but for now know that it reads the
root tree of one branch into your current staging area and working directory. We just switched back to your master branch,
 and we pull the rack_branch branch into the rack subdirectory of our master branch of our main project:
$ 'git read-tree --prefix=rack/ -u rack_branch'
When we commit, it looks like we have all the Rack files under that subdirectory — as though we copied them in from a tarball.
 What gets interesting is that we can fairly easily merge changes from one of the branches to the other. So, if the
Rack project updates, we can pull in upstream changes by switching to that branch and pulling:
$ git checkout rack_branch
$ git pull
Then- we can merge those changes back into our master branch. To pull in the changes and prepopulate the commit message,
 use the --squash option, as well as the recursive merge strategys -Xsubtree option.
The recursive strategy is the default here, but we include it for clarity.
$ git checkout master
$ 'git merge --squash -s recursive -Xsubtree=rack rack_branch'
Squash commit -- not updating HEAD
Automatic merge went well; stopped before committing as requested
All the changes from the Rack project are merged in and ready to be committed locally. You can also do- the
 opposite — make changes in the rack subdirectory of your master branch and then- merge them into your rack_branch branch later to
submit them to the maintainers or push them upstream.
This gives us a way to have a workflow somewhat similar to the submodule workflow without using submodules.
 We can keep branches with other related projects in our repository and subtree merge them into our project occasionally.
It is nice in some ways, for example all the code is committed to a single place. However, it has other drawbacks in that its a
 bit more complex and easier to make mistakes in reintegrating changes or accidentally pushing a branch into an unrelated repository.
Another slightly weird thing is that to get a diff between what you have in your rack subdirectory and the code in
 your rack_branch branch — to see if you need to merge them — you cant use the normal diff command. Instead, you
must run git diff-tree with the branch you want to compare to:
$ 'git diff-tree -p rack_branch'
Or- to compare what is in your rack subdirectory with what the master branch on the server was the last time you fetched, you can run:
$ 'git diff-tree -p rack_remote/master'

https://git-scm.com/book/en/v2/Git-Tools-Rerere
Rerere
The git rerere functionality is a bit of a hidden feature. The name stands for 'reuse recorded resolution' and, as the
 name implies, it allows you to ask Git to remember how youve resolved a hunk conflict so that the next time it sees the
same conflict, Git can resolve it for you automatically.
With rerere enabled, you can attempt the occasional merge, resolve the conflicts, then- back out of the merge.
 If- you do- this continuously, then- the final merge should be easy because rerere can just do- everything for you automatically.
This same tactic can be used if you want to keep a branch rebased so you dont have to deal with the same rebasing conflicts each time you do- it.
 Or if you want to take a branch that you merged and fixed a bunch of conflicts and then- decide to rebase it
instead — you likely wont have to do- all the same conflicts again.
Another application of rerere is where you merge a bunch of evolving topic branches together into a testable head occasionally, as the
 Git project itself often does. If the tests fail, you can rewind the merges and re-do them without the topic branch that made the
tests fail without having to re-resolve the conflicts again.
To enable rerere functionality, you simply have to run this config setting:
$ 'git config --global rerere.enabled true'
You can also turn it on by creating the '.git/rr-cache' directory in a specific repository, but the config setting is
 clearer and enables that feature globally for you.
$ 'git rerere status'
$ 'git rerere diff'
Also /and this isnt really related to rerere/, you can use git ls-files -u to see the conflicted files and the before, left and right versions:
$ 'git ls-files -u'
$ 'git rerere'
So- if you do- a lot of re-merges, or want to keep a topic branch up to date with your master branch without a ton of merges,
 or you rebase often, you can turn on rerere to help your life out a bit.

https://git-scm.com/book/en/v2/Git-Tools-Debugging-with-Git
Debugging with Git
In addition to being primarily for version control, Git also provides a couple commands to help you debug your source code projects.
 Because Git is designed to handle nearly any type of content, these tools are pretty generic, but they can often help you hunt for a
bug or culprit when things go wrong.

File Annotation
If- you track down a bug in your code and want to know when it was introduced and why, file annotation is often your best tool.
 It shows you what commit was the last to modify each line of any file. So if you see that a method in your code is buggy,
you can annotate the file with git blame to determine which commit was responsible for the introduction of that line
$ 'git blame -L' 69,82 Makefile
Another cool thing about Git is that it doesnt track file renames explicitly. It records the snapshots and then- tries to
 figure out what was renamed implicitly, after the fact. One of the interesting features of this is that you can ask it to
figure out all sorts of code movement as well. If you pass -C to git blame, Git analyzes the file youre annotating and
 tries to figure out where snippets of code within it originally came from if they were copied from elsewhere
$ 'git blame -C -L' 141,153 GITPackUpload.m

Binary Search
Annotating a file helps if you know where the issue is to begin with. If you dont know what is breaking, and there have
 been dozens or hundreds of commits since the last state where you know the code worked, youll likely turn to
git bisect for help. The bisect command does a binary search through your commit history to help you identify as quickly as
 possible which commit introduced an issue.
Lets say you just pushed out a release of your code to a production environment, youre getting bug reports about something that
 wasnt happening in your development environment, and you cant imagine why the code is doing that. You go back to your code, and
it turns out you can reproduce the issue, but you cant figure out what is going wrong. You can bisect the code to find out.
 First you run git bisect start to get things going, and then- you use git bisect bad to tell the system that the
current commit youre on is broken. Then, you must tell bisect when the last known good state was, using git bisect good <good_commit>:
$ 'git bisect start'
$ 'git bisect bad'
$ 'git bisect good v1.0'
#Bisecting: 6 revisions left to test after this
Git figured out that about 12 commits came between the commit you marked as the last good commit /v1.0/ and the current bad version,
 and it checked out the middle one for you. At this point, you can run your test to see if the issue exists as of this commit. If it does,
then- it was introduced sometime before this middle commit. if it doesnt, then- the problem was introduced sometime after the
 middle commit. It turns out there is no issue here, and you tell Git that by typing git bisect good and continue your journey:
$ git bisect good
#Bisecting: 3 revisions left to test after this
Now youre on another commit, halfway between the one you just tested and your bad commit. You run your test again and
 find that this commit is broken, so you tell Git that with git bisect bad:
$ git bisect bad
#Bisecting: 1 revisions left to test after this
This commit is fine, and now Git has all the information it needs to determine where the issue was introduced. It tells you the
 SHA-1 of the first bad commit and show some of the commit information and which files were modified in that commit so you can
figure out what happened that may have introduced this bug:
$ git bisect good
#b047b02ea83310a70fd603dc8cd7a6cd13d15c04 is first bad commit
When youre finished, you should run git bisect reset to reset your HEAD to where you were before you started, or youll end up in a weird state:
$ 'git bisect reset'
This is a powerful tool that can help you check hundreds of commits for an introduced bug in minutes. In fact, if you have a
 script that will exit 0 if the project is good or non-0 if the project is bad, you can fully automate git bisect. First,
you again tell it the scope of the bisect by providing the known bad and good commits. You can do- this by listing them with the
 bisect start command if you want, listing the known bad commit first and the known good commit second:
$ 'git bisect start HEAD v1.0'
$ 'git bisect run test-error.sh'
Doing so automatically runs test-error.sh on each checked-out commit until Git finds the first broken commit.
 You can also run something like make or make tests or whatever you have that runs automated tests for you.

https://git-scm.com/book/en/v2/Git-Tools-Submodules
Submodules
It often happens that while working on one project, you need to use another project from within it. Perhaps its a
 library that a third party developed or that youre developing separately and using in multiple parent projects.
A common issue arises in these scenarios: you want to be able to treat the two projects as separate yet
 still be able to use one from within the other.
Submodules allow you to keep a Git repository as a subdirectory of another Git repository. This lets you
 clone another repository into your project and keep your commits separate.

$ git submodule add https://github.com/chaconinc/DbConnector
By default, submodules will add the subproject into a directory named the same as the repository, in this case =DbConnector=.
 You can add a different path at the end of the command if you want it to go elsewhere.
$ git status
First you should notice the new '.gitmodules' file. This is a configuration file that stores the mapping between the
 projects URL and the local subdirectory youve pulled it into:
#[submodule "DbConnector"]
#	path = DbConnector
#	url = https://github.com/chaconinc/DbConnector
If- you have multiple submodules, youll have multiple entries in this file. Its important to note that this file is
 version-controlled with your other files, like your '.gitignore' file. Its pushed and pulled with the rest of your project.
This is how other people who clone this project know where to get the submodule projects from.
Note- Since the URL in the '.gitmodules' file is what other people will first try to clone/fetch from, make sure to use a
 URL that they can access if possible. For example, if you use a different URL to push to than others would to pull from,
use the one that others have access to. You can overwrite this value locally with
 'git config submodule.DbConnector.url PRIVATE_URL' for your own use. When applicable, a relative URL can be helpful.
The other listing in the git status output is the project folder entry. If you run git diff on that, you see something interesting:
$ 'git diff --cached' DbConnector
Although DbConnector is a subdirectory in your working directory, Git sees it as a submodule and doesnt track its
 contents when youre not in that directory. Instead, Git sees it as a particular commit from that repository.
If- you want a little nicer diff output, you can pass the --submodule option to git diff.
$ 'git diff --cached --submodule'
+[submodule "DbConnector"]
+       path = DbConnector
+       url = https://github.com/chaconinc/DbConnector
#Submodule DbConnector 0000000...c3f01dc /new submodule/
When you commit, you see something like this:
$ 'git commit -am' Add DbConnector module
#[master fb9093c] Add DbConnector module
# create mode 100644 .gitmodules
# create mode 160000 DbConnector
Notice the 160000 mode for the DbConnector entry. That is a special mode in Git that basically means youre recording a
 commit as a directory entry rather than a subdirectory or a file.
Lastly- push these changes:
$ 'git push origin master'

Cloning a Project with Submodules
Here well clone a project with a submodule in it. When you clone such a project, by default you get the directories that
 contain submodules, but none of the files within them yet:
$ 'git clone' https://github.com/chaconinc/MainProject
$ cd MainProject
$ ls -la
#total 16
$ cd DbConnector/
$ ls
#$
The DbConnector directory is there, but empty. You must run two commands:
 'git submodule init' to initialize your local configuration file, and
  'git submodule update' to fetch all the data from that project and check out the appropriate commit listed in your superproject:
$ git submodule init
#Submodule 'DbConnector' (https://github.com/chaconinc/DbConnector) registered for path 'DbConnector'
$ git submodule update
Cloning into 'DbConnector'...
Now your DbConnector subdirectory is at the exact state it was in when you committed earlier.
There is another way to do- this which is a little simpler, however. If you pass --recurse-submodules to the git clone command,
 it will automatically initialize and update each submodule in the repository, including nested submodules if any of the
submodules in the repository have submodules themselves.
$ 'git clone --recurse-submodules' https://github.com/chaconinc/MainProject
If- you already cloned the project and forgot --recurse-submodules, you can combine the
 git submodule init and git submodule update steps by running 'git submodule update --init'. To also initialize,
fetch and checkout any nested submodules, you can use the foolproof 'git submodule update --init --recursive'.

Pulling in Upstream Changes from the Submodule Remote
The simplest model of using submodules in a project would be if you were simply consuming a subproject and wanted to
 get updates from it from time to time but were not actually modifying anything in your checkout.
If- you want to check for new work in a submodule, you can go into the directory and run git fetch and git merge the upstream branch to update the local code.
$ git fetch
Now if you go back into the main project and run git diff --submodule you can see that the submodule was updated and get a
 list of commits that were added to it. If you dont want to type --submodule every time you run git diff, you can set it as the
default format by setting the diff.submodule config value to =log=.
$ 'git config --global diff.submodule log'
$ git diff
There is an easier way to do- this as well, if you prefer to not manually fetch and merge in the subdirectory. If you run
 git submodule update --remote, Git will go into your submodules and fetch and update for you.
$ 'git submodule update --remote' DbConnector
This command will by default assume that you want to update the checkout to the default branch of the
 remote submodule repository =the one pointed to by HEAD on the remote=. You can, however, set this to something
different if you want. For example, if you want to have the DbConnector submodule track that repositorys =stable= branch,
 you can set it in either your .gitmodules file =so everyone else also tracks it=, or just in your local .git/config file.
Lets set it in the .gitmodules file:
$ 'git config -f .gitmodules submodule.DbConnector.branch stable'
If- you leave off the -f .gitmodules it will only make the change for you.
$ git submodule update --remote
If- you set the configuration setting status.submodulesummary, Git will also show you a short summary of changes to your submodules:
$ 'git config status.submodulesummary 1'
$ git status
At this point if you run git diff we can see both that we have modified our .gitmodules file and also that there are a
 number of commits that weve pulled down and are ready to commit to our submodule project.
$ git diff
Once committed, you can see this information after the fact as well when you run git log -p.
$ 'git log -p --submodule'
Git will by default try to update all of your submodules when you run git submodule update --remote. If you have a lot of them,
 you may want to pass the name of just the submodule you want to try to update.

Pulling Upstream Changes from the Project Remote
Lets now step into the shoes of your collaborator, who has their own local clone of the MainProject repository.
 Simply executing git pull to get your newly committed changes is not enough:
$ git pull
By default, the git pull command recursively fetches submodules changes, as we can see in the output of the first command above.
 However- it does not update the submodules. This is shown by the output of the git status command, which shows the
submodule is =modified=, and has =new commits=.
 To finalize the update, you need to run git submodule update:
$ 'git submodule update --init --recursive'
Note that to be on the safe side, you should run git submodule update with the --init flag in case the MainProject commits you
 just pulled added new submodules, and with the --recursive flag if any submodules have nested submodules.
If- you want to automate this process, you can add the --recurse-submodules flag to the git pull command =since Git 2.14=.
 This will make Git run git submodule update right after the pull, putting the submodules in the correct state.
Moreover- if you want to make Git always pull with --recurse-submodules, you can set the configuration option
 submodule.recurse to true =this works for git pull since Git 2.15=. This option will make Git use the --recurse-submodules
flag for all commands that support it =except clone=. There is a special situation that can happen when pulling superproject updates:
 it could be that the upstream repository has changed the URL of the submodule in the .gitmodules file in one of the
commits you pull. This can happen for example if the submodule project changes its hosting platform. In that case,
 it is possible for git pull --recurse-submodules, or git submodule update, to fail if the superproject references a
submodule commit that is not found in the submodule remote locally configured in your repository.
In order to remedy this situation, the git submodule sync command is required:
# copy the new URL to your local config
$ 'git submodule sync --recursive'
# update the submodule from the new URL
$ git submodule update --init --recursive

Working on a Submodule
Its quite likely that if youre using submodules, youre doing so because you really want to work on the code in the
 submodule at the same time as youre working on the code in the main project =or across several submodules=.
Otherwise you would probably instead be using a simpler dependency management system =such as Maven or Rubygems=.
So far, when weve run the git submodule update command to fetch changes from the submodule repositories, Git would get the
 changes and update the files in the subdirectory but will leave the sub-repository in whats called a =detached HEAD= state.
This means that there is no local working branch =like master, for example= tracking changes. With no working branch tracking changes,
 that means even if you commit changes to the submodule, those changes will quite possibly be lost the next time you run
git submodule update. You have to do- some extra steps if you want changes in a submodule to be tracked.
In order to set up your submodule to be easier to go in and hack on, you need to do- two things. You need to go into each submodule and
 check out a branch to work on. Then you need to tell Git what to do- if you have made changes and later
git submodule update --remote pulls in new work from upstream. The options are that you can merge them into your local work,
 or you can try to rebase your local work on top of the new changes.
First of all, lets go into our submodule directory and check out a branch.
$ cd DbConnector/
$ 'git checkout' stable
#Switched to branch 'stable'
Lets try updating our submodule with the =merge= option. To specify it manually, we can just add the --merge option to
 our update call. Here well see that there was a change on the server for this submodule and it gets merged in.
$ cd ..
$ 'git submodule update --remote --merge'
If- we go into the DbConnector directory, we have the new changes already merged into our local stable branch.
 Now lets see what happens when we make our own local change to the library and someone else pushes another change to the upstream at the same time.
$ cd DbConnector/
$ vim src/db.c
$ 'git commit -am' 'Unicode support'
Now if we update our submodule we can see what happens when we have made a local change and upstream also has a change we need to incorporate.
$ cd ..
$ 'git submodule update --remote --rebase'
#First- rewinding head to replay your work on top of it...
#Applying: Unicode support
#Submodule path 'DbConnector': rebased into '5d60ef9bbebf5a0c1c1050f242ceeb54ad58da94'
If- you forget the --rebase or --merge, Git will just update the submodule to whatever is on the server and reset your project to a detached HEAD state.
$ 'git submodule update --remote'
#Submodule path 'DbConnector': checked out '5d60ef9bbebf5a0c1c1050f242ceeb54ad58da94'
If- this happens, dont worry, you can simply go back into the directory and check out your branch again =which will still contain your work= and
 merge or rebase origin/stable =or whatever remote branch you want= manually.
If- you havent committed your changes in your submodule and you run a submodule update that would cause issues,
 Git will fetch the changes but not overwrite unsaved work in your submodule directory.
$ git submodule update --remote
If- you made changes that conflict with something changed upstream, Git will let you know when you run the update.
$ 'git submodule update --remote --merge'
#Auto-merging scripts/setup.sh
#CONFLICT (content): Merge conflict in scripts/setup.sh
#Recorded preimage for 'scripts/setup.sh'
#Automatic merge failed; fix conflicts and then- commit the result.
#Unable to merge 'c75e92a2b3855c9e5b66f915308390d9db204aca' in submodule path 'DbConnector'
You can go into the submodule directory and fix the conflict just as you normally would.

Publishing Submodule Changes
Now we have some changes in our submodule directory. Some of these were brought in from upstream by our updates and
 others were made locally and arent available to anyone else yet as we havent pushed them yet.
If- we commit in the main project and push it up without pushing the submodule changes up as well, other people who try to
 check out our changes are going to be in trouble since they will have no way to get the submodule changes that are depended on.
Those changes will only exist on our local copy.
In order to make sure this doesnt happen, you can ask Git to check that all your submodules have been pushed properly before
 pushing the main project. The git push command takes the --recurse-submodules argument which can be set to either =check= or =on-demand=.
The /check/ option will make push simply fail if any of the committed submodule changes havent been pushed.
$ 'git push --recurse-submodules=check'
As you can see, it also gives us some helpful advice on what we might want to do- next. The simple option is to go into
 each submodule and manually push to the remotes to make sure theyre externally available and then- try this push again.
If- you want the =check= behavior to happen for all pushes, you can make this behavior the default by doing git config push.recurseSubmodules check.
The other option is to use the =on-demand= value, which will try to do- this for you.
$ 'git push --recurse-submodules=on-demand'
As you can see there, Git went into the DbConnector module and pushed it before pushing the main project. If that
 submodule push fails for some reason, the main project push will also fail. You can make this behavior the default by
doing git config push.recurseSubmodules on-demand.

Merging Submodule Changes
If- you change a submodule reference at the same time as someone else, you may run into some problems. That is, if the
 submodule histories have diverged and are committed to diverging branches in a superproject, it may take a bit of work for you to fix.
If- one of the commits is a direct ancestor of the other =a fast-forward merge=, then- Git will simply choose the
 latter for the merge, so that works fine.
Git will not attempt even a trivial merge for you.
$ 'git pull'
#From https://github.com/chaconinc/MainProject
#   9a377d1..eb974f8  master     -> origin/master
#Fetching submodule DbConnector
#warning: Failed to merge submodule DbConnector /merge following commits not found/
#Auto-merging DbConnector
#CONFLICT (submodule): Merge conflict in DbConnector
#Automatic merge failed; fix conflicts and then- commit the result.
So basically what has happened here is that Git has figured out that the two branches record points in the
 submodules history that are divergent and need to be merged. It explains it as =merge following commits not found=, which is
confusing but well explain why that is in a bit.
To solve the problem, you need to figure out what state the submodule should be in. Strangely, Git doesnt really give you
 much information to help out here, not even the SHA-1s of the commits of both sides of the history. Fortunately, its simple to
figure out. If you run git diff you can get the SHA-1s of the commits recorded in both branches you were trying to merge.
$ 'git diff'
#diff --cc DbConnector
#index eb41d76,c771610..0000000
So- in this case, eb41d76 is the commit in our submodule that we had and c771610 is the commit that upstream had. If we go into
 our submodule directory, it should already be on eb41d76 as the merge would not have touched it. If for whatever reason its not,
you can simply create and checkout a branch pointing to it.
What is important is the SHA-1 of the commit from the other side. This is what youll have to merge in and resolve.
 You can either just try the merge with the SHA-1 directly, or you can create a branch for it and then- try to merge that in.
We would suggest the latter, even if only to make a nicer merge commit message.
So- we will go into our submodule directory, create a branch named =try-merge= based on that second SHA-1 from git diff, and manually merge.
$ cd DbConnector
$ 'git rev-parse HEAD'
#eb41d764bccf88be77aced643c13a7fa86714135
$ 'git branch' try-merge c771610
$ 'git merge' try-merge
#Auto-merging src/main.c
#Automatic merge failed; fix conflicts and then- commit the result.
We got an actual merge conflict here, so if we resolve that and commit it, then- we can simply update the main project with the result.
1 First we resolve the conflict.
#$ vim src/main.c
#$ git add src/main.c
#$ git commit -am 'merged our changes'
2 Then we go back to the main project directory.
#$ cd ..
3 We can check the SHA-1s again.
$ 'git diff'
#diff --cc DbConnector
#index eb41d76,c771610..0000000
4 Resolve the conflicted submodule entry.
$ 'git add' DbConnector
5 Commit our merge.
$ 'git commit -m' "Merge Tom's Changes"
It can be a bit confusing, but its really not very hard.
Interestingly- there is another case that Git handles. If a merge commit exists in the submodule directory that
 contains both commits in its history, Git will suggest it to you as a possible solution. It sees that at some point in the
submodule project, someone merged branches containing these two commits, so maybe youll want that one.
This is why the error message from before was =merge following commits not found=, because it could not do- this.
 Its confusing because who would expect it to try to do- this?
If- it does find a single acceptable merge commit, youll see something like this:
$ git merge origin/master
#warning: Failed to merge submodule DbConnector (not fast-forward)
#Found a possible merge resolution for the submodule:
# 9fd905e5d7f45a0d4cbc43d1ee550f16a30e825a: > merged our changes
If- this is correct simply add it to the index for example
by using:
  'git update-index --cacheinfo' 160000 9fd905e5d7f45a0d4cbc43d1ee550f16a30e825a "DbConnector"
which will accept this suggestion.
Auto-merging DbConnector
#CONFLICT (submodule): Merge conflict in DbConnector
#Automatic merge failed; fix conflicts and then- commit the result.
The suggested command Git is providing will update the index as though you had run git add =which clears the conflict=, then- commit.
 You probably shouldnt do- this though. You can just as easily go into the submodule directory, see what the difference is,
fast-forward to this commit, test it properly, and then- commit it.
# cd DbConnector/
$ 'git merge' 9fd905e
#Updating eb41d76..9fd905e
#Fast-forward
$ cd ..
$ 'git add' DbConnector
$ 'git commit -am' 'Fast forward to a common submodule child'
This accomplishes the same thing, but at least this way you can verify that it works and
 you have the code in your submodule directory when youre done.

Submodule Foreach
There is a foreach submodule command to run some arbitrary command in each submodule. This can be really helpful if you have a
 number of submodules in the same project.
For- example, lets say we want to start a new feature or do- a bugfix and we have work going on in several submodules.
 We can easily stash all the work in all our submodules.
$ 'git submodule foreach' 'git stash'
Then- we can create a new branch and switch to it in all our submodules.
$ git submodule foreach 'git checkout -b featureA'
#Entering 'CryptoLibrary'
#Switched to a new branch 'featureA'
#Entering 'DbConnector'
#Switched to a new branch 'featureA'
You get the idea. One really useful thing you can do- is produce a nice unified diff of what is changed in your main project and all your subprojects as well.
$ 'git diff'; 'git submodule foreach' 'git diff'

Useful Aliases
You may want to set up some aliases for some of these commands as they can be quite long and you cant set configuration options for
 most of them to make them defaults. We covered setting up Git aliases in Git Aliases, but here is an example of what
you may want to set up if you plan on working with submodules in Git a lot.
$ git config alias.sdiff '!'"git diff && git submodule foreach 'git diff'"
$ git config alias.spush 'push --recurse-submodules=on-demand'
$ 'git config alias.supdate' 'submodule update --remote --merge'
This way you can simply run git supdate when you want to update your submodules, or git spush to push with submodule dependency checking.

Switching branches
For- instance, switching branches with submodules in them can also be tricky with Git versions older than Git 2.13.
 If- you create a new branch, add a submodule there, and then- switch back to a branch without that submodule, you still have the
submodule directory as an untracked directory:
$ 'git --version'
#git version 2.12.2
$ 'git checkout -b' add-crypto
#Switched to a new branch 'add-crypto'
$ 'git submodule add' https://github.com/chaconinc/CryptoLibrary
#Cloning into 'CryptoLibrary'...
$ git commit -am 'Add crypto library'
#[add-crypto 4445836] Add crypto library
# create mode 160000 CryptoLibrary
$ 'git checkout' master
#warning: unable to rmdir CryptoLibrary: Directory not empty
#Switched to branch 'master'
#Your branch is up-to-date with 'origin/master'.
$ 'git status'
#On branch master
#Your branch is up-to-date with 'origin/master'.
#Untracked files:
#  (use "git add <file>..." to include in what will be committed)
#	CryptoLibrary/
#nothing added to commit but untracked files present (use "git add" to track)
Removing the directory isnt difficult, but it can be a bit confusing to have that in there. If you do- remove it
 and then- switch back to the branch that has that submodule, you will need to run submodule update --init to repopulate it.
$ 'git clean -ffdx'
#Removing CryptoLibrary/
$ 'git checkout' add-crypto
#Switched to branch 'add-crypto'
$ ls CryptoLibrary/
$ 'git submodule update --init'
#Submodule path 'CryptoLibrary': checked out 'b8dda6aa182ea4464f3f3264b11e0268545172af'
$ ls CryptoLibrary/
#Makefile	includes	scripts		src
Again- not really very difficult, but it can be a little confusing.
Newer Git versions /Git >= 2.13/ simplify all this by adding the --recurse-submodules flag to the git checkout command,
 which takes care of placing the submodules in the right state for the branch we are switching to.
$ 'git --version'
#git version 2.13.3
$ 'git checkout -b' add-crypto
#Switched to a new branch 'add-crypto'
$ 'git submodule add' https://github.com/chaconinc/CryptoLibrary
#Cloning into 'CryptoLibrary'...
$ 'git commit -am' 'Add crypto library'
#[add-crypto 4445836] Add crypto library
# create mode 160000 CryptoLibrary
$ 'git checkout --recurse-submodules' master
#Switched to branch 'master'
#Your branch is up-to-date with 'origin/master'.
$ 'git status'
#On branch master
#Your branch is up-to-date with 'origin/master'.
#nothing to commit, working tree clean
Using the --recurse-submodules flag of git checkout can also be useful when you work on several branches in the superproject,
 each having your submodule pointing at different commits. Indeed, if you switch between branches that record the submodule at
different commits, upon executing git status the submodule will appear as =modified=, and indicate =new commits=. That is
 because the submodule state is by default not carried over when switching branches.
This can be really confusing, so its a good idea to always git checkout --recurse-submodules when your project has submodules.
 For- older Git versions that do- not have the --recurse-submodules flag, after the checkout you can use
git submodule update --init --recursive to put the submodules in the right state.
Luckily- you can tell Git />=2.14/ to always use the --recurse-submodules flag by setting the configuration option submodule.recurse:
 git config submodule.recurse true. As noted above, this will also make Git recurse into submodules for every command that
has a --recurse-submodules option =except git clone=.

Switching from subdirectories to submodules
The other main caveat that many people run into involves switching from subdirectories to submodules. If youve been
 tracking files in your project and you want to move them out into a submodule, you must be careful or Git will get angry at you.
Assume that you have files in a subdirectory of your project, and you want to switch it to a submodule. If you delete the
 subdirectory and then- run submodule add, Git yells at you:
$ rm -Rf CryptoLibrary/
$ 'git submodule add' https://github.com/chaconinc/CryptoLibrary
#'CryptoLibrary' already exists in the index
You have to unstage the CryptoLibrary directory first. Then you can add the submodule:
$ 'git rm -r' CryptoLibrary
$ 'git submodule add' https://github.com/chaconinc/CryptoLibrary
#Cloning into 'CryptoLibrary'...
#Checking connectivity... done.
Now suppose you did that in a branch. If you try to switch back to a branch where those files are still in the
 actual tree rather than a submodule — you get this error:
$ 'git checkout' master
#error: The following untracked working tree files would be overwritten by checkout:
#  CryptoLibrary/includes/crypto.h
#Please move or remove them before you can switch branches.
#Aborting
You can force it to switch with checkout -f, but be careful that you dont have unsaved changes in there as they could be overwritten with that command.
$ 'git checkout -f' master
#warning: unable to rmdir CryptoLibrary: Directory not empty
#Switched to branch 'master'
Then- when you switch back, you get an empty CryptoLibrary directory for some reason and git submodule update may not fix it either.
 You may need to go into your submodule directory and run a git checkout . to get all your files back. You could
run this in a submodule foreach script to run it for multiple submodules.
Its important to note that submodules these days keep all their Git data in the top projects .git directory, so unlike
 much older versions of Git, destroying a submodule directory wont lose any commits or branches that you had.
With these tools, submodules can be a fairly simple and effective method for developing on several related but still separate projects simultaneously.

https://git-scm.com/book/en/v2/Git-Tools-Bundling
Bundling
Though weve covered the common ways to transfer Git data over a network /HTTP, SSH, etc/, there is actually one more way to do- so that
 is not commonly used but can actually be quite useful.
Git is capable of =bundling= its data into a single file. This can be useful in various scenarios. Maybe your network is
 down and you want to send changes to your co-workers. Perhaps youre working somewhere offsite and dont have
access to the local network for security reasons. Maybe your wireless/ethernet card just broke. Maybe you dont have access to a
 shared server for the moment, you want to email someone updates and you dont want to transfer 40 commits via format-patch.
This is where the git bundle command can be helpful. The bundle command will package up everything that would normally be
 pushed over the wire with a git push command into a binary file that you can email to someone or put on a flash drive, then- unbundle into another repository.
Lets see a simple example. Lets say you have a repository with two commits:
$ git log
#commit 9a466c572fe88b195efd356c3f2bbeccdb504102
#    Second commit
#commit b1ec3248f39900d2a406049d762aa68e9641be25
#    First commit
If- you want to send that repository to someone and you dont have access to a repository to push to,
 or simply dont want to set one up, you can bundle it with git bundle create.
$ 'git bundle create' repo.bundle HEAD master
Now you have a file named repo.bundle that has all the data needed to re-create the repositorys master branch.
 With the bundle command you need to list out every reference or specific range of commits that you want to be included.
If- you intend for this to be cloned somewhere else, you should add HEAD as a reference as well as weve done- here.
You can email this repo.bundle file to someone else, or put it on a USB drive and walk it over.
On the other side, say you are sent this repo.bundle file and want to work on the project. You can clone from the
 binary file into a directory, much like you would from a URL.
$ 'git clone' repo.bundle repo
If- you dont include HEAD in the references, you have to also specify -b master or whatever branch is included because otherwise it
 wont know what branch to check out.
Now lets say you do- three commits on it and want to send the new commits back via a bundle on a USB stick or email.
$ git log --oneline
#71b84da Last commit - second repo
#c99cf5b Fourth commit - second repo
#7011d3d Third commit - second repo
#9a466c5 Second commit
#b1ec324 First commit
First we need to determine the range of commits we want to include in the bundle. Unlike the network protocols which figure out the
 minimum set of data to transfer over the network for us, well have to figure this out manually. Now, you could just do- the
same thing and bundle the entire repository, which will work, but its better to just bundle up the difference - just the three commits we just made locally.
In order to do- that, youll have to calculate the difference. As we described in Commit Ranges, you can specify a range of commits in a
 number of ways. To get the three commits that we have in our master branch that werent in the branch we originally cloned,
we can use something like origin/master..master or master ^origin/master. You can test that with the log command.
$ 'git log --oneline master ^origin/master'
#71b84da Last commit - second repo
#c99cf5b Fourth commit - second repo
#7011d3d Third commit - second repo
So now that we have the list of commits we want to include in the bundle, lets bundle them up. We do- that with the
 git bundle create command, giving it a filename we want our bundle to be and the range of commits we want to go into it.
$ 'git bundle create' commits.bundle 'master ^9a466c5'
Now we have a commits.bundle file in our directory. If we take that and send it to our partner, she can then- import it into the
 original repository, even if more work has been done- there in the meantime.
When she gets the bundle, she can inspect it to see what it contains before she imports it into her repository. The first command is the
 bundle verify command that will make sure the file is actually a valid Git bundle and that you have all the necessary ancestors to reconstitute it properly.
$ 'git bundle verify' ../commits.bundle
#The bundle contains 1 ref
#71b84daaf49abed142a373b6e5c59a22dc6560dc refs/heads/master
#The bundle requires these 1 ref
#9a466c572fe88b195efd356c3f2bbeccdb504102 second commit
../commits.bundle is 'okay'
If- the bundler had created a bundle of just the last two commits they had done, rather than all three, the
 original repository would not be able to import it, since it is missing requisite history. The verify command would have looked like this instead:
$ 'git bundle verify' ../commits-bad.bundle
'error': Repository lacks these prerequisite commits:
#error: 7011d3d8fc200abe0ad561c011c3852a4b7bbe95 Third commit - second repo
However- our first bundle is valid, so we can fetch in commits from it. If you want to see what branches are in the
 bundle that can be imported, there is also a command to just list the heads:
$ 'git bundle list-heads' ../commits.bundle
#71b84daaf49abed142a373b6e5c59a22dc6560dc refs/heads/master
The verify sub-command will tell you the heads as well. The point is to see what can be pulled in, so you can use the
 fetch or pull commands to import commits from this bundle. Here well fetch the master branch of the bundle to a
branch named other-master in our repository:
$ 'git fetch' ../commits.bundle 'master:other-master'
#From ../commits.bundle
# * [new branch]      master     -> other-master
Now we can see that we have the imported commits on the other-master branch as well as any commits weve done- in the meantime in our own master branch.
$ 'git log --oneline --decorate --graph --all'
#* 8255d41 (HEAD, master) Third commit - first repo
#| * 71b84da (other-master) Last commit - second repo
#| * c99cf5b Fourth commit - second repo
#| * 7011d3d Third commit - second repo
#|/
#* 9a466c5 Second commit
#* b1ec324 First commit
So- git bundle can be really useful for sharing or doing network-type operations when you dont have the proper network or shared repository to do- so.

https://git-scm.com/book/en/v2/Git-Tools-Replace
Replace
As weve emphasized before, the objects in Gits object database are unchangeable, but Git does provide an interesting way to
 pretend to replace objects in its database with other objects.
The replace command lets you specify an object in Git and say "every time you refer to this object, pretend it’s a different object".
 This is most commonly useful for replacing one commit in your history with another one without having to rebuild the
entire history with, say, 'git filter-branch.'
$ git log --oneline
#ef989d8 Fifth commit
#c6e1e95 Fourth commit
#9c68fdc Third commit
#945704c Second commit
#c1822cf First commit
$ 'git branch' history c6e1e95
$ git log --oneline --decorate
#ef989d8 (HEAD, master) Fifth commit
#c6e1e95 (history) Fourth commit
#9c68fdc Third commit
#945704c Second commit
#c1822cf First commit
Now we can push the new history branch to the master branch of our new repository:
$ 'git remote add' project-history https://github.com/schacon/project-history
$ 'git push' project-history history:master
OK- so our history is published. Now the harder part is truncating our recent history down so its smaller. We need an
 overlap so we can replace a commit in one with an equivalent commit in the other, so were going to truncate this to
just commits four and five =so commit four overlaps=.
$ git log --oneline --decorate
#ef989d8 (HEAD, master) Fifth commit
#c6e1e95 (history) Fourth commit
#9c68fdc Third commit
#945704c Second commit
#c1822cf First commit
Its useful in this case to create a base commit that has instructions on how to expand the history, so other developers
 know what to do- if they hit the first commit in the truncated history and need more. So, what were going to do- is create an
initial commit object as our base point with instructions, then- rebase the remaining commits =four and five= on top of it.
To do- that, we need to choose a point to split at, which for us is the third commit, which is 9c68fdc in SHA-speak. So,
 our base commit will be based off of that tree. We can create our base commit using the commit-tree command, which just
takes a tree and will give us a brand new, parentless commit object SHA-1 back.
#$ echo 'Get history from https://github.com/schacon/project-history' | git commit-tree 9c68fdc^{tree}
#622e88e9cbfbacfb75b5279245b9fb38dfea10cf
Note- The commit-tree command is one of a set of commands that are commonly referred to as 'plumbing' commands.
 These are commands that are not generally meant to be used directly, but instead are used by other Git commands to do-
smaller jobs. On occasions when were doing weirder things like this, they allow us to do- really low-level things but are
 not meant for daily use. You can read more about plumbing commands in Plumbing and Porcelain.
https://git-scm.com/book/en/v2/ch00/_plumbing_porcelain
OK- so now that we have a base commit, we can rebase the rest of our history on top of that with git rebase --onto. The --onto argument will be
 the SHA-1 we just got back from commit-tree and the rebase point will be the third commit =the parent of the first commit we want to keep, 9c68fdc=:
$ 'git rebase --onto' 622e88 9c68fdc
#First- rewinding head to replay your work on top of it...
#Applying: fourth commit
#Applying: fifth commit
OK- so now weve re-written our recent history on top of a throw away base commit that now has instructions in it on how to
 reconstitute the entire history if we wanted to. We can push that new history to a new project and now when people clone that repository,
they will only see the most recent two commits and then- a base commit with instructions.
Lets now switch roles to someone cloning the project for the first time who wants the entire history. To get the
 history data after cloning this truncated repository, one would have to add a second remote for the historical repository and fetch:
$ 'git clone' https://github.com/schacon/project
$ cd project
$ git log --oneline master
#e146b5f Fifth commit
#81a708d Fourth commit
#622e88e Get history from https://github.com/schacon/project-history
$ 'git remote add' project-history https://github.com/schacon/project-history
$ 'git fetch' project-history
From https://github.com/schacon/project-history
# * [new branch]      master     -> project-history/master
Now the collaborator would have their recent commits in the master branch and the historical commits in the project-history/master branch.
$ git log --oneline master
#e146b5f Fifth commit
#81a708d Fourth commit
#622e88e Get history from https://github.com/schacon/project-history
$ git log --oneline project-history/master
#c6e1e95 Fourth commit
#9c68fdc Third commit
#945704c Second commit
#c1822cf First commit
To combine them, you can simply call git replace with the commit you want to replace and then- the commit you want to
 replace it with. So we want to replace the "fourth" commit in the master branch with the "fourth" commit in the project-history/master branch:
$ 'git replace' 81a708d c6e1e95
Now- if you look at the history of the master branch, it appears to look like this:
$ git log --oneline master
#e146b5f Fifth commit
#81a708d Fourth commit
#9c68fdc Third commit
#945704c Second commit
#c1822cf First commit
Without having to change all the SHA-1s upstream, we were able to replace one commit in our history with an
 entirely different commit and all the normal tools =bisect, blame, etc= will work how we would expect them to.
Interestingly- it still shows 81a708d as the SHA-1, even though its actually using the c6e1e95 commit data that
 we replaced it with. Even if you run a command like cat-file, it will show you the replaced data:
$ 'git cat-file -p' 81a708d
#tree 7bc544cf438903b65ca9104a1e30345eee6c083d
#parent 9c68fdceee073230f19ebb8b5e7fc71b479c0252
#author Scott Chacon <schacon@gmail.com> 1268712581 -0700
#committer Scott Chacon <schacon@gmail.com> 1268712581 -0700
#fourth commit
Remember that the actual parent of 81a708d was our placeholder commit =622e88e=, not 9c68fdce as it states here.
Another interesting thing is that this data is kept in our references:
$ 'git for-each-ref'
#e146b5f14e79d4935160c0e83fb9ebe526b8da0d commit	refs/heads/master
#c6e1e95051d41771a649f3145423f8809d1a74d4 commit	refs/remotes/history/master
#e146b5f14e79d4935160c0e83fb9ebe526b8da0d commit	refs/remotes/origin/HEAD
#e146b5f14e79d4935160c0e83fb9ebe526b8da0d commit	refs/remotes/origin/master
#c6e1e95051d41771a649f3145423f8809d1a74d4 commit	refs/replace/81a708dd0e167a3f691541c7a6463343bc457040
This means that its easy to share our replacement with others, because we can push this to our server and other people can
 easily download it. This is not that helpful in the history grafting scenario weve gone over here /since everyone would be
  downloading both histories anyhow, so why separate them?/ but it can be useful in other circumstances.

https://git-scm.com/book/en/v2/Git-Tools-Credential-Storage
Credential Storage
If- you use the SSH transport for connecting to remotes, its possible for you to have a key without a passphrase,
 which allows you to securely transfer data without typing in your username and password. However, this isnt possible with the
HTTP protocols — every connection needs a username and password. This gets even harder for systems with two-factor authentication,
 where the token you use for a password is randomly generated and unpronounceable.
Fortunately- Git has a credentials system that can help with this. Git has a few options provided in the box:
The default is not to cache at all. Every connection will prompt you for your username and password.
The /cache/ mode keeps credentials in memory for a certain period of time. None of the passwords are ever stored on disk,
 and they are purged from the cache after 15 minutes.
The /store/ mode saves the credentials to a plain-text file on disk, and they never expire. This means that until you
 change your password for the Git host, you wont ever have to type in your credentials again. The downside of this approach is that
your passwords are stored in cleartext in a plain file in your home directory.
If- youre using Windows, you can enable the Git Credential Manager feature when installing Git for Windows or separately install the
 latest GCM as a standalone service. It can also serve credentials to WSL1 or WSL2. See GCM Install Instructions for more information.
https://github.com/git-ecosystem/git-credential-manager#readme
You can choose one of these methods by setting a Git configuration value:
$ 'git config --global credential.helper cache'
Some of these helpers have options. The =store= helper can take a --file <path> argument, which customizes where the
 plain-text file is saved 'the default is ~/.git-credentials'. The =cache= helper accepts the --timeout <seconds> option,
which changes the amount of time its daemon is kept running =the default is /900/, or 15 minutes=. Heres an example of how
 youd configure the =store= helper with a custom file name:
$ 'git config --global credential.helper store --file ~/.my-credentials'
Git even allows you to configure several helpers. When looking for credentials for a particular host, Git will query them in order, and
 stop after the first answer is provided. When saving credentials, Git will send the username and password to all of the
helpers in the list, and they can choose what to do= with them. Heres what a .gitconfig would look like if you had a
 credentials file on a thumb drive, but wanted to use the in-memory cache to save some typing if the drive isnt plugged in:
#[credential]
#    helper = store --file /mnt/thumbdrive/.git-credentials
#    helper = cache --timeout 30000

Under the Hood
How does this all work? Gits root command for the credential-helper system is git credential,
 which takes a command as an argument, and then- more input through stdin.
This might be easier to understand with an example. Lets say that a credential helper has been configured, and the helper has
 stored credentials for mygithost. Heres a session that uses the =fill= command, which is invoked when Git is trying to find credentials for a host:
1 This is the command line that initiates the interaction.
$ 'git credential fill'
2 Git-credential is then- waiting for input on stdin. We provide it with the things we know: the protocol and hostname.
#protocol=https
#host=mygithost
3 A blank line indicates that the input is complete, and the credential system should answer with what it knows.
#
4 Git-credential then- takes over, and writes to stdout with the bits of information it found.
#protocol=https
#host=mygithost
#username=bob
#password=s3cre7
5 If credentials are not found, Git asks the user for the username and password, and provides them back to the
 invoking stdout =here theyre attached to the same console=.
$ 'git credential fill'
#protocol=https
#host=unknownhost
#
#Username for 'https://unknownhost': bob
#Password for 'https://bob@unknownhost':
#protocol=https
#host=unknownhost
#username=bob
#password=s3cre7
The credential system is actually invoking a program thats separate from Git itself; which one and how depends on the
 credential.helper configuration value. There are several forms it can take:
#Configuration Value                 /=/	Behavior
foo                                 /=/ Runs git-credential-foo
foo -a --opt=bcd                    /=/ Runs git-credential-foo -a --opt=bcd
/absolute/path/foo -xyz             /=/ Runs /absolute/path/foo -xyz
#!f() { echo "password=s3cre7"; }; f /=/ Code after ! evaluated in shell
So the helpers described above are actually named 'git-credential-cache', 'git-credential-store', and so on, and we can
 configure them to take command-line arguments. The general form for this is 'git-credential-foo [args] <action>.'
The stdin/stdout protocol is the same as git-credential, but they use a slightly different set of actions:
'get' is a request for a username/password pair.
'store' is a request to save a set of credentials in this helpers memory.
'erase' purge the credentials for the given properties from this helpers memory.
For- the store and erase actions, no response is required =Git ignores it anyway=. For the get action, however,
 Git is very interested in what the helper has to say. If the helper doesnt know anything useful, it can simply exit with
no output, but if it does know, it should augment the provided information with the information it has stored. The output is
 treated like a series of assignment statements; anything provided will replace what Git already knows.
Heres the same example from above, but skipping git-credential and going straight for git-credential-store:
1 Here we tell git-credential-store to save some credentials: the username =bob= and the password =s3cre7= are to be used when https://mygithost is accessed.
$ 'git credential-store --file' ~/git.store 'store'
#protocol=https
#host=mygithost
#username=bob
#password=s3cre7
2 Now well retrieve those credentials. We provide the parts of the connection we already know =https://mygithost=, and an empty line.
$ 'git credential-store --file ~/git.store get'
#protocol=https
#host=mygithost
3 git-credential-store replies with the username and password we stored above.
#username=bob
#password=s3cre7
Heres what the ~/git.store file looks like:
#https://bob:s3cre7@mygithost
Its just a series of lines, each of which contains a credential-decorated URL. The osxkeychain and wincred helpers use the
 native format of their backing stores, while cache uses its own in-memory format =which no other process can read=.

A Custom Credential Cache
Given that git-credential-store and friends are separate programs from Git, its not much of a leap to realize that
 any program can be a Git credential helper. The helpers provided by Git cover many common use cases, but not all. For example,
lets say your team has some credentials that are shared with the entire team, perhaps for deployment. These are stored in a
 shared directory, but you dont want to copy them to your own credential store, because they change often. None of the
existing helpers cover this case; lets see what it would take to write our own. There are several key features this program needs to have:
The only action we need to pay attention to is get; store and erase are write operations, so well just exit cleanly when theyre received.
The file format of the shared-credential file is the same as that used by git-credential-store.
The location of that file is fairly standard, but we should allow the user to pass a custom path just in case.
Well save our helper as git-credential-read-only, put it somewhere in our PATH and mark it executable.
$ 'git config --global credential.helper' 'read-only' --file /mnt/shared/creds

'https://git-scm.com/book/en/v2/Customizing-Git-Git-Configuration';
Git Configuration
you can specify Git configuration settings with the 'git config' command. One of the first things you did was set up your name and email address:
$ 'git config --global' user.name "John Doe"
$ 'git config --global' user.email johndoe@example.com
Now youll learn a few of the more interesting options that you can set in this manner to customize your Git usage.
First- a quick review: Git uses a series of configuration files to determine non-default behavior that you may want.
 The first place Git looks for these values is in the system-wide '[path]/etc/gitconfig' file, which contains settings that
are applied to every user on the system and all of their repositories. If you pass the option '--system' to git config, it reads and
 writes from this file specifically.
The next place Git looks is the "$HOME/.gitconfig" =or "$HOME/.config/git/config"= file, which is specific to each user. You can make
 Git read and write to this file by passing the '--global' option.
Finally- Git looks for configuration values in the configuration file in the Git directory ='.git/config'= of whatever
 repository youre currently using. These values are specific to that single repository, and represent passing the '--local' option to
git config. If you dont specify which level you want to work with, this is the default.
Each of these =levels= /'system, global, local'/ overwrites values in the previous level,
 so values in '.git/config' trump those in '[path]/etc/gitconfig', for instance.
Note- Gits configuration files are plain-text, so you can also set these values by manually editing the file and
 inserting the correct syntax. Its generally easier to run the 'git config' command, though.

'man git-config'
https://git-scm.com/docs/git-config
$ 'git config --global core.editor' emacs
$ 'git config --global commit.template' ~/.gitmessage.txt
'core.pager'
This setting determines which pager is used when Git pages output such as log and diff. You can set it to more or to
 your favorite pager =by default, its less=, or you can turn it off by setting it to a blank string:
$ git config --global core.pager ''
If- you run that, Git will print the entire output of all commands, no matter how long they are.
'user.signingkey'
If- youre making signed annotated tags =as discussed in Signing Your Work=, setting your GPG signing key as a
 configuration setting makes things easier. Set your key ID like so:
$ git config --global user.signingkey /gpg-key-id/
Now- you can sign tags without having to specify your key every time with the git tag command:
$ git tag -s /tag-name/
'core.excludesfile'
You can put patterns in your projects .gitignore file to have Git not see them as untracked files or try to stage them when you
 run git add on them, as discussed in Ignoring Files.
But sometimes you want to ignore certain files for all repositories that you work with. If your computer is running macOS, youre
 probably familiar with .DS_Store files. If your preferred editor is Emacs or Vim, you know about filenames that end with a ~ or .swp.
This setting lets you write a kind of global '.gitignore' file. If you create a "$HOME/.gitignore_global" file with these contents:
#*~
#.*.swp
#.DS_Store
and you run 'git config --global core.excludesfile ~/.gitignore_global', Git will never again bother you about those files.

'External Merge and Diff Tools';
Although Git has an internal implementation of diff, which is what weve been showing in this book, you can set up an
 external tool instead. You can also set up a graphical merge-conflict-resolution tool instead of having to resolve conflicts manually.
Set up a merge wrapper script named extMerge that calls your binary with all the arguments provided:
$ cat /usr/local/bin/'extMerge'
#!/bin/sh
#/Applications/p4merge.app/Contents/MacOS/p4merge $*
The diff wrapper checks to make sure seven arguments are provided and passes two of them to your merge script. By default,
 Git passes the following arguments to the diff program:
'path old-file old-hex old-mode new-file new-hex new-mode'
Because you only want the old-file and new-file arguments, you use the wrapper script to pass the ones you need.
$ cat /usr/local/bin/'extDiff'
#!/bin/sh
#[ $# -eq 7 ] && /usr/local/bin/extMerge "$2" "$5"
You also need to make sure these tools are executable:
#$ sudo chmod +x /usr/local/bin/extMerge
#$ sudo chmod +x /usr/local/bin/extDiff
Now you can set up your config file to use your custom merge resolution and diff tools. This takes a
 number of custom settings: 'merge.tool' to tell Git what strategy to use, 'mergetool./tool/.cmd' to specify how to
run the command, 'mergetool./tool/.trustExitCode' to tell Git if the exit code of that program indicates a
 successful merge resolution or not, and 'diff.external' to tell Git what command to run for diffs. So, you can either run four config commands:
$ 'git config --global merge.tool' extMerge
$ 'git config --global mergetool.extMerge.cmd' \
#  'extMerge "$BASE" "$LOCAL" "$REMOTE" "$MERGED"'
$ 'git config --global mergetool.extMerge.trustExitCode' false
$ 'git config --global diff.external' extDiff
or you can edit your ~/.gitconfig file to add these lines:
#[merge]
#  tool = extMerge
#[mergetool "extMerge"]
#  cmd = extMerge "$BASE" "$LOCAL" "$REMOTE" "$MERGED"
#  trustExitCode = false
#[diff]
#  external = extDiff
After all this is set, if you run diff commands such as this:
$ 'git diff' 32d1776b1^ 32d1776b1
Instead of getting the diff output on the command line, Git fires up P4Merge:
If- you try to merge two branches and subsequently have merge conflicts, you can run the command 'git mergetool';
 it starts P4Merge to let you resolve the conflicts through that GUI tool.
The nice thing about this wrapper setup is that you can change your diff and merge tools easily. For example, to
 change your extDiff and extMerge tools to run the KDiff3 tool instead, all you have to do- is edit your extMerge file:
$ cat /usr/local/bin/extMerge
#!/bin/sh
#/Applications/kdiff3.app/Contents/MacOS/kdiff3 $*
Now- Git will use the KDiff3 tool for diff viewing and merge conflict resolution.
Git comes preset to use a number of other merge-resolution tools without your having to set up the cmd configuration.
 To see a list of the tools it supports, try this:
$ 'git mergetool --tool-help'
If- youre not interested in using KDiff3 for diff but rather want to use it just for merge resolution, and the
 kdiff3 command is in your path, then- you can run:
$ git config --global merge.tool kdiff3
If- you run this instead of setting up the extMerge and extDiff files, Git will use KDiff3 for merge resolution and the normal Git diff tool for diffs.

'core.autocrlf'
If- youre on a Windows machine, set it to true — this converts LF endings into CRLF when you check out code:
$ 'git config --global core.autocrlf true'
If- youre on a Linux or macOS system that uses LF line endings, then- you dont want Git to automatically convert them when you
 check out files; however- if a file with CRLF endings accidentally gets introduced, then- you may want Git to fix it.
You can tell Git to convert CRLF to LF on commit but not the other way around by setting core.autocrlf to input:
$ 'git config --global core.autocrlf input'
This setup should leave you with CRLF endings in Windows checkouts, but LF endings on macOS and Linux systems and in the repository.

'core.whitespace'
Git comes preset to detect and fix some whitespace issues. It can look for six primary whitespace issues — three are
 enabled by default and can be turned off, and three are disabled by default but can be activated.
The three that are turned on by default are
 'blank-at-eol' which looks for spaces at the end of a line;
 'blank-at-eof' which notices blank lines at the end of a file; and
 'space-before-tab' which looks for spaces before tabs at the beginning of a line.
The three that are disabled by default but can be turned on are
 'indent-with-non-tab' which looks for lines that begin with spaces instead of tabs =and is controlled by the tabwidth option=;
 'tab-in-indent' which watches for tabs in the indentation portion of a line; and
 'cr-at-eol' which tells Git that carriage returns at the end of lines are OK.
You can tell Git which of these you want enabled by setting core.whitespace to the values you want on or off, separated by commas.
 You can disable an option by prepending a - in front of its name, or use the default value by leaving it out of the
setting string entirely. For example, if you want all but space-before-tab to be set, you can do- this =with trailing-space being a
 short-hand to cover both blank-at-eol and blank-at-eof=:
$ 'git config --global core.whitespace' \
    trailing-space,-space-before-tab,indent-with-non-tab,tab-in-indent,cr-at-eol
Or you can specify the customizing part only:
$ 'git config --global core.whitespace' \
    -space-before-tab,indent-with-non-tab,tab-in-indent,cr-at-eol
Git will detect these issues when you run a git diff command and try to color them so you can possibly fix them before you commit.
 It will also use these values to help you when you apply patches with git apply. When youre applying patches, you can ask Git to
warn you if its applying patches with the specified whitespace issues:
$ 'git apply --whitespace=warn' /patch/
Or you can have Git try to automatically fix the issue before applying the patch:
$ 'git apply --whitespace=fix' /patch/
These options apply to the 'git rebase' command as well. If youve committed whitespace issues but havent yet pushed upstream,
 you can run 'git rebase --whitespace=fix' to have Git automatically fix whitespace issues as its rewriting the patches.

'Server Configuration';
Not nearly as many configuration options are available for the server side of Git, but there are a few interesting ones you may want to take note of.
'receive.fsckObjects'
Git is capable of making sure every object received during a push still matches its SHA-1 checksum and points to
 valid objects. However, it doesnt do- this by default; its a fairly expensive operation, and might slow down the operation,
especially on large repositories or pushes. If you want Git to check object consistency on every push, you can force it to do- so by
 setting receive.fsckObjects to true:
$ 'git config --system receive.fsckObjects true'
Now- Git will check the integrity of your repository before each push is accepted to make sure faulty =or malicious= clients arent introducing corrupt data.
'receive.denyNonFastForwards'
If- you rebase commits that youve already pushed and then- try to push again, or otherwise try to push a commit to a
 remote branch that doesnt contain the commit that the remote branch currently points to, youll be denied.
This is generally good policy; but in the case of the rebase, you may determine that you know what youre doing and
 can force-update the remote branch with a -f flag to your push command.
To tell Git to refuse force-pushes, set receive.denyNonFastForwards:
$ 'git config --system receive.denyNonFastForwards true'
The other way you can do- this is via server-side receive hooks, which well cover in a bit. That approach lets you do- more
 complex things like deny non-fast-forwards to a certain subset of users.
'receive.denyDeletes'
One of the workarounds to the denyNonFastForwards policy is for the user to delete the branch and then- push it back up with the
 new reference. To avoid this, set receive.denyDeletes to true:
$ 'git config --system receive.denyDeletes true'
This denies any deletion of branches or tags — no user can do- it. To remove remote branches, you must remove the
 ref files from the server manually. There are also more interesting ways to do- this on a per-user basis via ACLs,
as youll learn in An Example Git-Enforced Policy.
https://git-scm.com/book/en/v2/ch00/_an_example_git_enforced_policy

'https://git-scm.com/book/en/v2/Customizing-Git-Git-Attributes';
Git Attributes
Some of these settings can also be specified for a path, so that Git applies those settings only for a subdirectory or
 subset of files. These path-specific settings are called Git attributes and are set either in a '.gitattributes' file in one of
your directories =normally the root of your project= or in the '.git/info/attributes' file if you dont want the
 attributes file committed with your project.
Using attributes, you can do- things like specify separate merge strategies for individual files or directories in your project,
 tell Git how to diff non-text files, or have Git filter content before you check it into or out of Git.

'Binary Files';
One cool trick for which you can use Git attributes is telling Git which files are binary =in cases it otherwise may not be able to figure out= and
 giving Git special instructions about how to handle those files. For instance, some text files may be machine generated and
not diffable, whereas some binary files can be diffed. Youll see how to tell Git which is which.

'Identifying Binary Files';
Some files look like text files but for all intents and purposes are to be treated as binary data. For instance,
 Xcode projects on macOS contain a file that ends in .pbxproj, which is basically a JSON =plain-text JavaScript data format= dataset
written out to disk by the IDE, which records your build settings and so on. Although its technically a text file =because its all UTF-8=,
 you dont want to treat it as such because its really a lightweight database – you cant merge the contents if two people change it,
and diffs generally arent helpful. The file is meant to be consumed by a machine. In essence, you want to treat it like a binary file.
To tell Git to treat all pbxproj files as binary data, add the following line to your '.gitattributes' file:
#*.pbxproj binary
Now- Git wont try to convert or fix CRLF issues; nor will it try to compute or print a diff for changes in this file when
 you run git show or git diff on your project

'Diffing Binary Files';
You can also use the Git attributes functionality to effectively diff binary files. You do- this by telling Git how to
 convert your binary data to a text format that can be compared via the normal diff.
First- youll use this technique to solve one of the most annoying problems known to humanity: version-controlling Microsoft Word documents.
 If- you want to version-control Word documents, you can stick them in a Git repository and commit every once in a while;
but what good does that do? If you run git diff normally, you only see something like this:
$ git diff
diff --git a/chapter1.docx b/chapter1.docx
index 88839c4..4afcb7c 100644
Binary files a/chapter1.docx and b/chapter1.docx differ
You cant directly compare two versions unless you check them out and scan them manually, right? It turns out you can do- this
 fairly well using Git attributes. Put the following line in your '.gitattributes' file:
#*.docx diff=word
This tells Git that any file that matches this pattern =.docx= should use the =word= filter when you try to view a diff that
 contains changes. What is the =word= filter? You have to set it up. Here youll configure Git to use the docx2txt program to
convert Word documents into readable text files, which it will then- diff properly.
First- youll need to install docx2txt; you can download it from https://sourceforge.net/projects/docx2txt. Follow the
 instructions in the INSTALL file to put it somewhere your shell can find it. Next, youll write a wrapper script to
convert output to the format Git expects. Create a file thats somewhere in your path called docx2txt, and add these contents:
#!/bin/bash
docx2txt.pl "$1" -
Dont forget to chmod a+x that file. Finally, you can configure Git to use this script:
$ git config diff.word.textconv docx2txt
Now Git knows that if it tries to do- a diff between two snapshots, and any of the files end in .docx, it should run those
 files through the =word= filter, which is defined as the docx2txt program. This effectively makes nice text-based versions of
your Word files before attempting to diff them.
$ git diff
Another interesting problem you can solve this way involves diffing image files. One way to do- this is to run image files through a
 filter that extracts their EXIF information – metadata that is recorded with most image formats. If you download and
install the exiftool program, you can use it to convert your images into text about the metadata, so at least the
 diff will show you a textual representation of any changes that happened. Put the following line in your '.gitattributes' file:
#*.png diff=exif
Configure Git to use this tool:
$ git config diff.exif.textconv exiftool
$ git diff

'Keyword Expansion';
SVN- or CVS-style keyword expansion is often requested by developers used to those systems. The main problem with this in Git is that
 you cant modify a file with information about the commit after youve committed, because Git checksums the file first. However,
you can inject text into a file when its checked out and remove it again before its added to a commit. Git attributes offers you two ways to do- this.
First- you can inject the SHA-1 checksum of a blob into an \$Id$ field in the file automatically. If you set this attribute on a
 file or set of files, then- the next time you check out that branch, Git will replace that field with the SHA-1 of the blob.
Its important to notice that it isnt the SHA-1 of the commit, but of the blob itself. Put the following line in your '.gitattributes' file:
#*.txt ident
Add an \$Id$ reference to a test file:
$ echo \$Id$ > test.txt
The next time you check out this file, Git injects the SHA-1 of the blob:
$ rm test.txt
$ git checkout -- test.txt
$ cat test.txt
#$Id: 42812b7653c7b88933f8a9d6cad0ca16714b9bb3 $
However- that result is of limited use. If youve used keyword substitution in CVS or Subversion, you can include a
 datestamp – the SHA-1 isnt all that helpful, because its fairly random and you cant tell if one SHA-1 is older or newer than
another just by looking at them.
It turns out that you can write your own filters for doing substitutions in files on commit/checkout. These are
 called /clean/ and /smudge/ filters. In the '.gitattributes' file, you can set a filter for particular paths and then- set up scripts that
will process files just before theyre checked out /=smudge=, see The =smudge= filter is run on checkout/ and
 just before theyre staged /=clean=, see The =clean= filter is run when files are staged/.
The original commit message for this feature gives a simple example of running all your C source code through the
 indent program before committing. You can set it up by setting the filter attribute in your '.gitattributes' file to
filter \*.c files with the =indent= filter:
#*.c filter=indent
Then- tell Git what the =indent= filter does on smudge and clean:
$ 'git config --global filter.indent.clean' indent
$ 'git config --global filter.indent.smudge' cat
In this case, when you commit files that match \*.c, Git will run them through the indent program before it stages them and then-
 run them through the cat program before it checks them back out onto disk. The cat program does essentially nothing:
it spits out the same data that it comes in. This combination effectively filters all C source code files through indent before committing.
Another interesting example gets \$Date$ keyword expansion, RCS style. To do- this properly, you need a small script that
 takes a filename, figures out the last commit date for this project, and inserts the date into the file. Here is a small Ruby script that does that:
#! /usr/bin/env ruby
#data = STDIN.read
#last_date = `git log --pretty=format:"%ad" -1`
#puts data.gsub('$Date$', '$Date: ' + last_date.to_s + '$')
All the script does is get the latest commit date from the git log command, stick that into any \$Date$ strings it sees in stdin, and
 print the results – it should be simple to do- in whatever language youre most comfortable in. You can name this file expand_date and
put it in your path. Now, you need to set up a filter in Git =call it dater= and tell it to use your expand_date filter to
 smudge the files on checkout. Youll use a Perl expression to clean that up on commit:
$ 'git config filter.dater.smudge' expand_date
$ 'git config filter.dater.clean'
# \'perl -pe "s/\\\$Date[^\\\$]*\\\$/\\\$Date\\\$/"'
This Perl snippet strips out anything it sees in a \$Date$ string, to get back to where you started. Now that
 your filter is ready, you can test it by setting up a Git attribute for that file that engages the new filter and
creating a file with your \$Date$ keyword:
#date*.txt filter=dater
#$ echo '# $Date$' > date_test.txt
If- you commit those changes and check out the file again, you see the keyword properly substituted:
$ 'git add' date_test.txt .gitattributes
$ 'git commit -m' "Test date expansion in Git"
#$ rm date_test.txt
$ 'git checkout' date_test.txt
#$ cat date_test.txt
# $Date: Tue Apr 21 07:26:52 2009 -0700$
You can see how powerful this technique can be for customized applications. You have to be careful, though, because the
 '.gitattributes' file is committed and passed around with the project, but the driver =in this case, dater= isnt, so it
wont work everywhere. When you design these filters, they should be able to fail gracefully and have the project still work properly.

'Exporting Your Repository';
Git attribute data also allows you to do- some interesting things when exporting an archive of your project.
'export-ignore'
You can tell Git not to export certain files or directories when generating an archive. If there is a subdirectory or file that
 you dont want to include in your archive file but that you do- want checked into your project, you can determine those files via the export-ignore attribute.
For- example, say you have some test files in a test/ subdirectory, and it doesnt make sense to include them in the
 tarball export of your project. You can add the following line to your Git attributes file:
#test/ export-ignore
Now- when you run git archive to create a tarball of your project, that directory wont be included in the archive.
'export-subst'
When exporting files for deployment you can apply git logs formatting and keyword-expansion processing to
 selected portions of files marked with the export-subst attribute.
For- instance, if you want to include a file named LAST_COMMIT in your project, and have metadata about the
 last commit automatically injected into it when git archive runs, you can for example set up your '.gitattributes' and LAST_COMMIT files like this:
LAST_COMMIT export-subst
#$ echo 'Last commit date: $Format:%cd by %aN$' > LAST_COMMIT
$ 'git add' LAST_COMMIT .gitattributes
$ 'git commit' -am 'adding LAST_COMMIT file for archives'
When you run git archive, the contents of the archived file will look like this:
$ 'git archive' HEAD | tar xCf ../deployment-testing -
$ cat ../deployment-testing/LAST_COMMIT
#Last commit date: Tue Apr 21 08:38:48 2009 -0700 by Scott Chacon
The substitutions can include for example the commit message and any git notes, and git log can do- simple word wrapping:
#$ echo '$Format:Last commit: %h by %aN at %cd%n%+w(76,6,9)%B$' > LAST_COMMIT
$ 'git commit -am'
#\'export-subst uses git log'\''s custom formatter
#git archive uses git log'\''s `pretty=format:` processor
#directly, and strips the surrounding `$Format:` and `$`
#markup from the output.'
$ 'git archive' @ | tar xfO - LAST_COMMIT
#Last commit: 312ccc8 by Jim Hill at Fri May 8 09:14:04 2015 -0700
#       export-subst uses git log's custom formatter
#         git archive uses git log's `pretty=format:` processor directly, and
#         strips the surrounding `$Format:` and `$` markup from the output.
The resulting archive is suitable for deployment work, but like any exported archive it isnt suitable for further development work.

'Merge Strategies';
You can also use Git attributes to tell Git to use different merge strategies for specific files in your project.
 One very useful option is to tell Git to not try to merge specific files when they have conflicts, but rather to
use your side of the merge over someone elses.
This is helpful if a branch in your project has diverged or is specialized, but you want to be able to
 merge changes back in from it, and you want to ignore certain files. Say you have a database settings file called database.xml that
is different in two branches, and you want to merge in your other branch without messing up the database file.
 You can set up an attribute like this:
#database.xml merge=ours
And then- define a dummy ours merge strategy with:
$ 'git config --global merge.ours.driver true'
If- you merge in the other branch, instead of having merge conflicts with the database.xml file, you see something like this:
$ 'git merge' topic
#Auto-merging database.xml
#Merge made by recursive.
In this case, database.xml stays at whatever version you originally had.

'https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks';
Git Hooks
Like many other Version Control Systems, Git has a way to fire off custom scripts when certain important actions occur.
 There are two groups of these hooks: 'client-side' and 'server-side'. Client-side hooks are triggered by operations such as
committing and merging, while server-side hooks run on network operations such as receiving pushed commits.

'Installing a Hook';
The hooks are all stored in the hooks subdirectory of the Git directory. In most projects, thats '.git/hooks'. When you initialize a
 new repository with 'git init', Git populates the hooks directory with a bunch of example scripts, many of which are
useful by themselves; but they also document the input values of each script. All the examples are written as shell scripts,
 with some Perl thrown in, but any properly named executable scripts will work fine – you can write them in Ruby or Python or
whatever language you are familiar with. If you want to use the bundled hook scripts, youll have to rename them; their file names all end with .sample.
To enable a hook script, put a file in the hooks subdirectory of your '.git' directory that is named appropriately =without any extension= and
 is executable. From that point forward, it should be called.

'Client-Side Hooks';
There are a lot of client-side hooks. This section splits them into committing-workflow hooks, email-workflow scripts, and everything else.
Note- Its important to note that client-side hooks are not copied when you clone a repository. If your intent with these
 scripts is to enforce a policy, youll probably want to do- that on the server side; see the example in An Example Git-Enforced Policy.

'Committing-Workflow Hooks';
The first four hooks have to do- with the committing process.
pre-commit
prepare-commit-msg
commit-msg
post-commit

'Email Workflow Hooks';
git format-patch
applypatch-msg
pre-applypatch
post-applypatch

'Other Client Hooks';
pre-rebase
post-rewrite
post-checkout
post-merge
pre-push
pre-auto-gc

'Server-Side Hooks';
In addition to the client-side hooks, you can use a couple of important server-side hooks as a system administrator to
 enforce nearly any kind of policy for your project. These scripts run before and after pushes to the server.
The pre hooks can exit non-zero at any time to reject the push as well as print an error message back to the client;
 you can set up a push policy thats as complex as you wish.
pre-receive
update
post-receive
Tip- If youre writing a script/hook that others will need to read, prefer the long versions of command-line flags;

'https://git-scm.com/book/en/v2/Customizing-Git-An-Example-Git-Enforced-Policy';
An Example Git-Enforced Policy
Server-Side Hook
For- this example, all the server-side work will go into the 'update' file in your 'hooks' directory.
  The '.git/hooks/update' hook runs once per branch being pushed and takes three arguments:
The name of the reference being pushed to
The old revision where that branch was
The new revision being pushed
You also have access to the user doing the pushing if the push is being run over SSH. If youve allowed everyone to
 connect with a single user =like 'git'= via public-key authentication, you may have to give that user a shell wrapper that
determines which user is connecting based on the public key, and set an environment variable accordingly.

'Enforcing a Specific Commit-Message Format';
'.git/hooks/update'
#$regex = /\[ref: (\d+)\]/
# enforced custom commit message format
#def check_message_format
#  missed_revs = `git rev-list #{$oldrev}..#{$newrev}`.split("\n")
#  missed_revs.each do |rev|
#    message = `git cat-file commit #{rev} | sed '1,/^$/d'`
#    if !$regex.match(message)
#      puts "[POLICY] Your message is not formatted correctly"
#      exit 1
#    end
#  end
#end
#check_message_format
Putting that in your '.git/hooks/update' script, will reject updates that contain commits that have messages that dont adhere to your rule.

'Enforcing a User-Based ACL System';
To enforce this, youll write those rules to a file named 'acl' that lives in your bare Git repository on the server.
 Youll have the update hook look at those rules, see what files are being introduced for all the commits being pushed, and
determine whether the user doing the push has access to update all those files.
'acl' file uses a series of lines, where the first field is avail or unavail, the next field is a comma-delimited list of the
 users to which the rule applies, and the last field is the path to which the rule applies =blank meaning open access=.
All of these fields are delimited by a pipe '|' character.
avail|nickh,pjhyett,defunkt,tpw
avail|usinclair,cdickens,ebronte|doc
avail|schacon|lib
avail|schacon|tests
'.git/hooks/update'
#def get_acl_access_data(acl_file)
#  # read in ACL data
#  acl_file = File.read(acl_file).split("\n").reject { |line| line == '' }
#  access = {}
#  acl_file.each do |line|
#    avail, users, path = line.split('|')
#    next unless avail == 'avail'
#    users.split(',').each do |user|
#      access[user] ||= []
#      access[user] << path
#    end
#  end
#  access
#end
## only allows certain users to modify certain subdirectories in a project
#def check_directory_perms
#  access = get_acl_access_data('acl')
#  # see if anyone is trying to push something they can't
#  new_commits = `git rev-list #{$oldrev}..#{$newrev}`.split("\n")
#  new_commits.each do |rev|
#    files_modified = `git log -1 --name-only --pretty=format:'' #{rev}`.split("\n")
#    files_modified.each do |path|
#      next if path.size == 0
#      has_file_access = false
#      access[$user].each do |access_path|
#        if !access_path  # user has access to everything
#           || (path.start_with? access_path) # access to this path
#          has_file_access = true
#        end
#      end
#      if !has_file_access
#        puts "[POLICY] You do not have access to push to #{path}"
#        exit 1
#      end
#    end
#  end
#end
#check_directory_perms
Now your users cant push any commits with badly formed messages or with modified files outside of their designated paths.
From now on, as long as that '.git/hooks/update' script is there and executable, your repository will never have a
 commit message without your pattern in it, and your users will be sandboxed.

'Client-Side Hooks';
The downside to the server side hooks approach is the whining that will inevitably result when your users commit pushes are rejected.
 Having their carefully crafted work rejected at the last minute can be extremely frustrating and confusing;
and furthermore, they will have to edit their history to correct it, which isnt always for the faint of heart.
The answer to this dilemma is to provide some client-side hooks that users can run to notify them when theyre doing something that
 the server is likely to reject. That way, they can correct any problems before committing and before those issues become
more difficult to fix. Because hooks arent transferred with a clone of a project, you must distribute these scripts some
 other way and then- have your users copy them to their '.git/hooks' directory and make them executable.
You can distribute these hooks within the project or in a separate project, but Git wont set them up automatically.
First- the ACL file is in a different place, because this script runs from your working directory, not from your '.git' directory.
 You have to change the path to the ACL file from this:
#access = get_acl_access_data('acl')
to this:
#access = get_acl_access_data('.git/acl')
The other important difference is the way you get a listing of the files that have been changed. Because the
 server-side method looks at the log of commits, and, at this point, the commit hasnt been recorded yet, you must get your
file listing from the staging area instead. Instead of:
#files_modified = `git log -1 --name-only --pretty=format:'' #{ref}`
you have to use:
#files_modified = `git diff-index --cached --name-only HEAD`
But those are the only two differences – otherwise, the script works the same way. One caveat is that it expects you to
 be running locally as the same user you push as to the remote machine. If that is different, you must set the
#$user variable manually.

'https://git-scm.com/book/en/v2/Git-and-Other-Systems-Git-as-a-Client';

'https://git-scm.com/book/en/v2/Git-and-Other-Systems-Migrating-to-Git';

'https://git-scm.com/book/en/v2/Git-Internals-Plumbing-and-Porcelain';
First- if it isnt yet clear, Git is fundamentally a content-addressable filesystem with a VCS user interface written on top of it.
 Youll learn more about what this means in a bit.
'Plumbing and Porcelain';
This book covers primarily how to use Git with 30 or so subcommands such as checkout, branch, remote, and so on. But because
 Git was initially a toolkit for a version control system rather than a full user-friendly VCS, it has a number of subcommands that
do- low-level work and were designed to be chained together UNIX-style or called from scripts. These commands are
 generally referred to as Gits =plumbing= commands, while the more user-friendly commands are called =porcelain= commands.
Many of the plumbing commands arent meant to be used manually on the command line,
 but rather to be used as building blocks for new tools and custom scripts.
When you run 'git init' in a new or existing directory, Git creates the '.git' directory, which is where almost everything that
 Git stores and manipulates is located. If you want to back up or clone your repository, copying this single directory elsewhere gives you
nearly everything you need. This entire chapter basically deals with what you can see in this directory. Heres what a
 newly-initialized '.git' directory typically looks like:
$ ls -F1
#config
#description
#HEAD
#hooks/
#info/
#objects/
#refs/
Depending on your version of Git, you may see some additional content there, but this is a
 fresh 'git init' repository — its what you see by default. The description file is used only by the GitWeb program,
so dont worry about it. The config file contains your project-specific configuration options, and the info directory keeps a
 global exclude file for ignored patterns that you dont want to track in a '.gitignore' file. The hooks directory contains
your client- or server-side hook scripts, which are discussed in detail in Git Hooks.
This leaves four important entries: the HEAD and =yet to be created= index files, and the objects and refs directories.
 These are the core parts of Git. The objects directory stores all the content for your database, the refs directory stores
pointers into commit objects in that data =branches, tags, remotes and more=, the HEAD file points to the
 branch you currently have checked out, and the index file is where Git stores your staging area information.

'https://git-scm.com/book/en/v2/Git-Internals-Git-Objects';
'Git Objects';
Git is a content-addressable filesystem. Great. What does that mean? It means that at the core of Git is a simple key-value data store.
 What this means is that you can insert any kind of content into a Git repository, for which Git will hand you back a
unique key you can use later to retrieve that content.
As a demonstration, lets look at the plumbing command 'git hash-object', which takes some data, stores it in your
 '.git/objects' directory =the object database=, and gives you back the unique key that now refers to that data object.
First- you initialize a new Git repository and verify that there is =predictably= nothing in the objects directory:
$ 'git init' test
#Initialized empty Git repository in /tmp/test/.git/
$ cd test
$ 'find .git/objects'
#.git/objects
#.git/objects/info
#.git/objects/pack
$ 'find .git/objects -type f'
Git has initialized the objects directory and created pack and info subdirectories in it, but there are no regular files.
 Now- lets use git hash-object to create a new data object and manually store it in your new Git database:
$ echo 'test content' | 'git hash-object -w --stdin'
#d670460b4b4aece5915caf5c68d12f560a9fe3e4
In its simplest form, 'git hash-object' would take the content you handed to it and merely return the unique key that
 would be used to store it in your Git database. The -w option then- tells the command to not simply return the key,
but to write that object to the database. Finally, the --stdin option tells git hash-object to get the content to be
 processed from stdin; otherwise- the command would expect a filename argument at the end of the command containing the content to be used.
The output from the above command is a 40-character checksum hash. This is the SHA-1 hash — a checksum of the
 content youre storing plus a header, which youll learn about in a bit. Now you can see how Git has stored your data:
$ find .git/objects -type f
#.git/objects/d6/70460b4b4aece5915caf5c68d12f560a9fe3e4
If- you again examine your objects directory, you can see that it now contains a file for that new content.
 This is how Git stores the content initially — as a single file per piece of content, named with the SHA-1 checksum of the
content and its header. The subdirectory is named with the first 2 characters of the SHA-1, and the filename is the remaining 38 characters.
Once you have content in your object database, you can examine that content with the 'git cat-file' command.
 This command is sort of a Swiss army knife for inspecting Git objects. Passing -p to cat-file instructs the command to
first figure out the type of content, then- display it appropriately:
$ 'git cat-file -p' d670460b4b4aece5915caf5c68d12f560a9fe3e4
#test content
Now- you can add content to Git and pull it back out again. You can also do- this with content in files. For example, you can do-
 some simple version control on a file. First, create a new file and save its contents in your database:
$ echo 'version 1' > test.txt
$ 'git hash-object -w' test.txt
#83baae61804e65cc73a7201a7252750c76066a30
Then- write some new content to the file, and save it again:
$ echo 'version 2' > test.txt
$ 'git hash-object -w' test.txt
#1f7a7a472abf3dd9643fd615f6da379c4acb3e3a
Your object database now contains both versions of this new file =as well as the first content you stored there=:
$ find .git/objects -type f
#.git/objects/1f/7a7a472abf3dd9643fd615f6da379c4acb3e3a
#.git/objects/83/baae61804e65cc73a7201a7252750c76066a30
#.git/objects/d6/70460b4b4aece5915caf5c68d12f560a9fe3e4
At this point, you can delete your local copy of that test.txt file, then- use Git to retrieve, from the object database, either the first version you saved:
$ 'git cat-file -p' 83baae61804e65cc73a7201a7252750c76066a30 > test.txt
$ cat test.txt
#version 1
or the second version:
$ 'git cat-file -p' 1f7a7a472abf3dd9643fd615f6da379c4acb3e3a > test.txt
$ cat test.txt
#version 2
But remembering the SHA-1 key for each version of your file isnt practical; plus- you arent storing the filename in your
 system — just the content. This object type is called a blob. You can have Git tell you the
object type of any object in Git, given its SHA-1 key, with git cat-file -t:
$ 'git cat-file -t' 1f7a7a472abf3dd9643fd615f6da379c4acb3e3a
#blob

'Tree Objects';
The next type of Git object well examine is the tree, which solves the problem of storing the filename and also allows you to
 store a group of files together. Git stores content in a manner similar to a UNIX filesystem, but a bit simplified.
All the content is stored as tree and blob objects, with trees corresponding to UNIX directory entries and
 blobs corresponding more or less to inodes or file contents. A single tree object contains one or more entries,
each of which is the SHA-1 hash of a blob or subtree with its associated mode, type, and filename. For example, lets say you have a
 project where the most-recent tree looks something like:
$ 'git cat-file -p master^{tree}'
#100644 blob a906cb2a4a904a152e80877d4088654daad0c859      README
#100644 blob 8f94139338f9404f26296befa88755fc2598c289      Rakefile
#040000 tree 99f1a6d12cb4b6f19c8655fca46c3ecf317074e0      lib
The 'master^{tree}' syntax specifies the tree object that is pointed to by the last commit on your master branch.
 Notice that the lib subdirectory isnt a blob but a pointer to another tree:
$ 'git cat-file -p' 99f1a6d12cb4b6f19c8655fca46c3ecf317074e0
#100644 blob 47c6340d6459e05787f644c2447d2595f5d3a54b      simplegit.rb
Note- Depending on what shell you use, you may encounter errors when using the 'master^{tree}' syntax.
 In CMD on Windows, the ^ character is used for escaping, so you have to double it to avoid this:
# git cat-file -p master^^{tree}.
When using PowerShell, parameters using {} characters have to be quoted to avoid the parameter being parsed incorrectly:
# git cat-file -p 'master^{tree}'.
If- youre using ZSH, the ^ character is used for globbing, so you have to enclose the whole expression in quotes:
# git cat-file -p "master^{tree}".
You can fairly easily create your own tree. Git normally creates a tree by taking the state of your staging area or index and
 writing a series of tree objects from it. So, to create a tree object, you first have to set up an index by staging some files.
To create an index with a single entry — the first version of your test.txt file — you can use the
 plumbing command 'git update-index'. You use this command to artificially add the earlier version of the test.txt file to a
new staging area. You must pass it the '--add' option because the file doesnt yet exist in your staging area =you dont even have a
 staging area set up yet= and '--cacheinfo' because the file youre adding isnt in your directory but is in your database. Then-
you specify the mode, SHA-1, and filename:
$ 'git update-index --add --cacheinfo' '100644' 83baae61804e65cc73a7201a7252750c76066a30 test.txt
In this case, youre specifying a mode of '100644', which means its a 'normal' file.
 Other options are '100755', which means its an 'executable' file;
  and '120000', which specifies a 'symbolic link'. The mode is taken from normal UNIX modes but is much less flexible
 — these three modes are the only ones that are valid for files =blobs= in Git =although other modes are
 used for directories and submodules=.
Now- you can use 'git write-tree' to write the staging area out to a tree object. No '-w' option is needed — calling this
 command automatically creates a tree object from the state of the index if that tree doesnt yet exist:
$ 'git write-tree'
#d8329fc1cc938780ffdd9f94e0d364e0ea74f579
$ 'git cat-file -p' d8329fc1cc938780ffdd9f94e0d364e0ea74f579
#100644 blob 83baae61804e65cc73a7201a7252750c76066a30      test.txt
You can also verify that this is a tree object using the same git cat-file command you saw earlier:
$ 'git cat-file -t' d8329fc1cc938780ffdd9f94e0d364e0ea74f579
#tree
Youll now create a new tree with the second version of test.txt and a new file as well:
$ echo 'new file' > new.txt
$ 'git update-index --cacheinfo 100644' 1f7a7a472abf3dd9643fd615f6da379c4acb3e3a test.txt
$ 'git update-index --add' new.txt
Your staging area now has the new version of test.txt as well as the new file new.txt.
 Write out that tree =recording the state of the staging area or index to a tree object= and see what it looks like:
$ 'git write-tree'
#0155eb4229851634a0f03eb265b69f5a2d56f341
$ 'git cat-file -p' 0155eb4229851634a0f03eb265b69f5a2d56f341
#100644 blob fa49b077972391ad58037050f2a75f74e3671e92      new.txt
#100644 blob 1f7a7a472abf3dd9643fd615f6da379c4acb3e3a      test.txt
Notice that this tree has both file entries and also that the test.txt SHA-1 is the =version 2= SHA-1 from earlier =1f7a7a=.
 Just for fun, youll add the first tree as a subdirectory into this one. You can read trees into your staging area by
calling 'git read-tree'. In this case, you can read an existing tree into your staging area as a
 subtree by using the '--prefix' option with this command:
$ 'git read-tree --prefix'=bak d8329fc1cc938780ffdd9f94e0d364e0ea74f579
$ 'git write-tree'
#3c4e9cd789d88d8d89c1073707c3585e41b0e614
$ 'git cat-file -p' 3c4e9cd789d88d8d89c1073707c3585e41b0e614
#040000 tree d8329fc1cc938780ffdd9f94e0d364e0ea74f579      bak
#100644 blob fa49b077972391ad58037050f2a75f74e3671e92      new.txt
#100644 blob 1f7a7a472abf3dd9643fd615f6da379c4acb3e3a      test.txt
If- you created a working directory from the new tree you just wrote, you would get the two files in the top level of the
 working directory and a subdirectory named bak that contained the first version of the test.txt file.

'Commit Objects';
If- youve done- all of the above, you now have three trees that represent the different snapshots of your project that
 you want to track, but the earlier problem remains: you must remember all three SHA-1 values in order to recall the snapshots.
You also dont have any information about who saved the snapshots, when they were saved, or why they were saved.
 This is the basic information that the commit object stores for you.
To create a commit object, you call commit-tree and specify a single tree SHA-1 and which commit objects, if any, directly preceded it.
 Start with the first tree you wrote:
$ echo 'First commit' | 'git commit-tree' d8329f
#fdf4fc3344e67ab068f836878b6c4951e3b15f3d
Note- You will get a different hash value because of different creation time and author data. Moreover, while in principle any
 commit object can be reproduced precisely given that data, historical details of this books construction mean that the
printed commit hashes might not correspond to the given commits. Replace commit and tag hashes with your own checksums further in this chapter.
Now you can look at your new commit object with git cat-file:
$ 'git cat-file -p' fdf4fc3
#tree d8329fc1cc938780ffdd9f94e0d364e0ea74f579
#author Scott Chacon <schacon@gmail.com> 1243040974 -0700
#committer Scott Chacon <schacon@gmail.com> 1243040974 -0700
#
#First commit
The format for a commit object is simple: it specifies the top-level tree for the snapshot of the project at that point;
 the parent commits if any =the commit object described above does not have any parents=;
the author/committer information =which uses your user.name and user.email configuration settings and a timestamp=;
 a blank line, and then- the commit message.
Next- youll write the other two commit objects, each referencing the commit that came directly before it:
$ echo 'Second commit' | 'git commit-tree' 0155eb '-p' fdf4fc3
#cac0cab538b970a37ea1e769cbbde608743bc96d
$ echo 'Third commit'  | 'git commit-tree' 3c4e9c '-p' cac0cab
#1a410efbd13591db07496601ebc7a059dd55cfe9
Each of the three commit objects points to one of the three snapshot trees you created. Oddly enough, you have a
 real Git history now that you can view with the git log command, if you run it on the last commit SHA-1:
$ 'git log --stat' 1a410e
#commit 1a410efbd13591db07496601ebc7a059dd55cfe9
#Author: Scott Chacon <schacon@gmail.com>
#Date:   Fri May 22 18:15:24 2009 -0700
#
#	Third commit
#
# bak/test.txt | 1 +
# 1 file changed, 1 insertion(+)
#
#commit cac0cab538b970a37ea1e769cbbde608743bc96d
#Author: Scott Chacon <schacon@gmail.com>
#Date:   Fri May 22 18:14:29 2009 -0700
#
#	Second commit
#
# new.txt  | 1 +
# test.txt | 2 +-
# 2 files changed, 2 insertions(+), 1 deletion(-)
#
#commit fdf4fc3344e67ab068f836878b6c4951e3b15f3d
#Author: Scott Chacon <schacon@gmail.com>
#Date:   Fri May 22 18:09:34 2009 -0700
#
#    First commit
#
# test.txt | 1 +
# 1 file changed, 1 insertion(+)
Amazing- Youve just done- the low-level operations to build up a Git history without using any of the front end commands.
 This is essentially what Git does when you run the git add and git commit commands — it stores blobs for the
files that have changed, updates the index, writes out trees, and writes commit objects that reference the top-level trees and
 the commits that came immediately before them. These three main Git objects — the blob, the tree, and the
commit — are initially stored as separate files in your '.git/objects' directory. Here are all the objects in the
 example directory now, commented with what they store:
$ find .git/objects -type f
#.git/objects/01/55eb4229851634a0f03eb265b69f5a2d56f341 # tree 2
#.git/objects/1a/410efbd13591db07496601ebc7a059dd55cfe9 # commit 3
#.git/objects/1f/7a7a472abf3dd9643fd615f6da379c4acb3e3a # test.txt v2
#.git/objects/3c/4e9cd789d88d8d89c1073707c3585e41b0e614 # tree 3
#.git/objects/83/baae61804e65cc73a7201a7252750c76066a30 # test.txt v1
#.git/objects/ca/c0cab538b970a37ea1e769cbbde608743bc96d # commit 2
#.git/objects/d6/70460b4b4aece5915caf5c68d12f560a9fe3e4 # 'test content'
#.git/objects/d8/329fc1cc938780ffdd9f94e0d364e0ea74f579 # tree 1
#.git/objects/fa/49b077972391ad58037050f2a75f74e3671e92 # new.txt
#.git/objects/fd/f4fc3344e67ab068f836878b6c4951e3b15f3d # commit 1

'Object Storage';
We mentioned earlier that there is a header stored with every object you commit to your Git object database.
 Lets take a minute to see how Git stores its objects. Youll see how to store a blob object — in this case,
the string =what is up, doc?= — interactively in the Ruby scripting language.
You can start up interactive Ruby mode with the irb command:
$ irb
#>> content = "what is up, doc?"
#=> "what is up, doc?"
Git first constructs a header which starts by identifying the type of object — in this case, a blob. To that
 first part of the header, Git adds a space followed by the size in bytes of the content, and adding a final null byte:
#>> header = "blob #{content.bytesize}\0"
#=> "blob 16\u0000"
Git concatenates the header and the original content and then- calculates the SHA-1 checksum of that new content.
 You can calculate the SHA-1 value of a string in Ruby by including the SHA1 digest library with the require command and then- calling
#  Digest::SHA1.hexdigest() with the string:
#>> store = header + content
#=> "blob 16\u0000what is up, doc?"
#>> require 'digest/sha1'
#=> true
#>> sha1 = Digest::SHA1.hexdigest(store)
#=> "bd9dbf5aae1a3862dd1526723246b20206e5fc37"
Lets compare that to the output of git hash-object. Here we use echo -n to prevent adding a newline to the input.
$ echo -n "what is up, doc?" | 'git hash-object --stdin'
#bd9dbf5aae1a3862dd1526723246b20206e5fc37
Git compresses the new content with zlib, which you can do- in Ruby with the zlib library.
 First- you need to require the library and then- run
#  Zlib::Deflate.deflate() on the content:
#>> require 'zlib'
#=> true
#>> zlib_content = Zlib::Deflate.deflate(store)
#=> "x\x9CK\xCA\xC9OR04c(\xCFH,Q\xC8,V(-\xD0QH\xC9O\xB6\a\x00_\x1C\a\x9D"
Finally- youll write your zlib-deflated content to an object on disk. Youll determine the path of the object you want to
 write out =the first two characters of the SHA-1 value being the subdirectory name, and the last 38 characters being the
filename within that directory=. In Ruby, you can use the #FileUtils.mkdir_p() function to create the subdirectory if it
 doesnt exist. Then, open the file with #File.open() and write out the previously zlib-compressed content to the
file with a #write() call on the resulting file handle:
#>> path = '.git/objects/' + sha1[0,2] + '/' + sha1[2,38]
#=> ".git/objects/bd/9dbf5aae1a3862dd1526723246b20206e5fc37"
#>> require 'fileutils'
#=> true
#>> FileUtils.mkdir_p(File.dirname(path))
#=> ".git/objects/bd"
#>> File.open(path, 'w') { |f| f.write zlib_content }
#=> 32
Lets check the content of the object using git cat-file:
$ 'git cat-file -p' bd9dbf5aae1a3862dd1526723246b20206e5fc37
#what is up, doc?
Thats it – youve created a valid Git blob object.
All Git objects are stored the same way, just with different types –
 instead of the string blob, the header will begin with commit or tree. Also, although the blob content can be nearly anything,
the commit and tree content are very specifically formatted.

'https://git-scm.com/book/en/v2/Git-Internals-Git-References';
'Git References';
If- you were interested in seeing the history of your repository reachable from commit, say, 1a410e, you could run something like
 'git log' 1a410e to display that history, but you would still have to remember that 1a410e is the commit you want to
use as the starting point for that history. Instead, it would be easier if you had a file in which you could store that
 SHA-1 value under a simple name so you could use that simple name rather than the raw SHA-1 value.
In Git, these simple names are called =references= or =refs=; you can find the files that contain those SHA-1 values in the
 '.git/refs' directory. In the current project, this directory contains no files, but it does contain a simple structure:
$ find .git/refs
#.git/refs
#.git/refs/heads
#.git/refs/tags
$ find .git/refs -type f
To create a new reference that will help you remember where your latest commit is, you can technically do- something as simple as this:
$ echo 1a410efbd13591db07496601ebc7a059dd55cfe9 > '.git/refs/heads/master'
Now- you can use the head reference you just created instead of the SHA-1 value in your Git commands:
$ 'git log --pretty=oneline master'
#1a410efbd13591db07496601ebc7a059dd55cfe9 Third commit
#cac0cab538b970a37ea1e769cbbde608743bc96d Second commit
#fdf4fc3344e67ab068f836878b6c4951e3b15f3d First commit
You arent encouraged to directly edit the reference files; instead- Git provides the
 safer command 'git update-ref' to do- this if you want to update a reference:
$ 'git update-ref refs/heads/master' 1a410efbd13591db07496601ebc7a059dd55cfe9
Thats basically what a branch in Git is: a simple pointer or reference to the head of a line of work.
 To create a branch back at the second commit, you can do- this:
$ 'git update-ref refs/heads/test' cac0ca
Your branch will contain only work from that commit down:
$ 'git log --pretty=oneline' test
#cac0cab538b970a37ea1e769cbbde608743bc96d Second commit
#fdf4fc3344e67ab068f836878b6c4951e3b15f3d First commit
When you run commands like 'git branch' =branch=, Git basically runs that update-ref command to add the SHA-1 of the
 last commit of the branch youre on into whatever new reference you want to create.

'The HEAD';
The question now is, when you run 'git branch' =branch=, how does Git know the SHA-1 of the last commit? The answer is the HEAD file.
Usually the HEAD file is a symbolic reference to the branch youre currently on. By symbolic reference, we mean that
 unlike a normal reference, it contains a pointer to another reference.
However in some rare cases the HEAD file may contain the SHA-1 value of a Git object. This happens when you
 checkout a tag, commit, or remote branch, which puts your repository in "detached HEAD" state.
If- you look at the file, youll normally see something like this:
$ cat .git/HEAD
#ref: refs/heads/master
If- you run 'git checkout' test, Git updates the file to look like this:
$ cat .git/HEAD
#ref: refs/heads/test
When you run 'git commit', it creates the commit object, specifying the parent of that commit object to be
 whatever SHA-1 value the reference in HEAD points to.
You can also manually edit this file, but again a safer command exists to do- so:
 git symbolic-ref. You can read the value of your HEAD via this command:
$ 'git symbolic-ref HEAD'
#refs/heads/master
You can also set the value of HEAD using the same command:
$ 'git symbolic-ref HEAD' refs/heads/test
$ cat .git/HEAD
#ref: refs/heads/test
You cant set a symbolic reference outside of the refs style:
$ 'git symbolic-ref HEAD' test
#fatal: Refusing to point HEAD outside of refs/

'Tags';
We just finished discussing Gits three main object types =blobs, trees and commits=, but there is a fourth.
 The tag object is very much like a commit object — it contains a tagger, a date, a message, and a pointer.
The main difference is that a tag object generally points to a commit rather than a tree. Its like a branch reference, but
 it never moves — it always points to the same commit but gives it a friendlier name.
As discussed in Git Basics, there are two types of tags: annotated and lightweight.
 You can make a 'lightweight' tag by running something like this:
$ 'git update-ref' refs/tags/v1.0 cac0cab538b970a37ea1e769cbbde608743bc96d
That is all a lightweight tag is — a reference that never moves. An annotated tag is more complex, however. If
 you create an 'annotated' tag, Git creates a tag object and then- writes a reference to point to it rather than
directly to the commit. You can see this by creating an annotated tag =using the -a option=:
$ 'git tag -a' v1.1 1a410efbd13591db07496601ebc7a059dd55cfe9 -m 'Test tag'
Heres the object SHA-1 value it created:
$ cat .git/refs/tags/v1.1
#9585191f37f7b0fb9444f35a9bf50de191beadc2
Now- run git cat-file -p on that SHA-1 value:
$ 'git cat-file -p' 9585191f37f7b0fb9444f35a9bf50de191beadc2
#object 1a410efbd13591db07496601ebc7a059dd55cfe9
#type commit
#tag v1.1
#tagger Scott Chacon <schacon@gmail.com> Sat May 23 16:48:58 2009 -0700
#Test tag
Notice that the object entry points to the commit SHA-1 value that you tagged. Also notice that it doesnt need to
 point to a commit; you can tag any Git object. In the Git source code, for example, the maintainer has added their
GPG public key as a blob object and then- tagged it. You can view the public key by running this in a clone of the Git repository:
$ 'git cat-file blob' junio-gpg-pub
The Linux kernel repository also has a non-commit-pointing tag object — the
 first tag created points to the initial tree of the import of the source code.

'Remotes';
The third type of reference that youll see is a remote reference. If you add a remote and push to it, Git stores the
 value you last pushed to that remote for each branch in the 'refs/remotes' directory. For instance, you can add a
remote called origin and push your master branch to it:
$ 'git remote add' origin git@github.com:schacon/simplegit-progit.git
$ 'git push' origin master
#Counting objects: 11, done.
#Compressing objects: 100% (5/5), done.
#Writing objects: 100% (7/7), 716 bytes, done.
#Total 7 (delta 2), reused 4 (delta 1)
#To git@github.com:schacon/simplegit-progit.git
#  a11bef0..ca82a6d  master -> master
Then- you can see what the master branch on the origin remote was the last time you communicated with the server,
 by checking the refs/remotes/origin/master file:
$ cat .git/refs/remotes/origin/master
#ca82a6dff817ec66f44342007202690a93763949
Remote references differ from branches ='refs/heads' references= mainly in that theyre considered read-only.
 You can 'git checkout' to one, but Git wont symbolically reference HEAD to one, so youll never update it with a
commit command. Git manages them as bookmarks to the last known state of where those branches were on those servers.

'https://git-scm.com/book/en/v2/Git-Internals-Packfiles';
'Packfiles';
If- you followed all of the instructions in the example from the previous section, you should now have a
 test Git repository with 11 objects — four blobs, three trees, three commits, and one tag:
$ find .git/objects -type f
#.git/objects/01/55eb4229851634a0f03eb265b69f5a2d56f341 # tree 2
#.git/objects/1a/410efbd13591db07496601ebc7a059dd55cfe9 # commit 3
#.git/objects/1f/7a7a472abf3dd9643fd615f6da379c4acb3e3a # test.txt v2
#.git/objects/3c/4e9cd789d88d8d89c1073707c3585e41b0e614 # tree 3
#.git/objects/83/baae61804e65cc73a7201a7252750c76066a30 # test.txt v1
#.git/objects/95/85191f37f7b0fb9444f35a9bf50de191beadc2 # tag
#.git/objects/ca/c0cab538b970a37ea1e769cbbde608743bc96d # commit 2
#.git/objects/d6/70460b4b4aece5915caf5c68d12f560a9fe3e4 # 'test content'
#.git/objects/d8/329fc1cc938780ffdd9f94e0d364e0ea74f579 # tree 1
#.git/objects/fa/49b077972391ad58037050f2a75f74e3671e92 # new.txt
#.git/objects/fd/f4fc3344e67ab068f836878b6c4951e3b15f3d # commit 1
Git compresses the contents of these files with zlib, and youre not storing much, so all these files collectively take up
 only 925 bytes. Now youll add some more sizable content to the repository to demonstrate an interesting feature of Git.
To demonstrate, well add the repo.rb file from the Grit library — this is about a 22K source code file:
$ curl https://raw.githubusercontent.com/mojombo/grit/master/lib/grit/repo.rb > repo.rb
$ 'git checkout' master
$ 'git add' repo.rb
$ 'git commit -m' 'Create repo.rb'
#[master 484a592] Create repo.rb
# 3 files changed, 709 insertions(+), 2 deletions(-)
# delete mode 100644 bak/test.txt
# create mode 100644 repo.rb
# rewrite test.txt (100%)
If- you look at the resulting tree, you can see the SHA-1 value that was calculated for your new repo.rb blob object:
$ 'git cat-file -p' #master^{tree}
#100644 blob fa49b077972391ad58037050f2a75f74e3671e92      new.txt
#100644 blob 033b4468fa6b2a9547a70d88d1bbe8bf3f9ed0d5      repo.rb
#100644 blob e3f094f522629ae358806b17daf78246c27c007b      test.txt
You can then- use git cat-file to see how large that object is:
$ 'git cat-file -s' 033b4468fa6b2a9547a70d88d1bbe8bf3f9ed0d5
#22044
At this point, modify that file a little, and see what happens:
$ echo '# testing' >> repo.rb
$ 'git commit -am' 'Modify repo.rb a bit'
#[master 2431da6] Modify repo.rb a bit
# 1 file changed, 1 insertion(+)
Check the tree created by that last commit, and you see something interesting:
$ 'git cat-file -p' #master^{tree}
#100644 blob fa49b077972391ad58037050f2a75f74e3671e92      new.txt
#100644 blob b042a60ef7dff760008df33cee372b945b6e884e      repo.rb
#100644 blob e3f094f522629ae358806b17daf78246c27c007b      test.txt
The blob is now a different blob, which means that although you added only a single line to the end of a 400-line file,
 Git stored that new content as a completely new object:
$ 'git cat-file -s' b042a60ef7dff760008df33cee372b945b6e884e
#22054
You have two nearly identical 22K objects on your disk =each compressed to approximately 7K=. Wouldnt it be nice if
 Git could store one of them in full but then- the second object only as the delta between it and the first?
It turns out that it can. The initial format in which Git saves objects on disk is called a =loose= object format. However,
 occasionally Git packs up several of these objects into a single binary file called a =packfile= in order to save space and
be more efficient. Git does this if you have too many loose objects around, if you run the git gc command manually, or if
 you push to a remote server. To see what happens, you can manually ask Git to pack up the objects by calling the git gc command:
$ git gc
#Counting objects: 18, done.
#Delta compression using up to 8 threads.
#Compressing objects: 100% (14/14), done.
#Writing objects: 100% (18/18), done.
#Total 18 (delta 3), reused 0 (delta 0)
If- you look in your objects directory, youll find that most of your objects are gone, and a new pair of files has appeared:
$ find .git/objects -type f
#.git/objects/bd/9dbf5aae1a3862dd1526723246b20206e5fc37
#.git/objects/d6/70460b4b4aece5915caf5c68d12f560a9fe3e4
#.git/objects/info/packs
#.git/objects/pack/pack-978e03944f5c581011e6998cd0e9e30000905586.idx
#.git/objects/pack/pack-978e03944f5c581011e6998cd0e9e30000905586.pack
The objects that remain are the blobs that arent pointed to by any commit — in this case,
 the "what is up, doc?" example and the =test content= example blobs you created earlier. Because you never added them to
any commits, theyre considered dangling and arent packed up in your new packfile.
The other files are your new packfile and an index. The packfile is a single file containing the contents of all the
 objects that were removed from your filesystem. The index is a file that contains offsets into that packfile so you can
quickly seek to a specific object. What is cool is that although the objects on disk before you ran the gc command were
 collectively about 15K in size, the new packfile is only 7K. Youve cut your disk usage by half by packing your objects.
How does Git do- this? When Git packs objects, it looks for files that are named and sized similarly, and stores just the
 deltas from one version of the file to the next. You can look into the packfile and see what Git did to save space.
The git verify-pack plumbing command allows you to see what was packed up:
$ 'git verify-pack -v' .git/objects/pack/pack-978e03944f5c581011e6998cd0e9e30000905586.idx
#2431da676938450a4d72e260db3bf7b0f587bbc1 commit 223 155 12
#69bcdaff5328278ab1c0812ce0e07fa7d26a96d7 commit 214 152 167
#80d02664cb23ed55b226516648c7ad5d0a3deb90 commit 214 145 319
#43168a18b7613d1281e5560855a83eb8fde3d687 commit 213 146 464
#092917823486a802e94d727c820a9024e14a1fc2 commit 214 146 610
#702470739ce72005e2edff522fde85d52a65df9b commit 165 118 756
#d368d0ac0678cbe6cce505be58126d3526706e54 tag    130 122 874
#fe879577cb8cffcdf25441725141e310dd7d239b tree   136 136 996
#d8329fc1cc938780ffdd9f94e0d364e0ea74f579 tree   36 46 1132
#deef2e1b793907545e50a2ea2ddb5ba6c58c4506 tree   136 136 1178
#d982c7cb2c2a972ee391a85da481fc1f9127a01d tree   6 17 1314 1 \
#  deef2e1b793907545e50a2ea2ddb5ba6c58c4506
#3c4e9cd789d88d8d89c1073707c3585e41b0e614 tree   8 19 1331 1 \
#  deef2e1b793907545e50a2ea2ddb5ba6c58c4506
#0155eb4229851634a0f03eb265b69f5a2d56f341 tree   71 76 1350
#83baae61804e65cc73a7201a7252750c76066a30 blob   10 19 1426
#fa49b077972391ad58037050f2a75f74e3671e92 blob   9 18 1445
#b042a60ef7dff760008df33cee372b945b6e884e blob   22054 5799 1463
#033b4468fa6b2a9547a70d88d1bbe8bf3f9ed0d5 blob   9 20 7262 1 \
#  b042a60ef7dff760008df33cee372b945b6e884e
#1f7a7a472abf3dd9643fd615f6da379c4acb3e3a blob   10 19 7282
#non delta: 15 objects
#chain length = 1: 3 objects
#.git/objects/pack/pack-978e03944f5c581011e6998cd0e9e30000905586.pack: ok
Here- the 033b4 blob, which if you remember was the first version of your repo.rb file, is referencing the b042a blob,
 which was the second version of the file. The third column in the output is the size of the object in the pack, so you
can see that b042a takes up 22K of the file, but that 033b4 only takes up 9 bytes. What is also interesting is that the
 second version of the file is the one that is stored intact, whereas the original version is stored as a delta — this is
because youre most likely to need faster access to the most recent version of the file.
The really nice thing about this is that it can be repacked at any time. Git will occasionally repack your database automatically,
 always trying to save more space, but you can also manually repack at any time by running git gc by hand.

'https://git-scm.com/book/en/v2/Git-Internals-The-Refspec';
'The Refspec';
Throughout this book, weve used simple mappings from remote branches to local references, but they can be more complex.
 Suppose you were following along with the last couple sections and had created a
small local Git repository, and now wanted to add a remote to it:
$ 'git remote add' origin https://github.com/schacon/simplegit-progit
Running the command above adds a section to your repositorys '.git/config' file, specifying the name of the remote =origin=,
 the URL of the remote repository, and the refspec to be used for fetching:
#[remote "origin"]
#	url = https://github.com/schacon/simplegit-progit
#	fetch = +refs/heads/*:refs/remotes/origin/*
The format of the refspec is, first, an optional +, followed by 'src:dst', where =src= is the pattern for references on the
 remote side and =dst= is where those references will be tracked locally. The + tells Git to update the reference even if it isnt a fast-forward.
In the default case that is automatically written by a git remote add origin command, Git fetches all the
 references under 'refs/heads/' on the server and writes them to 'refs/remotes/origin/' locally. So, if there is a
master branch on the server, you can access the log of that branch locally via any of the following:
$ 'git log' origin/master
$ 'git log' remotes/origin/master
$ 'git log' refs/remotes/origin/master
Theyre all equivalent, because Git expands each of them to refs/remotes/origin/master.
If- you want Git instead to pull down only the master branch each time, and not every other branch on the remote server,
 you can change the fetch line to refer to that branch only:
#fetch = +refs/heads/master:refs/remotes/origin/master
This is just the default refspec for git fetch for that remote. If you want to do- a one-time only fetch, you can specify the
 specific refspec on the command line, too. To pull the master branch on the remote down to origin/mymaster locally, you can run:
$ 'git fetch' origin master:refs/remotes/origin/mymaster
You can also specify multiple refspecs. On the command line, you can pull down several branches like so:
$ 'git fetch' origin master:refs/remotes/origin/mymaster topic:refs/remotes/origin/topic
#From git@github.com:schacon/simplegit
# ! [rejected]        master     -> origin/mymaster  (non fast forward)
# * [new branch]      topic      -> origin/topic
In this case, the master branch pull was rejected because it wasnt listed as a fast-forward reference.
 You can override that by specifying the + in front of the refspec.
You can also specify multiple refspecs for fetching in your configuration file. If you want to always fetch the master and
 experiment branches from the origin remote, add two lines:
#[remote "origin"]
#	url = https://github.com/schacon/simplegit-progit
#	fetch = +refs/heads/master:refs/remotes/origin/master
#	fetch = +refs/heads/experiment:refs/remotes/origin/experiment
Since Git 2.6.0 you can use partial globs in the pattern to match multiple branches, so this works:
#fetch = +refs/heads/qa*:refs/remotes/origin/qa*
Even better, you can use namespaces =or directories= to accomplish the same with more structure. If you have a QA team that
 pushes a series of branches, and you want to get the master branch and any of the QA teams branches but nothing else,
you can use a config section like this:
#[remote "origin"]
#	url = https://github.com/schacon/simplegit-progit
#	fetch = +refs/heads/master:refs/remotes/origin/master
#	fetch = +refs/heads/qa/*:refs/remotes/origin/qa/*
If- you have a complex workflow process that has a QA team pushing branches, developers pushing branches, and
 integration teams pushing and collaborating on remote branches, you can namespace them easily this way.

'Pushing Refspecs';
Its nice that you can fetch namespaced references that way, but how does the QA team get their branches into
 a qa/ namespace in the first place? You accomplish that by using refspecs to push.
If- the QA team wants to push their master branch to qa/master on the remote server, they can run:
$ 'git push' origin master:refs/heads/qa/master
If- they want Git to do- that automatically each time they run git push origin, they can add a push value to their config file:
#[remote "origin"]
#	url = https://github.com/schacon/simplegit-progit
#	fetch = +refs/heads/*:refs/remotes/origin/*
#	push = refs/heads/master:refs/heads/qa/master
Again- this will cause a git push origin to push the local master branch to the remote qa/master branch by default.
Note- You cannot use the refspec to fetch from one repository and push to another one. For an example to do- so, refer to
 Keep your GitHub public repository up-to-date.
https://git-scm.com/book/en/v2/ch00/_fetch_and_push_on_different_repositories

'Deleting References';
You can also use the refspec to delete references from the remote server by running something like this:
$ 'git push' origin :topic
Because the refspec is 'src:dst', by leaving off the =src= part, this basically says to
 make the topic branch on the remote nothing, which deletes it.
Or you can use the newer syntax =available since Git v1.7.0=:
$ 'git push' origin '--delete' topic

'https://git-scm.com/book/en/v2/Git-Internals-Transfer-Protocols';
'Transfer Protocols';
Git can transfer data between two repositories in two major ways: the =dumb= protocol and the =smart= protocol.

'The Dumb Protocol';
If- youre setting up a repository to be served read-only over HTTP, the dumb protocol is likely what will be used.
 This protocol is called =dumb= because it requires no Git-specific code on the server side during the transport process;
the fetch process is a series of HTTP GET requests, where the client can assume the layout of the Git repository on the server.
Note- The dumb protocol is fairly rarely used these days. Its difficult to secure or make private,
 so most Git hosts =both cloud-based and on-premises= will refuse to use it. Its generally advised to use the smart protocol,
which we describe a bit further on.
Lets follow the http-fetch process for the simplegit library:
$ 'git clone' http://server/simplegit-progit.git
The first thing this command does is pull down the 'info/refs' file. This file is written by the 'update-server-info' command,
 which is why you need to enable that as a 'post-receive' hook in order for the HTTP transport to work properly:
#=> GET info/refs
#ca82a6dff817ec66f44342007202690a93763949     refs/heads/master
Now you have a list of the remote references and SHA-1s. Next, you look for what the HEAD reference is so you know what to
 check out when youre finished:
#=> GET HEAD
#ref: refs/heads/master
You need to check out the master branch when youve completed the process. At this point, youre ready to start the
 walking process. Because your starting point is the ca82a6 commit object you saw in the 'info/refs' file, you start by fetching that:
#=> GET objects/ca/82a6dff817ec66f44342007202690a93763949
#(179 bytes of binary data)
You get an object back – that object is in loose format on the server, and you fetched it over a static HTTP GET request.
 You can 'zlib-uncompress' it, strip off the header, and look at the commit content:
$ 'git cat-file -p' ca82a6dff817ec66f44342007202690a93763949
#tree cfda3bf379e4f8dba8717dee55aab78aef7f4daf
#parent 085bb3bcb608e1e8451d4b2432f8ecbe6306e7e7
#author Scott Chacon <schacon@gmail.com> 1205815931 -0700
#committer Scott Chacon <schacon@gmail.com> 1240030591 -0700
#Change version number
Next- you have two more objects to retrieve – cfda3b, which is the tree of content that the commit we just retrieved points to;
 and 085bb3, which is the parent commit:
#=> GET objects/08/5bb3bcb608e1e8451d4b2432f8ecbe6306e7e7
#(179 bytes of data)
That gives you your next commit object. Grab the tree object:
#=> GET objects/cf/da3bf379e4f8dba8717dee55aab78aef7f4daf
#(404 - Not Found)
Oops – it looks like that tree object isnt in loose format on the server, so you get a 404 response back. There are a
 couple of reasons for this – the object could be in an alternate repository, or it could be in a packfile in this repository.
Git checks for any listed alternates first:
#=> GET objects/info/http-alternates
#(empty file)
If- this comes back with a list of alternate URLs, Git checks for loose files and packfiles there – this is a nice mechanism for
 projects that are forks of one another to share objects on disk. However, because no alternates are listed in this case,
your object must be in a packfile. To see what packfiles are available on this server, you need to get the 'objects/info/packs' file,
 which contains a listing of them =also generated by update-server-info=:
#=> GET objects/info/packs
#P pack-816a9b2334da9953e530f27bcac22082a9f5b835.pack
There is only one packfile on the server, so your object is obviously in there, but youll check the index file to make sure.
 This is also useful if you have multiple packfiles on the server, so you can see which packfile contains the object you need:
#=> GET objects/pack/pack-816a9b2334da9953e530f27bcac22082a9f5b835.idx
#(4k of binary data)
Now that you have the packfile index, you can see if your object is in it – because the index lists the SHA-1s of the
 objects contained in the packfile and the offsets to those objects. Your object is there, so go ahead and get the whole packfile:
#=> GET objects/pack/pack-816a9b2334da9953e530f27bcac22082a9f5b835.pack
#(13k of binary data)
You have your tree object, so you continue walking your commits. Theyre all also within the packfile you just downloaded,
 so you dont have to do- any more requests to your server. Git checks out a working copy of the master branch that was
pointed to by the HEAD reference you downloaded at the beginning.

'The Smart Protocol';
The dumb protocol is simple but a bit inefficient, and it cant handle writing of data from the client to the server.
 The smart protocol is a more common method of transferring data, but it requires a process on the remote end that
is intelligent about Git – it can read local data, figure out what the client has and needs, and generate a
 custom packfile for it. There are two sets of processes for transferring data:
a pair for uploading data and a pair for downloading data.

'Uploading Data';
To upload data to a remote process, Git uses the send-pack and receive-pack processes. The send-pack process runs on the
 client and connects to a receive-pack process on the remote side.
#SSH
For- example, say you run git push origin master in your project, and origin is defined as a URL that uses the SSH protocol.
 Git fires up the send-pack process, which initiates a connection over SSH to your server. It tries to run a command on the
remote server via an SSH call that looks something like this:
$ ssh -x git@server "git-receive-pack 'simplegit-progit.git'"
#00a5ca82a6dff817ec66f4437202690a93763949 refs/heads/master□report-status \
#	delete-refs side-band-64k quiet ofs-delta \
#	agent=git/2:2.1.1+github-607-gfba4028 delete-refs
#0000
The 'git-receive-pack' command immediately responds with one line for each reference it currently has – in this case,
 just the master branch and its SHA-1. The first line also has a list of the
servers capabilities =here, report-status, delete-refs, and some others, including the client identifier=.
The data is transmitted in chunks. Each chunk starts with a 4-character hex value specifying how long the chunk is =including the
 4 bytes of the length itself=. Chunks usually contain a single line of data and a trailing linefeed.
Your first chunk starts with 00a5, which is hexadecimal for 165, meaning the chunk is 165 bytes long.
 The next chunk is 0000, meaning the server is done- with its references listing.
Now that it knows the servers state, your 'send-pack' process determines what commits it has that the server doesnt.
 For- each reference that this push will update, the send-pack process tells the 'receive-pack' process that information.
For- instance, if youre updating the master branch and adding an experiment branch, the send-pack response may look something like this:
#0076ca82a6dff817ec66f44342007202690a93763949 15027957951b64cf874c3557a0f3547bd83b3ff6 \
#	refs/heads/master report-status
#006c0000000000000000000000000000000000000000 cdfdb42577e2506715f8cfeacdbabc092bf63e8d \
#	refs/heads/experiment
#0000
Git sends a line for each reference youre updating with the lines length, the old SHA-1, the new SHA-1, and
 the reference that is being updated. The first line also has the clients capabilities. The SHA-1 value of
all 0s means that nothing was there before – because youre adding the experiment reference. If you were deleting a reference,
 you would see the opposite: all 0s on the right side.
Next- the client sends a packfile of all the objects the server doesnt have yet. Finally, the server responds with a
 success /or failure/ indication:
#000eunpack ok
#HTTP(S)
This process is mostly the same over HTTP, though the handshaking is a bit different. The connection is initiated with this request:
#=> GET http://server/simplegit-progit.git/info/refs?service=git-receive-pack
#001f# service=git-receive-pack
#00ab6c5f0e45abd7832bf23074a333f739977c9e8188 refs/heads/master□report-status \
#	delete-refs side-band-64k quiet ofs-delta \
#	agent=git/2:2.1.1~vmg-bitmaps-bugaloo-608-g116744e
#0000
Thats the end of the first client-server exchange. The client then- makes another request, this time a POST,
 with the data that send-pack provides.
#=> POST http://server/simplegit-progit.git/git-receive-pack
The POST request includes the send-pack output and the packfile as its payload. The server then- indicates success or
 failure with its HTTP response.
Keep in mind the HTTP protocol may further wrap this data inside a chunked transfer encoding.

'Downloading Data';
When you download data, the 'fetch-pack' and 'upload-pack' processes are involved. The client initiates a fetch-pack process that
 connects to an upload-pack process on the remote side to negotiate what data will be transferred down.
#SSH
If- youre doing the fetch over SSH, fetch-pack runs something like this:
$ ssh -x git@server "git-upload-pack 'simplegit-progit.git'"
After fetch-pack connects, upload-pack sends back something like this:
#00dfca82a6dff817ec66f44342007202690a93763949 HEAD□multi_ack thin-pack \
#	side-band side-band-64k ofs-delta shallow no-progress include-tag \
#	multi_ack_detailed symref=HEAD:refs/heads/master \
#	agent=git/2:2.1.1+github-607-gfba4028
#003fe2409a098dc3e53539a9028a94b6224db9d6a6b6 refs/heads/master
#0000
This is very similar to what receive-pack responds with, but the capabilities are different. In addition, it sends back
 what HEAD points to 'symref=HEAD:refs/heads/master' so the client knows what to check out if this is a clone.
At this point, the fetch-pack process looks at what objects it has and responds with the objects that it needs by
 sending /want/ and then- the SHA-1 it wants. It sends all the objects it already has with =have= and then- the SHA-1.
At the end of this list, it writes =done= to initiate the upload-pack process to begin sending the packfile of the data it needs:
#003cwant ca82a6dff817ec66f44342007202690a93763949 ofs-delta
#0032have 085bb3bcb608e1e8451d4b2432f8ecbe6306e7e7
#0009done
#0000
#HTTP(S)
The handshake for a fetch operation takes two HTTP requests. The first is a GET to the same endpoint used in the dumb protocol:
#=> GET $GIT_URL/info/refs?service=git-upload-pack
#001e# service=git-upload-pack
#00e7ca82a6dff817ec66f44342007202690a93763949 HEAD□multi_ack thin-pack \
#	side-band side-band-64k ofs-delta shallow no-progress include-tag \
#	multi_ack_detailed no-done symref=HEAD:refs/heads/master \
#	agent=git/2:2.1.1+github-607-gfba4028
#003fca82a6dff817ec66f44342007202690a93763949 refs/heads/master
#0000
This is very similar to invoking git-upload-pack over an SSH connection, but the second exchange is performed as a separate request:
#=> POST $GIT_URL/git-upload-pack HTTP/1.0
#0032want 0a53e9ddeaddad63ad106860237bbf53411d11a7
#0032have 441b40d833fdfa93eb2908e52742248faf0ee993
#0000
Again- this is the same format as above. The response to this request indicates 'success' or 'failure', and includes the packfile.

'Protocols Summary';
This section contains a very basic overview of the transfer protocols. The protocol includes many other features,
 such as 'multi_ack' or 'side-band' capabilities, but covering them is outside the scope of this book. Weve tried to
give you a sense of the general back-and-forth between client and server; if- you need more knowledge than this,
 youll probably want to take a look at the Git source code.

'https://git-scm.com/book/en/v2/Git-Internals-Maintenance-and-Data-Recovery';
'Maintenance and Data Recovery';
Occasionally- you may have to do- some cleanup – make a repository more compact, clean up an imported repository, or recover lost work.

'Maintenance';
Occasionally- Git automatically runs a command called =auto gc=. Most of the time, this command does nothing. However, if
 there are too many loose objects =objects not in a packfile= or too many packfiles, Git launches a full-fledged 'git gc' command.
The 'gc' stands for garbage collect, and the command does a number of things: it gathers up all the loose objects and
 places them in packfiles, it consolidates packfiles into one big packfile, and it removes objects that arent reachable from
any commit and are a few months old.
You can run auto gc manually as follows:
$ 'git gc --auto'
Again- this generally does nothing. You must have around 7,000 loose objects or more than 50 packfiles for Git to fire up a
 real gc command. You can modify these limits with the 'gc.auto' and 'gc.autopacklimit' config settings, respectively.
The other thing gc will do- is pack up your references into a single file. Suppose your repository contains the following branches and tags:
$ find '.git/refs' -type f
#.git/refs/heads/experiment
#.git/refs/heads/master
#.git/refs/tags/v1.0
#.git/refs/tags/v1.1
If- you run 'git gc', youll no longer have these files in the refs directory. Git will move them for the sake of efficiency into a
 file named '.git/packed-refs' that looks like this:
$ cat .git/packed-refs
# pack-refs with: peeled fully-peeled
#cac0cab538b970a37ea1e769cbbde608743bc96d refs/heads/experiment
#ab1afef80fac8e34258ff41fc1b867c702daa24b refs/heads/master
#cac0cab538b970a37ea1e769cbbde608743bc96d refs/tags/v1.0
#9585191f37f7b0fb9444f35a9bf50de191beadc2 refs/tags/v1.1
#^1a410efbd13591db07496601ebc7a059dd55cfe9
If- you update a reference, Git doesnt edit this file but instead writes a new file to 'refs/heads'. To get the
 appropriate SHA-1 for a given reference, Git checks for that reference in the 'refs' directory and then-
checks the 'packed-refs' file as a fallback. So if you cant find a reference in the refs directory, its probably in your packed-refs file.
Notice the last line of the file, which begins with a ^. This means the tag directly above is an annotated tag and
 that line is the commit that the annotated tag points to.

'Data Recovery';
At some point in your Git journey, you may accidentally lose a commit. Generally, this happens because you force-delete a
 branch that had work on it, and it turns out you wanted the branch after all; or you hard-reset a branch, thus
abandoning commits that you wanted something from. Assuming this happens, how can you get your commits back?
Heres an example that hard-resets the master branch in your test repository to an older commit and then- recovers the
 lost commits. First, lets review where your repository is at this point:
$ 'git log --pretty=oneline'
#ab1afef80fac8e34258ff41fc1b867c702daa24b Modify repo.rb a bit
#484a59275031909e19aadb7c92262719cfcdf19a Create repo.rb
#1a410efbd13591db07496601ebc7a059dd55cfe9 Third commit
#cac0cab538b970a37ea1e769cbbde608743bc96d Second commit
#fdf4fc3344e67ab068f836878b6c4951e3b15f3d First commit
Now- move the master branch back to the middle commit:
$ 'git reset --hard' 1a410efbd13591db07496601ebc7a059dd55cfe9
#HEAD is now at 1a410ef Third commit
$ 'git log --pretty=oneline'
#1a410efbd13591db07496601ebc7a059dd55cfe9 Third commit
#cac0cab538b970a37ea1e769cbbde608743bc96d Second commit
#fdf4fc3344e67ab068f836878b6c4951e3b15f3d First commit
Youve effectively lost the top two commits – you have no branch from which those commits are reachable. You need to find the
 latest commit SHA-1 and then- add a branch that points to it. The trick is finding that latest commit SHA-1 –
its not like youve memorized it, right?
Often- the quickest way is to use a tool called 'git reflog'. As youre working, Git silently records what your HEAD is
 every time you change it. Each time you commit or change branches, the reflog is updated. The reflog is also updated by the
'git update-ref' command, which is another reason to use it instead of just writing the SHA-1 value to your ref files,
 as we covered in Git References. You can see where youve been at any time by running git reflog:
$ 'git reflog'
#1a410ef HEAD@{0}: reset: moving to 1a410ef
#ab1afef HEAD@{1}: commit: Modify repo.rb a bit
#484a592 HEAD@{2}: commit: Create repo.rb
Here we can see the two commits that we have had checked out, however there is not much information here. To see the
 same information in a much more useful way, we can run 'git log -g', which will give you a normal log output for your reflog.
$ 'git log -g'
#commit 1a410efbd13591db07496601ebc7a059dd55cfe9
#Reflog: HEAD@{0} (Scott Chacon <schacon@gmail.com>)
#Reflog message: updating HEAD
#Author: Scott Chacon <schacon@gmail.com>
#Date:   Fri May 22 18:22:37 2009 -0700
#		Third commit
#commit ab1afef80fac8e34258ff41fc1b867c702daa24b
#Reflog: HEAD@{1} (Scott Chacon <schacon@gmail.com>)
#Reflog message: updating HEAD
#Author: Scott Chacon <schacon@gmail.com>
#Date:   Fri May 22 18:15:24 2009 -0700
#       Modify repo.rb a bit
It looks like the bottom commit is the one you lost, so you can recover it by creating a new branch at that commit.
 For- example, you can start a branch named recover-branch at that commit /ab1afef/:
$ 'git branch' recover-branch ab1afef
$ 'git log --pretty=oneline' recover-branch
#ab1afef80fac8e34258ff41fc1b867c702daa24b Modify repo.rb a bit
#484a59275031909e19aadb7c92262719cfcdf19a Create repo.rb
#1a410efbd13591db07496601ebc7a059dd55cfe9 Third commit
#cac0cab538b970a37ea1e769cbbde608743bc96d Second commit
#fdf4fc3344e67ab068f836878b6c4951e3b15f3d First commit
Cool – now you have a branch named recover-branch that is where your master branch used to be, making the
 first two commits reachable again. Next, suppose your loss was for some reason not in the reflog – you can simulate that
by removing recover-branch and deleting the reflog. Now the first two commits arent reachable by anything:
$ 'git branch -D' recover-branch
$ rm -Rf .git/logs/
Because the reflog data is kept in the '.git/logs/' directory, you effectively have no reflog. How can you recover that
 commit at this point? One way is to use the 'git fsck' utility, which checks your database for integrity. If you run it
with the --full option, it shows you all objects that arent pointed to by another object:
$ 'git fsck --full'
#Checking object directories: 100% (256/256), done.
#Checking objects: 100% (18/18), done.
#dangling blob d670460b4b4aece5915caf5c68d12f560a9fe3e4
#dangling commit ab1afef80fac8e34258ff41fc1b867c702daa24b
#dangling tree aea790b9a58f6cf6f2804eeac9f0abbe9631e4c9
#dangling blob 7108f7ecb345ee9d0084193f147cdad4d2998293
In this case, you can see your missing commit after the string =dangling commit=. You can recover it the same way,
 by adding a branch that points to that SHA-1.

'Removing Objects';
There are a lot of great things about Git, but one feature that can cause issues is the fact that a 'git clone' downloads the
 entire history of the project, including every version of every file. This is fine if the whole thing is source code,
because Git is highly optimized to compress that data efficiently. However, if someone at any point in the history of
 your project added a single huge file, every clone for all time will be forced to download that large file, even if it
was removed from the project in the very next commit. Because its reachable from the history, it will always be there.
This can be a huge problem when youre converting Subversion or Perforce repositories into Git. Because you dont download the
 whole history in those systems, this type of addition carries few consequences. If you did an import from another system or
otherwise find that your repository is much larger than it should be, here is how you can find and remove large objects.
Be warned: this technique is destructive to your commit history. It rewrites every commit object since the
 earliest tree you have to modify to remove a large file reference. If you do- this immediately after an import, before
anyone has started to base work on the commit, youre fine – otherwise, you have to notify all contributors that
 they must rebase their work onto your new commits.
To demonstrate, youll add a large file into your test repository, remove it in the next commit, find it, and
 remove it permanently from the repository. First, add a large object to your history:
$ curl -L https://www.kernel.org/pub/software/scm/git/git-2.1.0.tar.gz > git.tgz
$ 'git add' git.tgz
$ 'git commit -m' 'Add git tarball'
#[master 7b30847] Add git tarball
# 1 file changed, 0 insertions(+), 0 deletions(-)
# create mode 100644 git.tgz
Oops – you didnt want to add a huge tarball to your project. Better get rid of it:
$ 'git rm' git.tgz
#rm 'git.tgz'
$ 'git commit -m' 'Oops - remove large tarball'
#[master dadf725] Oops - remove large tarball
# 1 file changed, 0 insertions(+), 0 deletions(-)
# delete mode 100644 git.tgz
Now- gc your database and see how much space youre using:
$ 'git gc'
#Counting objects: 17, done.
#Delta compression using up to 8 threads.
#Compressing objects: 100% (13/13), done.
#Writing objects: 100% (17/17), done.
#Total 17 (delta 1), reused 10 (delta 0)
You can run the 'count-objects' command to quickly see how much space youre using:
$ 'git count-objects -v'
#count: 7
#size: 32
#in-pack: 17
#packs: 1
#size-pack: 4868
#prune-packable: 0
#garbage: 0
#size-garbage: 0
The 'size-pack' entry is the size of your packfiles in kilobytes, so youre using almost 5MB. Before the last commit,
 you were using closer to 2K – clearly, removing the file from the previous commit didnt remove it from your history.
Every time anyone clones this repository, they will have to clone all 5MB just to get this tiny project, because you
 accidentally added a big file. Lets get rid of it.
First you have to find it. In this case, you already know what file it is. But suppose you didnt;
 how would you identify what file or files were taking up so much space? If you run 'git gc', all the objects are in a packfile;
you can identify the big objects by running another plumbing command called 'git verify-pack' and sorting on the
 third field in the output, which is file size. You can also pipe it through the tail command because youre only interested in the
last few largest files:
$ 'git verify-pack -v' .git/objects/pack/pack-29…69.idx | sort -k 3 -n | tail -3
#dadf7258d699da2c8d89b09ef6670edb7d5f91b4 commit 229 159 12
#033b4468fa6b2a9547a70d88d1bbe8bf3f9ed0d5 blob   22044 5792 4977696
#82c99a3e86bb1267b236a4b6eff7868d97489af1 blob   4975916 4976258 1438
The big object is at the bottom: 5MB. To find out what file it is, youll use the 'rev-list' command,
 which you used briefly in Enforcing a Specific Commit-Message Format. If you pass '--objects' to rev-list,
it lists all the commit SHA-1s and also the blob SHA-1s with the file paths associated with them.
 You can use this to find your blobs name:
$ 'git rev-list --objects --all' | grep 82c99a3
#82c99a3e86bb1267b236a4b6eff7868d97489af1 git.tgz
Now- you need to remove this file from all trees in your past. You can easily see what commits modified this file:
$ 'git log --oneline --branches --' git.tgz
#dadf725 Oops - remove large tarball
#7b30847 Add git tarball
You must rewrite all the commits downstream from 7b30847 to fully remove this file from your Git history.
 To do- so, you use 'filter-branch', which you used in Rewriting History:
$ 'git filter-branch --index-filter' 'git rm --ignore-unmatch --cached git.tgz' -- 7b30847^..
#Rewrite 7b30847d080183a1ab7d18fb202473b3096e9f34 (1/2)rm 'git.tgz'
#Rewrite dadf7258d699da2c8d89b09ef6670edb7d5f91b4 (2/2)
#Ref 'refs/heads/master' was rewritten
The '--index-filter' option is similar to the '--tree-filter' option used in Rewriting History, except that instead of passing a
 command that modifies files checked out on disk, youre modifying your staging area or index each time.
Rather than remove a specific file with something like rm file, you have to remove it with 'git rm --cached' – you must
 remove it from the index, not from disk. The reason to do- it this way is speed – because Git doesnt have to check out
each revision to disk before running your filter, the process can be much, much faster. You can accomplish the
 same task with '--tree-filter' if you want. The '--ignore-unmatch' option to 'git rm' tells it not to error out if the
pattern youre trying to remove isnt there. Finally, you ask 'filter-branch' to rewrite your history only from the 7b30847 commit up,
 because you know that is where this problem started. Otherwise, it will start from the beginning and will unnecessarily take longer.
Your history no longer contains a reference to that file. However, your 'reflog' and a new set of 'refs' that Git added when you
 did the filter-branch under .git/refs/original still do, so you have to remove them and then- repack the database.
You need to get rid of anything that has a pointer to those old commits before you repack:
$ rm -Rf .git/refs/original
$ rm -Rf .git/logs/
$ 'git gc'
#Counting objects: 15, done.
#Delta compression using up to 8 threads.
#Compressing objects: 100% (11/11), done.
#Writing objects: 100% (15/15), done.
#Total 15 (delta 1), reused 12 (delta 0)
Lets see how much space you saved.
$ 'git count-objects -v'
#count: 11
#size: 4904
#in-pack: 15
#packs: 1
#size-pack: 8
#prune-packable: 0
#garbage: 0
#size-garbage: 0
The packed repository size is down to 8K, which is much better than 5MB. You can see from the size value that the
 big object is still in your loose objects, so its not gone; but it wont be transferred on a push or subsequent clone,
which is what is important.
If- you really wanted to, you could remove the object completely by running 'git prune' with the '--expire' option:
$ 'git prune --expire now'
$ 'git count-objects -v'
#count: 0
#size: 0
#in-pack: 15
#packs: 1
#size-pack: 8
#prune-packable: 0
#garbage: 0
#size-garbage: 0

'https://git-scm.com/book/en/v2/Git-Internals-Environment-Variables';
'Environment Variables';
Git always runs inside a bash shell, and uses a number of shell environment variables to determine how it behaves. Occasionally,
 it comes in handy to know what these are, and how they can be used to make Git behave the way you want it to. This isnt an
exhaustive list of all the environment variables Git pays attention to, but well cover the most useful.

'Global Behavior';
Some of Gits general behavior as a computer program depends on environment variables.
'GIT_EXEC_PATH' determines where Git looks for its sub-programs =like 'git-commit', 'git-diff', and others=.
 You can check the current setting by running 'git --exec-path'.
'HOME' isnt usually considered customizable =too many other things depend on it=, but its where Git looks for the
 global configuration file. If you want a truly portable Git installation, complete with global configuration, you can
override HOME in the portable Gits shell profile.
'PREFIX' is similar, but for the system-wide configuration. Git looks for this file at "$PREFIX/etc/gitconfig".
'GIT_CONFIG_NOSYSTEM' if set, disables the use of the system-wide configuration file. This is useful if your
 system config is interfering with your commands, but you dont have access to change or remove it.
'GIT_PAGER' controls the program used to display multi-page output on the command line.
 If- this is unset, 'PAGER' will be used as a fallback.
'GIT_EDITOR' is the editor Git will launch when the user needs to edit some text =a commit message, for example=.
 If- unset, 'EDITOR' will be used.

'Repository Locations';
Git uses several environment variables to determine how it interfaces with the current repository.
'GIT_DIR' is the location of the '.git' folder. If this isnt specified, Git walks up the directory tree until it
 gets to ~ or /, looking for a .git directory at every step.
'GIT_CEILING_DIRECTORIES' controls the behavior of searching for a .git directory. If you access directories that are
 slow to load =such as those on a tape drive, or across a slow network connection=, you may want to have Git stop
trying earlier than it might otherwise, especially if Git is invoked when building your shell prompt.
'GIT_WORK_TREE' is the location of the root of the working directory for a non-bare repository. If '--git-dir' or GIT_DIR is
 specified but none of '--work-tree', GIT_WORK_TREE or 'core.worktree' is specified, the current working directory is regarded as the
top level of your working tree.
'GIT_INDEX_FILE' is the path to the index file =non-bare repositories only=.
'GIT_OBJECT_DIRECTORY' can be used to specify the location of the directory that usually resides at '.git/objects'.
'GIT_ALTERNATE_OBJECT_DIRECTORIES' is a colon-separated list =formatted like /dir/one:/dir/two:…= which tells Git where to
 check for objects if they arent in GIT_OBJECT_DIRECTORY. If you happen to have a lot of projects with large files that
have the exact same contents, this can be used to avoid storing too many copies of them.

'Pathspecs';
A /pathspec/ refers to how you specify paths to things in Git, including the use of wildcards.
 These are used in the '.gitignore' file, but also on the command-line 'git add *.c'.
'GIT_GLOB_PATHSPECS' and 'GIT_NOGLOB_PATHSPECS' control the default behavior of wildcards in pathspecs.
 If- 'GIT_GLOB_PATHSPECS' is set to 1, wildcard characters act as wildcards =which is the default=;
 if- 'GIT_NOGLOB_PATHSPECS' is set to 1, wildcard characters only match themselves, meaning something like '*.c' would
only match a file named =\*.c=, rather than any file whose name ends with .c. You can override this in individual cases by
 starting the pathspec with :/glob/ or :/literal/, as in :/glob/\*.c.
'GIT_LITERAL_PATHSPECS' disables both of the above behaviors; no wildcard characters will work,
 and the override prefixes are disabled as well.
'GIT_ICASE_PATHSPECS' sets all pathspecs to work in a case-insensitive manner.

'Committing';
The final creation of a Git commit object is usually done- by 'git-commit-tree', which uses these environment variables as
 its primary source of information, falling back to configuration values only if these arent present.
'GIT_AUTHOR_NAME' is the human-readable name in the =author= field.
'GIT_AUTHOR_EMAIL' is the email for the =author= field.
'GIT_AUTHOR_DATE' is the timestamp used for the =author= field.
'GIT_COMMITTER_NAME' sets the human name for the =committer= field.
'GIT_COMMITTER_EMAIL' is the email address for the =committer= field.
'GIT_COMMITTER_DATE' is used for the timestamp in the =committer= field.
'EMAIL' is the fallback email address in case the user.email configuration value isnt set. If this isnt set,
 Git falls back to the system user and host names.

'Networking';
Git uses the curl library to do- network operations over HTTP, so 'GIT_CURL_VERBOSE' tells Git to emit all the messages generated by
 that library. This is similar to doing curl -v on the command line.
'GIT_SSL_NO_VERIFY' tells Git not to verify SSL certificates. This can sometimes be necessary if youre using a
 self-signed certificate to serve Git repositories over HTTPS, or youre in the middle of setting up a Git server but
havent installed a full certificate yet.
If- the data rate of an HTTP operation is lower than 'GIT_HTTP_LOW_SPEED_LIMIT' bytes per second for longer than
 'GIT_HTTP_LOW_SPEED_TIME' seconds, Git will abort that operation. These values override the 'http.lowSpeedLimit' and
'http.lowSpeedTime' configuration values.
'GIT_HTTP_USER_AGENT' sets the user-agent string used by Git when communicating over HTTP. The default is a value like git/2.0.0.

'Diffing and Merging';
'GIT_DIFF_OPTS' is a bit of a misnomer. The only valid values are '-u<n>' or '--unified=<n>', which controls the
 number of context lines shown in a 'git diff' command.
'GIT_EXTERNAL_DIFF' is used as an override for the 'diff.external' configuration value. If its set, Git will invoke this
 program when git diff is invoked.
'GIT_DIFF_PATH_COUNTER' and 'GIT_DIFF_PATH_TOTAL' are useful from inside the program specified by
 'GIT_EXTERNAL_DIFF' or 'diff.external'. The former represents which file in a series is being diffed =starting with 1=, and
the latter is the total number of files in the batch.
'GIT_MERGE_VERBOSITY' controls the output for the recursive merge strategy. The allowed values are as follows:
#0 outputs nothing, except possibly a single error message.
#1 shows only conflicts.
#2 also shows file changes.
#3 shows when files are skipped because they haven’t changed.
#4 shows all paths as they are processed.
#5 and above show detailed debugging information.
The default value is 2.

'Debugging';
Want to really know what Git is up to? Git has a fairly complete set of traces embedded, and all you need to do- is turn them on.
 The possible values of these variables are as follows:
'true' '1', or '2' – the trace category is written to stderr.
An absolute path starting with / – the trace output will be written to that file.
'GIT_TRACE' controls general traces, which dont fit into any specific category. This includes the expansion of aliases, and
 delegation to other sub-programs.
$ GIT_TRACE=true 'git lga'
'GIT_TRACE_PACK_ACCESS' controls tracing of packfile access. The first field is the packfile being accessed, the second is the
 offset within that file:
$ GIT_TRACE_PACK_ACCESS=true 'git status'
'GIT_TRACE_PACKET' enables packet-level tracing for network operations.
$ GIT_TRACE_PACKET=true 'git ls-remote' origin
'GIT_TRACE_PERFORMANCE' controls logging of performance data. The output shows how long each particular git invocation takes.
$ GIT_TRACE_PERFORMANCE=true 'git gc'
'GIT_TRACE_SETUP' shows information about what Git is discovering about the repository and environment its interacting with.
$ GIT_TRACE_SETUP=true 'git status'

'Miscellaneous';
'GIT_SSH' if specified, is a program that is invoked instead of ssh when Git tries to connect to an SSH host. It is invoked like
# $GIT_SSH [username@]host [-p <port>] <command>.
Note that this isnt the easiest way to customize how ssh is invoked; it wont support extra command-line parameters.
 To support extra command-line parameters, you can use GIT_SSH_COMMAND, write a wrapper script and set GIT_SSH to
point to it or use the "$HOME/.ssh/config" file.
'GIT_SSH_COMMAND' sets the SSH command used when Git tries to connect to an SSH host. The command is interpreted by the shell,
 and extra command-line arguments can be used with ssh, such as
GIT_SSH_COMMAND="ssh -i ~/.ssh/my_key" 'git clone' git@example.com:my/repo.
'GIT_ASKPASS' is an override for the core.askpass configuration value. This is the program invoked whenever Git needs to
 ask the user for credentials, which can expect a text prompt as a command-line argument, and should return the
answer on stdout =see Credential Storage for more on this subsystem=.
'GIT_NAMESPACE' controls access to namespaced refs, and is equivalent to the '--namespace' flag. This is mostly useful on the
 server side, where you may want to store multiple forks of a single repository in one repository, only keeping the refs separate.
'GIT_FLUSH' can be used to force Git to use non-buffered I/O when writing incrementally to stdout.
 A value of 1 causes Git to flush more often, a value of 0 causes all output to be buffered.
The default value =if this variable is not set= is to choose an appropriate buffering scheme depending on the activity and the output mode.
'GIT_REFLOG_ACTION' lets you specify the descriptive text written to the reflog. Heres an example:
$ GIT_REFLOG_ACTION="my action" 'git commit --allow-empty -m' 'My message'
#[master 9e3d55a] My message
$ git reflog -1
#9e3d55a HEAD@{0}: my action: My message
