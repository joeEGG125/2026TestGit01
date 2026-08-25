#!/bin/sh

optUser=fep
fepFunctionPath=/$optUser/fep-app/bin/fep_function.sh
$fepFunctionPath start fep-server-mb.jar 2048m 2048m '-javaagent:/opt/appdynamics/appagent/javaagent.jar -Dappdynamics.agent.applicationName=FEP -Dappdynamics.agent.tierName=fep-server-mb -Dappdynamics.agent.nodeName=fep-server-mb-1P -Dappdynamics.cron.vm=true -Dappdynamics.agent.conf.dir=/fep/appdynamics/fep-server-mb' '' ''