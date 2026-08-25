#!/bin/sh

su - fepap1 "-c /fep/fep-app/bin/fep_function.sh Stop fep-gateway-atm-agent.jar"
su - fepap1 "-c /fep/fep-app/bin/fep_function.sh Stop fep-gateway-atm.jar"
