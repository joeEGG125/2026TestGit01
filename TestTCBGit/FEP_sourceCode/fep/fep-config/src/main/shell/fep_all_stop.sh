#!/bin/bash

source ./fep_all_function.sh

if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
	echo "Warning! you must be user $optUser to stop all fep app..."
	exit 1
fi

#service
doActionWithFepApp fep-service-appmon stop
#gateway
doActionWithFepApp fep-gateway-atm-agent stop
doActionWithFepApp fep-gateway-atm stop
doActionWithFepApp fep-gateway-fisc stop
doActionWithFepApp fep-gateway-pos stop
#web
doActionWithFepApp fep-web stop
#server
doActionWithFepApp fep-server-atm stop
doActionWithFepApp fep-server-fisc stop
doActionWithFepApp fep-server-mb stop
doActionWithFepApp fep-server-hce stop
#batch
doActionWithFepApp fep-batch stop
#service
doActionWithFepApp fep-service-ems stop
