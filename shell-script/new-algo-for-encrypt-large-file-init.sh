#!/bin/bash -ex

# define local application home path
APP_HOME=$HOME/.cipherbox

echo -n "Pick an existing cipherbox number: "
read cbnum

echo -n "How many random keys do you want to generate? [default: 10] "
read num
[ -z $num ] && num=10

for (( i=1; i <= $num; i++ ))
do
    keyhome=$APP_HOME/random-keys/$i
    keyuuid=`uuidgen`
    mkdir -p $keyhome
    echo Generating random key \#$i ...
    openssl rand -base64 128 -out $keyhome/$i.bin
    openssl rsautl -encrypt -inkey $APP_HOME/rsa-key-pairs/$cbnum/$cbnum.pub -pubin -in $keyhome/$i.bin -out $keyhome/$i.bin.enc
    rm $keyhome/$i.bin
done
