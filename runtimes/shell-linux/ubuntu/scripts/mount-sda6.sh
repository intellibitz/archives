#https://askubuntu.com/questions/46588/how-to-automount-ntfs-partitions
#http://askubuntu.com/questions/333287/external-hard-disk-read-only

sudo mount -o remount,uid=1000,gid=1000,rw /dev/sda6

#Try executing the following command in a terminal:

sudo mount -o remount,uid=1000,gid=1000,rw /dev/sdc1
#Explanation:

#-o means "with these options".
#remount - remounts the drive over the same mount point with the same previous options.
#uid=1000 - this option makes the user with id 1000 the owner of the drive. This is probably your username's id if you only have one username. If you have more than one username on your system, run the command id and use the number after uid=.
#gid=1000 - this option makes the group with id 1000 the group owner of the drive. Same notes as previous point.
#rw - this option mounts the drive as read/write. It was probably read/write anyways, but this is just to double check.
#Since you've already tried this command and it didn't work, let's try manually mounting the drive. Follow the below:

sudo umount /dev/sdc1
#This unmounts the drive.
sudo mkdir toshibaHDD
#This will create a new mount point.
sudo mount -o rw,uid=1000,gid=1000,user,exec,umask=003,blksize=4096 /dev/sdc1 /media/toshibaHDD
#user - permits any user to mount the drive
#exec - allows for execution of binaries on this drive. You can remove this option if you want.
#umask=003 - this will give rwxrwxr-- permissions to everything (directories and files) inside the drive. Alternatively, you can use dmask and fmask instead of umask to give separate permissions to directories and files (respectively).
#Now check the permissions of your drive.

#Edit
#Follow the steps to make it permanent:
#Unplug your external hard disk.
cd /etc
sudo cp fstab fstab.bak
#This takes a backup of the file we're about to edit.
sudo nano fstab
#This opens up the fstab file in a text editor.
#Move the blinking cursor to the end of the file, and paste the following two lines:
# line for mounting the external drive
UUID=D04A-0AE4   /media/toshibaHDD  exfat rw,uid=1000,gid=1000,user,exec,umask=003,blksize=4096   0   0
#Windows-Partition
UUID=<xxxxx> /media/win ntfs rw,auto,users,exec,nls=utf8,umask=003,gid=46,uid=1000    0   0

#Hit Ctrl+X, then Y, then Enter to save and close.
#That's it. Now, when you plug your external hard disk in, it will always have those options.

#The x flag is necessary for directories, in order to access their contents.
#With just the r flag on a directory, you can get a directory listing, but cannot access the files and subdirectories within it. With just the x flag on a directory, you won't see the directory listing, but may be able to access files and sub-directories if their permissions allow it and you can specify the exact name of the thing you're trying to access. So, in most cases, you have only two generally useful permissions choices for directories: r-x and rwx.
#So, since the umask mount option applies to both files and directories, and you don't want the x flag on files, you'll need to use fmask and dmask only, so you can place one set of permissions on files and another on directories.
#The permissions and the corresponding mask numbers:

#    rwx = mask number 0
#    rw- = mask number 1 (not very useful for directories)
#    r-x = mask number 2
#    r-- = mask number 3
#    -wx = mask number 4 (special case: an approximation of a "write-only directory")
#    -w- = mask number 5 (not very useful for directories)
#    --x = mask number 6 (for directories: access by known filenames only)
#    --- = mask number 7 (no access)
#
#If you want full access to directories, and everything except the x flag for files, you'll need 0 for the corresponding dmask number and 1 for the fmask number.
#For NTFS-3g mask numbers, the first digit will be always 0, to denote that the values are in octal numbers. The second digit will specify access for the user specified with the uid= option (or for the user doing the mounting, if not specified), the third digit will specify access for the group identified with the gid= option, and the last digit will specify access for everyone else.
#If this is your personal system and there are no other users who would need access to the NTFS filesystem, you could use the id command to identify your UID number, and then use mount options uid=<your UID here>,dmask=0077,fmask=0177. This would result all the files on the NTFS filesystem appearing as owned by you and with permissions -rw-------, and directories with drwx------.
#If there are other users who would need access to the NTFS filesystem, you could create a group for the NTFS access, add all the appropriate users to that group, then also specify the GID of that group in mount options: uid=<your UID here>,gid=<NTFS access group GID here>,dmask=0007,fmask=0117. This would give anyone in the group the same access as you have: files -rw-rw---- and directories drwxrwx---.
#Or you could keep the write access for yourself but give the group members read-only access: uid=<your UID here>,gid=<NTFS access group GID here>,dmask=0027,fmask=0137. This would result in permissions -rw-r----- for files and drwxr-x--- for directories.
#Or if you want to grant full access to multiple user accounts and read-only access to everyone else, then the mount options would be: uid=<your UID here>,gid=<NTFS access group GID here>,dmask=0002,fmask=0113. That would result in permissions -rw-rw-r-- for files and drwxrwxr-x for directories.