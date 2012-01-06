#!/bin/sh

echo "Hi $1, Your password for SVN access is $2" | mutt -s "Access details" $3
