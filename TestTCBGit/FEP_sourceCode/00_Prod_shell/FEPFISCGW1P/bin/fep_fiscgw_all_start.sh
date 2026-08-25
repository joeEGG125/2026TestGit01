#!/bin/sh

su - fepap1 -c "/fep/fep-app/fep-gateway-fisc/fep_gateway_fisc_start.sh"
su - fepap1 -c "/fep/fep-app/bin/fep_function.sh start fep-gateway-fisc-agent.jar"