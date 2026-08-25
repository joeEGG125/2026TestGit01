#!/bin/sh

su - fepap1 -c "/fep/fep-app/fep-gateway-atm/fep_gateway_atm_start.sh"
su - fepap1 -c "/fep/fep-app/bin/fep_function.sh start fep-gateway-atm-agent.jar"
