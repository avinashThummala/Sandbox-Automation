#!/bin/sh

/bin/echo "Your application has been deployed at http://code.mypuja.com:$1/" > deployFile.$1.$7

#Send over a mail with all the deployment details
/usr/bin/mutt -s "Deployment details" $3 < deployFile.$1.$7

rm deployFile.$1.$7

#Create a new directory with the play app
/usr/bin/svn export -r $7 --force file:///home/avinash/svnRepos/frepo/branches/$4_$5 $2 --username $4 --password $5

cd /home/avinash/$2

#run the newly created play app
/usr/share/play-framework/play run --http.port=$1 
