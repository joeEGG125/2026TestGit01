#!/bin/sh

optUser=fep
fepFunctionPath=/$optUser/fep-app/bin/fep_function.sh
$fepFunctionPath start fep-server-bizloan.jar 2048m 2048m '-javaagent:/opt/appdynamics/appagent/javaagent.jar -Dappdynamics.agent.applicationName=FEP-others -Dappdynamics.agent.tierName=fep-server-bizloan -Dappdynamics.agent.nodeName=fep-server-bizloan-2P -Dappdynamics.cron.vm=true -Dappdynamics.agent.conf.dir=/fep/appdynamics/fep-others' '' ''
