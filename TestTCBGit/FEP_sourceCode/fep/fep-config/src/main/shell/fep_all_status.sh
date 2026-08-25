#!/bin/bash

source ./fep_all_function.sh

if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
	echo "Warning! you must be user $optUser to check all fep app status..."
	exit 1
fi

#service
doActionWithFepApp fep-service-appmon status
doActionWithFepApp fep-service-ems status
#batch
doActionWithFepApp fep-batch status
#server
doActionWithFepApp fep-server-atm status
doActionWithFepApp fep-server-fisc status
doActionWithFepApp fep-server-mb status
doActionWithFepApp fep-server-hce status
#web
doActionWithFepApp fep-web status
#gateway
doActionWithFepApp fep-gateway-atm-agent status
doActionWithFepApp fep-gateway-atm status
doActionWithFepApp fep-gateway-fisc status
doActionWithFepApp fep-gateway-pos status
