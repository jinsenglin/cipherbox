#!/bin/bash -ex

# define local application home path
APP_HOME=$HOME/.cipherbox

echo -n "Pick an existing cipherbox number: "
read cbnum

echo -n "Pick an existing random key number: "
read rknum

echo -n "Pick an existing plain text file: "
read ptfile

openssl rsautl -decrypt -inkey $APP_HOME/rsa-key-pairs/$cbnum/$cbnum.pem -in $APP_HOME/random-keys/$rknum/$rknum.bin.enc -out key.bin
openssl enc -aes-256-cbc -salt -in $ptfile -out largefile.enc -pass file:./key.bin
sleep 1 && rm key.bin
