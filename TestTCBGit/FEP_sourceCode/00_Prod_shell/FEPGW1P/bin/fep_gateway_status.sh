#/bin/sh

HostName=`hostname`
optUserDir=/fep/fep-app
TimesTamp=`date +%Y-%m-%d" "%H:%M:%S`

gatewaySUT=`ps -ef | grep $optUserDir/fep-gateway-atm | grep -v grep | grep -v $optUserDir/fep-gateway-atm-agent | awk '{print $5" "$6}'`
gatewayAgentSUT=`ps -ef | grep $optUserDir/fep-gateway-atm-agent | grep -v grep | awk '{print $5" "$6}'`

gatewayPid=`ps -ef | grep $optUserDir/fep-gateway-atm | grep -v grep | grep -v $optUserDir/fep-gateway-atm-agent | awk '{print $2}'`
gatewayAgentPid=`ps -ef | grep $optUserDir/fep-gateway-atm-agent | grep -v grep | awk '{print $2}'`


echo "Current time: ${TimesTamp} Machine: ${HostName} Login ID: ${LOGIN}"
echo
if [[ -z $gatewayPid ]]; then
   echo FEP process:   fep-gateway-atm is not running!
else
   echo FEP process:   fep-gateway-atm is UP By: ${LOGIN} Uptime: ${gatewaySUT} PID:${gatewayPid}
fi
echo 
if [[ -z $gatewayAgentPid ]]; then
   echo FEP process:   fep-gateway-agent is not running!
else
   echo FEP process:   fep-gateway-agent is UP By: ${LOGIN} Uptime: ${gatewayAgentSUT} PID:${gatewayAgentPid}
fi
echo

