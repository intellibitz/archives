# https://tomcat.apache.org/tomcat-10.1-doc/RUNNING.txt
#https://www.digitalocean.com/community/tutorials/how-to-install-apache-tomcat-10-on-ubuntu-20-04

#Download and Install a Java SE Runtime Environment (JRE)
#Download and Install Apache Tomcat

#Set CATALINA_HOME (required) and CATALINA_BASE (optional)
#The CATALINA_HOME environment variable should be set to the location of the
#root directory of the "binary" distribution of Tomcat.

#The Tomcat startup scripts have some logic to set this variable
#automatically if it is absent, based on the location of the startup script
#in *nix and on the current directory in Windows. That logic might not work
#in all circumstances, so setting the variable explicitly is recommended.

#The CATALINA_BASE environment variable specifies location of the root
#directory of the "active configuration" of Tomcat. It is optional. It
#defaults to be equal to CATALINA_HOME.

#Using distinct values for the CATALINA_HOME and CATALINA_BASE variables is
#recommended to simplify further upgrades and maintenance. It is documented
#in the "Multiple Tomcat Instances" section below.

#Set JRE_HOME or JAVA_HOME (required)
#These variables are used to specify location of a Java Runtime
#Environment or of a Java Development Kit that is used to start Tomcat.

#The JRE_HOME variable is used to specify location of a JRE. The JAVA_HOME
#variable is used to specify location of a JDK.

#Using JAVA_HOME provides access to certain additional startup options that
#are not allowed when JRE_HOME is used.

#If both JRE_HOME and JAVA_HOME are specified, JRE_HOME is used.

#The recommended place to specify these variables is a "setenv" script. See
#below.

#Another frequently used variable is CATALINA_PID (on *nix only). It
#specifies the location of the file where process id of the forked Tomcat
#java process will be written. This setting is optional. It will activate
#the following features:

# *  better protection against duplicate start attempts and
# *  allows forceful termination of Tomcat process when it does not react to
#    the standard shutdown command.

#Using the "setenv" script (optional, recommended)

#Apart from CATALINA_HOME and CATALINA_BASE, all environment variables can
#be specified in the "setenv" script. The script is placed either into
#CATALINA_BASE/bin or into CATALINA_HOME/bin directory and is named
#setenv.bat (on Windows) or setenv.sh (on *nix). The file has to be
#readable.

#By default the setenv script file is absent. If the script file is present
#both in CATALINA_BASE and in CATALINA_HOME, the one in CATALINA_BASE is
#preferred.

#On *nix, $CATALINA_BASE/bin/setenv.sh:

  JRE_HOME=/usr/java/latest
  CATALINA_PID="/run/tomcat.pid"

#The CATALINA_HOME and CATALINA_BASE variables cannot be configured in the
#setenv script, because they are used to locate that file.

#All the environment variables described here and the "setenv" script are
#used only if you use the standard scripts to launch Tomcat. For example, if
#you have installed Tomcat as a service on Windows, the service wrapper
#launches Java directly and does not use the script files.
