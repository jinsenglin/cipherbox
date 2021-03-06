#!/bin/bash -ex

# define local application home path
APP_HOME=$HOME/.cipherbox

# prompt “input plaintext file path”
echo -n "input plaintext file path: "
read file

# prompt “input key name”
echo -n "input key name: "
read key

# use openssl to encrypt the plaintext file
# save the ciphertext file to ~/.cipherbox/[plaintext file name].tmp
cat $file | openssl rsautl -encrypt  -pubin -inkey $APP_HOME/rsa-key-pairs/$key/$key.pub > $APP_HOME/$(basename $file).tmp

# call Google Drive Java Client “BackupConfidentialsToGoogleDrive” with argument “plaintext file name”
mvn -f $APP_HOME/src/google-drive-adaptor/pom.xml exec:java -Dexec.mainClass=tw.jim.cipherbox.BackupConfidentialsToGoogleDrive -Dexec.args="$(basename $file)"

# rm ~/.cipherbox/[plaintext file name].tmp
rm $APP_HOME/$(basename $file).tmp
