#!/bin/sh
HostName=`hostname`
TimesTamp=`date +%Y-%m-%d" "%H:%M:%S`
echo "Current time: ${TimesTamp} Machine: ${HostName}   Login ID: $(whoami)"
optUser=fep
fepFunctionPath=/$optUser/fep-app/bin/fep_function.sh
$fepFunctionPath status fep-gateway-fisc.jar
