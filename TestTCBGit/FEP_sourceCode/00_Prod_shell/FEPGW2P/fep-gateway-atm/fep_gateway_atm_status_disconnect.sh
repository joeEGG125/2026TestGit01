#!/bin/sh
HostName=`hostname`
TimesTamp=`date +%Y-%m-%d" "%H:%M:%S`
#atmGateway=`ps -ef | grep /fep/fep-app/fep-gateway-atm/fep-gateway-atm | grep -v grep | grep -v bin/sh | awk '{print $5" "$6}'`

local appId=`ps -ef | grep /fep/fep-app/fep-gateway-atm/fep-gateway-atm | grep -v grep | grep -v bin/sh | awk '{print $2}'`
echo Current time: ${TimesTamp} Machine: ${HostName} Login ID: ${LOGIN}
echo
if [[ -z $appId ]]; then
   echo "The fep-gateway-atm is not running"
else
#   echo FEP process:   fep-gateway-atm is UP By:${LOGIN} Uptime:${atmGateway} PID:${appId}  
   java -Dfile.encoding=UTF-8 -cp /fep/fep-app/fep-gateway-atm/fep-gateway-atm.jar com.syscom.fep.gateway.cmd.ATMGatewayCommand -f monitor -d action=get&listClient=true  
fi
