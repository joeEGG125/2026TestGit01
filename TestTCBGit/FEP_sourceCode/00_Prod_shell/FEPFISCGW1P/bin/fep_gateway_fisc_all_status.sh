#/bin/sh

HostName=`hostname`
optUserDir=/fep/fep-app
TimesTamp=`date +%Y-%m-%d" "%H:%M:%S`

fiscgwSUT=`ps -ef | grep $optUserDir/fep-gateway-fisc | grep -v grep | grep -v $optUserDir/fep-gateway-fisc-agent | awk '{print $5" "$6}'`
fiscgwAgentSUT=`ps -ef | grep $optUserDir/fep-gateway-fisc-agent | grep -v grep | awk '{print $5" "$6}'`


fiscgwPid=`ps -ef | grep $optUserDir/fep-gateway-fisc | grep -v grep | grep -v $optUserDir/fep-gateway-fisc-agent | awk '{print $2}'`
fiscgwAgentPid=`ps -ef | grep $optUserDir/fep-gateway-fisc-agent | grep -v grep | awk '{print $2}'`

echo "Current time: ${TimesTamp} Machine: ${HostName} Login ID: ${LOGIN}"
echo '=================== FEP Fisc Gateway ============================'
printf "|    Process Name        |  Up by  |     Uptime     |    PID    |\n"
echo '----------------------------------------------------------------'
if [[ -z $fiscgwPid ]]; then
   printf "| %-23s| is not running!\n" "fep-gateway-fisc"
else
   printf "| %-23s| %-8s| %-15s| %-10s|\n" "fep-gateway-fisc" "$LOGIN" "$fiscgwSUT" "$fiscgwPid"
fi
if [[ -z $fiscgwAgentPid ]]; then
   printf "| %-23s| is not running!\n" "fep-gateway-fisc-agent"
else
   printf "| %-23s| %-8s| %-15s| %-10s|\n" "fep-gateway-fisc-agent" "$LOGIN" "$fiscgwAgentSUT" "$fiscgwAgentPid"
fi
echo ''
echo '============= FEP FISC Gateway Connecting State ================='
echo ''
/fep/fep-app/fep-gateway-fisc-agent/fep_gw_fiscagentcmd.sh check 172.29.14.1
/fep/fep-app/fep-gateway-fisc-agent/fep_gw_fiscagentcmd.sh check 172.29.14.2