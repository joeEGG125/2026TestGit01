#!/bin/bash

source ./fep_all_function.sh

if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
	echo "Warning! you must be user $optUser to start all fep app..."
	exit 1
fi

#service
doActionWithFepApp fep-service-ems start
#batch
doActionWithFepApp fep-batch start
#server
doActionWithFepApp fep-server-atm start
doActionWithFepApp fep-server-fisc start
doActionWithFepApp fep-server-mb start
doActionWithFepApp fep-server-hce start
#web
doActionWithFepApp fep-web start
#gateway
doActionWithFepApp fep-gateway-atm-agent start
doActionWithFepApp fep-gateway-atm start
doActionWithFepApp fep-gateway-fisc start
doActionWithFepApp fep-gateway-pos start
#service
doActionWithFepApp fep-service-appmon start