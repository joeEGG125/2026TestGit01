#!/bin/sh

HOSTNAME=`hostname`

case ${HOSTNAME} in
  "fepap1D")
    sudo -u wasadmin /fep/fep-web/restartFEPwasLiberty.sh
    ;;
# TEST
  "fepap1T")
    ALL_WAS_INSTANCES="web_server1a_t"
    sudo -u wasadmin /fep/fep-web/restartFEPwasLiberty.sh
    ;;
  "fepap2T")
    ALL_WAS_INSTANCES="web_server2a_t"
    sudo -u wasadmin /fep/fep-web/restartFEPwasLiberty.sh
    ;;
# PROD
  "fepap1P")
    ALL_WAS_INSTANCES="web_server1a_p"
    sudo -u wasadmin /fep/fep-web/restartFEPwasLiberty.sh
    ;;
  "fepap2P")
    ALL_WAS_INSTANCES="web_server2a_p"
    sudo -u wasadmin /fep/fep-web/restartFEPwasLiberty.sh
    ;;
esac
