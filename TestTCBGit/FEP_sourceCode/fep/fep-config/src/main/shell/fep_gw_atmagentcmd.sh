#!/bin/bash

optUser=syscom
optUserDir=/home/$optUser
jarFile=$optUserDir/fep-app/fep-gateway-atm-agent/fep-gateway-atm-agent.jar
programName=com.syscom.fep.gateway.cmd.ATMGatewayAgentCommand

function usage() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  echo "Usage: $0 {changefepap (host)|resetfepap|checkfepap}"
  echo "Example: $0 changefepap 127.0.0.1"
  echo "Example: $0 resetfepap"
  echo "Example: $0 checkfepap"
  exit 1
}

function changefepap() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f changefepap -d "host=$1" -i "$2" -p "$3"
}

function checkfepap() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f checkfepap -i "$1" -p "$2"
}

case $1 in
changefepap)
  changefepap $2 $3 $4
  ;;

resetfepap)
  changefepap "" $2 $3
  ;;

checkfepap)
  checkfepap $2 $3
  ;;

*)
  usage
  ;;
esac
