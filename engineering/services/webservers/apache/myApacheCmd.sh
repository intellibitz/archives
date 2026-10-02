#https://httpd.apache.org/docs/2.4/install.html

#Installing on Ubuntu/Debian
sudo apt install apache2
sudo service apache2 start
#Ubuntu
#The binary is called apache2 and is managed using systemd, so to start/stop the service use
# systemctl start apache2 and systemctl stop apache2, and use systemctl status apache2 and
# journalctl -u apache2 to check status. system and apache2ctl can also be used for
# service management if desired. Calling /usr/bin/apache2 directly will not work with the
# default configuration.

#https://httpd.apache.org/docs/2.4/invoking.html
#The first thing that httpd does when it is invoked is to locate and read the configuration
# file httpd.conf. The location of this file is set at compile-time, but it is possible to
# specify its location at run time using the -f command-line option as in
/usr/local/apache2/bin/apachectl -f /usr/local/apache2/conf/httpd.conf
#The apachectl script can operate in two modes. First, it can act as a simple front-end to the
# httpd command that simply sets any necessary environment variables and then invokes httpd, passing
# through any command line arguments. Second, apachectl can act as a SysV init script, taking
# simple one-word arguments like start, restart, and stop, and translating them into appropriate
# signals to httpd.

#Starting at Boot-Time
#If you want your server to continue running after a system reboot, you should add a call to
# apachectl to your system startup files (typically rc.local or a file in an rc.N directory).
# This will start Apache as root. Before doing this ensure that your server is properly
# configured for security and access restrictions.

#The apachectl script is designed to act like a standard SysV init script; it can take the
# arguments start, restart, and stop and translate them into the appropriate signals to httpd.
# So you can often simply link apachectl into the appropriate init directory. But be sure to
# check the exact requirements of your system.

#https://httpd.apache.org/docs/2.4/stopping.html
kill -TERM `cat /usr/local/apache2/logs/httpd.pid`
tail -f /usr/local/apache2/logs/error_log
apachectl -k stop
apachectl -k graceful
apachectl -k restart
apachectl -k graceful-stop
#apachectl is a front end to the Apache HyperText Transfer Protocol (HTTP) server. It is designed
# to help the administrator control the functioning of the Apache httpd daemon.

#httpd - Apache Hypertext Transfer Protocol Server
httpd [ -d serverroot ] [ -f config ] [ -C directive ] [ -c directive ] [ -D parameter ] [ -e level ] [ -E file ] [ -k start|restart|graceful|stop|graceful-stop ] [ -h ] [ -l ] [ -L ] [ -S ] [ -t ] [ -v ] [ -V ] [ -X ] [ -M ] [ -T ]

#https://httpd.apache.org/docs/2.4/howto/htaccess.html
#.htaccess files provide a way to make configuration changes on a per-directory basis.

#.htaccess files (or "distributed configuration files") provide a way to make configuration changes
# on a per-directory basis. A file, containing one or more configuration directives, is placed in a
# particular document directory, and the directives apply to that directory, and all subdirectories thereof.

#CGI example
#Finally, you may wish to use a .htaccess file to permit the execution of CGI programs in a
# particular directory. This may be implemented with the following configuration:

Options +ExecCGI
AddHandler cgi-script cgi pl

#Alternately, if you wish to have all files in the given directory be considered to be CGI programs,
# this may be done with the following configuration:

Options +ExecCGI
SetHandler cgi-script

#Note that AllowOverride Options and AllowOverride FileInfo must both be in effect for these
# directives to have any effect.

