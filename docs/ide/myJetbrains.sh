#

https://youtrack.jetbrains.com/articles/IDEA-A-2/Inotify-Watches-Limit-Linux

#For an intelligent IDE, it is essential to be in the know about any external changes in files it working with
# - e.g. changes made by VCS, or build tools, or code generators, etc. For that reason, the IntelliJ platform spins a
# background process to monitor such changes. The method it uses is platform-specific, and on Linux, it is the Inotify facility.

#Inotify requires a "watch handle" to be set for each directory in the project.
# Unfortunately, the default limit of watch handles may not be enough for reasonably sized projects, and
# reaching the limit will force IntelliJ platform to fall back to recursive scans of directory trees.

#To prevent this situation it is recommended to increase the watches limit (to, say, 512K):

#    Add the following line to either /etc/sysctl.conf file or a
#    new *.conf file (e.g. idea.conf) under /etc/sysctl.d/ directory:
    fs.inotify.max_user_watches = 524288
#    Then run this command to apply the change:
    sudo sysctl -p --system

#And don't forget to restart your IDE.

#Note: the watch limit is per-account setting. If there are other programs running under the
# same account that also use Inotify, the limit should be raised high enough to suit the needs of all of them.

#