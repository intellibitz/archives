#!/bin/bash

while true    
do
   ATIME=`stat -c %Z /path/to/the/file.txt`

   if [[ "$ATIME" != "$LTIME" ]]
   then    
       echo "RUN COMMAND"
       LTIME=$ATIME
   fi
   sleep 5
done
