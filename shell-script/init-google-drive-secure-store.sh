#!/bin/bash -ex

# define local application home path
APP_HOME=$HOME/.cipherbox

# clone this application source code to local application home
[ -d $APP_HOME/src ] || git clone https://github.com/jinsenglin/cipherbox.git $APP_HOME/src

# build soure code
[ -d $APP_HOME/src/google-drive-adaptor/target ] || mvn package -f $APP_HOME/src/google-drive-adaptor

# put the client_secret.json file downloaded from Google Developer Console to local application home
mkdir -p $APP_HOME/google
if [ ! -f $APP_HOME/google/client_secret.json ]; then
	echo Please download your client_secret.json file from Google Developer Console, save it as $APP_HOME/google/client_secret.json, and then run this script again.
	exit 1
fi

# generate RSA key pairs
[ -f $APP_HOME/rsa-key-pairs.tgz ] ||
(
echo -n "How many key pairs do you want to generate? [default: 10] "
read num
[ -z $num ] && num=10

for (( i=1; i <= $num; i++ ))
do
	keyhome=$APP_HOME/rsa-key-pairs/$i
	keyuuid=`uuidgen`
	mkdir -p $keyhome
	echo Generating key pair \#$i ...
	openssl genrsa -aes256 -passout pass:$keyuuid -out $keyhome/$i.pem 4096
	openssl rsa -in $keyhome/$i.pem -passin pass:$keyuuid -pubout -out $keyhome/$i.pub
done

echo -n "Select the key pair from 1 to $num? [default: 1] "
read i
[ -z $i ] && i=1

	keyhome=$APP_HOME/rsa-key-pairs/$i
	openssl genrsa -aes256 -out $keyhome/$i.pem 4096
	openssl rsa -in $keyhome/$i.pem -pubout -out $keyhome/$i.pub

cd $APP_HOME
tar -czf rsa-key-pairs.tgz rsa-key-pairs
cd -
)

# init google drive
mvn -f $APP_HOME/src/google-drive-adaptor/pom.xml exec:java -Dexec.mainClass=tw.jim.cipherbox.InitGoogleDriveSecureStore
