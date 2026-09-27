#!/bin/sh
############################################################
# <!--
# /# #
 #  Copyright (C) IntelliBitz Technologies.,  Muthu Ramadoss
 #  168, Medavakkam Main Road, Madipakkam, Chennai 600091, Tamilnadu, India.
 #  http://www.intellibitz.com
 #  training@intellibitz.com
 #  +91 44 2247 5106
 # http://groups.google.com/group/etoe
 # http://sted.sourceforge.net
 #
 #  This program is free software; you can redistribute it and/or
 #  modify it under the terms of the GNU General Public License
 #  as published by the Free Software Foundation; either version 2
 #  of the License, or (at your option) any later version.
 #
 #  This program is distributed in the hope that it will be useful,
 #  but WITHOUT ANY WARRANTY; without even the implied warranty of
 #  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 #  GNU General Public License for more details.
 #
 #  You should have received a copy of the GNU General Public License
 #  along with this program; if not, write to the Free Software
 #  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 #
 #  STED, Copyright (C) 2007 IntelliBitz Technologies
 #  STED comes with ABSOLUTELY NO WARRANTY;
 #  This is free software, and you are welcome
 #  to redistribute it under the GNU GPL conditions;
 #
 #  Visit http://www.gnu.org/ for GPL License terms.
 # /
#-->

#<!--
# #
 #  $Id:sted.sh 55 2007-05-19 05:55:34Z sushmu $
 #  $HeadURL: svn+ssh://sushmu@svn.code.sf.net/p/sted/code/FontTransliterator/trunk/bin/sted.sh $
 # /
#-->
############################################################

# ---------------------------------------------------------------------
# Set the JAVA Installation Directory
# ---------------------------------------------------------------------
JAVA_HOME=/usr/java/jdk1.6.0

# ---------------------------------------------------------------------
# JAVA executable.. set from JAVA_HOME
# ---------------------------------------------------------------------
JAVA_EXE=$JAVA_HOME/bin/java
if [ -z "$JAVA_EXE" ] ; then
	echo ---------------------------------------------------------------------
	echo ERROR: JAVA_HOME invalid. Will use 'java' instead of the full path
	echo NOTE:  Please set JAVA_HOME in 'sted.sh' if 'java' not in path.
	echo ---------------------------------------------------------------------
	JAVA_EXE=java
fi

# ---------------------------------------------------------------------
# Before you run STED specify the location of the
# directory where STED is installed
# In most cases you do not need to change the settings below.
# ---------------------------------------------------------------------
STED_HOME=..

CLASS_PATH="$STED_HOME/bin/sted.jar:$STED_HOME/bin/sted-widgets.jar:$STED_HOME"
if [ -z "$CLASS_PATH" ] ; then
	echo ---------------------------------------------------------------------
	echo ERROR: cannot start STED - 'sted.jar' NOT FOUND.
	echo Please make sure 'sted.jar' & 'sted-widgets.jar' is in the 'bin' folder.
	echo ---------------------------------------------------------------------
fi

# ---------------------------------------------------------------------
# Set STED path to customize
# ---------------------------------------------------------------------
# path to user settings
STED_SETTINGS_PATH=$STED_HOME/settings
# path to images
STED_ICON_PATH=$STED_HOME/images
# path to resource
STED_RESOURCE_PATH=$STED_HOME/resource
# path to store log
STED_LOG_PATH=$STED_HOME/log

# ---------------------------------------------------------------------
# IF RUNNING CONSOLE.. SET CONSOLE DEPENDENT SYSTEM PROPERTIES
# ---------------------------------------------------------------------
STED_CONSOLE_JVM_ARGS="-Dfontmap.file=fontmap.xml -Dinput.file=input.txt -Doutput.file=output.txt"

# ---------------------------------------------------------------------
# You may specify your own JVM arguments in STED_JVM_ARGS variable.
# ---------------------------------------------------------------------
STED_JVM_ARGS="-Xms16m -Xmx64m -Dsun.java2d.nodraw=true"

# ---------------------------------------------------------------------
# NO NEED TO CHANGE ANYTHING BELOW
# ---------------------------------------------------------------------
JVM_ARGS="$STED_JVM_ARGS -Dsted.home.path=$STED_HOME -Dsted.settings.path=$STED_SETTINGS_PATH -Dsted.icon.path=$STED_ICON_PATH -Dsted.resource.path=$STED_RESOURCE_PATH -Dsted.log.path=$STED_LOG_PATH"

OLD_PATH=$PATH
PATH=$STED_HOME/bin:$PATH

# ---------------------------------------------------------------------
# STED Main Class
# ---------------------------------------------------------------------
MAIN_CLASS_NAME=intellibitz.sted.Main

# ---------------------------------------------------------------------
# Launch STED GUI
# ---------------------------------------------------------------------
"$JAVA_EXE" $JVM_ARGS -cp "$CLASS_PATH" $MAIN_CLASS_NAME $*

# ---------------------------------------------------------------------
# Launch STED Console
# ---------------------------------------------------------------------
#JVM_ARGS="$STED_CONSOLE_JVM_ARGS $JVM_ARGS"
#"$JAVA_EXE" $JVM_ARGS -cp "$CLASS_PATH" $MAIN_CLASS_NAME -c $*

PATH=$OLD_PATH
