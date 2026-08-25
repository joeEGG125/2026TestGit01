#!/bin/sh

optUser=fep
fepFunctionPath=/$optUser/fep-app/bin/fep_function.sh
$fepFunctionPath start fep-server-hce.jar 2048m 2048m '-javaagent:/opt/appdynamics/appagent/javaagent.jar -Dappdynamics.agent.applicationName=FEP -Dappdynamics.agent.tierName=fep-server-hce -Dappdynamics.agent.nodeName=fep-server-hce-2P -Dappdynamics.cron.vm=true -Dappdynamics.agent.conf.dir=/fep/appdynamics/fep-server-hce' '' ''