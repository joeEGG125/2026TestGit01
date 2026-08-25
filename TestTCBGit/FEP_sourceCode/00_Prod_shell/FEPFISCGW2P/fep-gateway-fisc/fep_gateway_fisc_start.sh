#!/bin/sh

optUser=fep
fepFunctionPath=/$optUser/fep-app/bin/fep_function.sh
$fepFunctionPath start fep-gateway-fisc.jar '' '' '-javaagent:/opt/appdynamics/appagent/javaagent.jar -Dappdynamics.agent.applicationName=FEP -Dappdynamics.agent.tierName=fep-gateway-fisc -Dappdynamics.agent.nodeName=fep-gateway-fisc-2P -Dappdynamics.cron.vm=true -Dappdynamics.agent.conf.dir=/fep/appdynamics/fep-gateway-fisc'
