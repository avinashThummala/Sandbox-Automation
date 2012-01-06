#!/bin/bash

file="svnRepos/svnAccess"
matchString="[frepo:/trunk]"
newFile="svnRepos/.svnAccess_$3"
newString="$1 = r"


#To add a new user name and password for SVN access to SVNConf file
htpasswd -mb svnRepos/svnConf $1 $3

touch $newFile

#To modify the SVNAccess file
while read LINE
do
	echo $LINE >> $newFile

	if [ "$LINE" == "$matchString" ];
	then
		echo $newString >> $newFile
	fi

done < $file

addString1="[frepo:/branches/$1_$3]"
addString2="$1 = rw"

echo "" >> $newFile
echo $addString1 >> $newFile
echo $addString2 >> $newFile

mv $newFile $file

firstRepo="file:///home/avinash/svnRepos/frepo/trunk"
secondRepo="file:///home/avinash/svnRepos/frepo/branches/$1_$3/"

#To create a branch by using svn copy
svn copy $firstRepo $secondRepo -m "Creating a new branch" --username baggio --password thummala


echo "Hi $1," > mailMessage_$3
echo "" >> mailMessage_$3
echo "You have read only access to the trunk at http://code.mypuja.com/repos/frepo/trunk" >> mailMessage_$3
echo "You have read/write access to your dedicated branch at http://code.mypuja.com/repos/frepo/branches/$1_$3" >> mailMessage_$3
echo "Your SVN username and password for access in both cases are \"$1\" and \"$3\"" >> mailMessage_$3
echo "" >> mailMessage_$3
echo "Whenever you commit changes you made to your dedicated branch, we will automatically deploy your code on any available port at that point of time on our server and send you the details by mail." >> mailMessage_$3
echo "" >> mailMessage_$3
echo "Thanks," >> mailMessage_$3
echo "Mypuja team." >> mailMessage_$3

#To send over a mail with the SVN access details
mutt -s "SVN Access details" $2 < mailMessage_$3

rm mailMessage_$3

adityaMail="awatal@gmail.com"

#To notify Aditya about the new user name registration
#echo "The registered developer $1 has signed the NDA form" | mutt -s "New developer Info" $adityaMail
