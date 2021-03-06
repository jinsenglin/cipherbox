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

# download RSA key pairs and metadata from Google Drive
[ -d $APP_HOME/rsa-key-pairs ] ||
(
mvn -f $APP_HOME/src/google-drive-adaptor/pom.xml exec:java -Dexec.mainClass=tw.jim.cipherbox.RebuildApplicationHome

cd $APP_HOME
tar -zxf rsa-key-pairs.tgz
cd -
)
