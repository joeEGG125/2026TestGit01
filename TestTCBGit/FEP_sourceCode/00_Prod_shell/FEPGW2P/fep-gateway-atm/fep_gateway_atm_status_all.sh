#!/bin/sh
HostName=`hostname`
TimesTamp=`date +%Y-%m-%d" "%H:%M:%S`

local appId=`ps -ef | grep /fep/fep-app/fep-gateway-atm/fep-gateway-atm | grep -v grep | grep -v bin/sh | awk '{print $2}'`
echo Current time: ${TimesTamp} Machine: ${HostName} Login ID: ${LOGIN}
echo
if [[ -z $appId ]]; then
   echo "The fep-gateway-atm is not running"
else
   java -Dfile.encoding=UTF-8 -cp /fep/fep-app/fep-gateway-atm/fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f clientlist -d atmStatus=$1 
fi

