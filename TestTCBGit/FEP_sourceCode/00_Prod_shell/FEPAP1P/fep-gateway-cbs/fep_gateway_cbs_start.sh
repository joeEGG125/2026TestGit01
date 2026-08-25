#!/bin/sh

optUser=fep
fepFunctionPath=/$optUser/fep-app/bin/fep_function.sh
$fepFunctionPath start fep-gateway-cbs.jar 4096m 4096m '-javaagent:/opt/appdynamics/appagent/javaagent.jar -Dappdynamics.agent.applicationName=FEP -Dappdynamics.agent.tierName=fep-gateway-cbs -Dappdynamics.agent.nodeName=fep-gateway-cbs-1P -Dappdynamics.cron.vm=true -Dappdynamics.agent.conf.dir=/fep/appdynamics/fep-gateway-cbs' '' ''