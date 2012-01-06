#!/bin/sh

#$1 repo path
#$2 revision number

dirName=$(basename $1)_$2

newRevUser=$(svnlook author /home/avinash/svnRepos/frepo)

#To find the next available non-verfied port to deply the new play framework
/usr/bin/java -cp $PATH:/home/avinash:/home/avinash/mysql-connector-java-5.1.18-bin.jar FPort $dirName $newRevUser $1 $2
