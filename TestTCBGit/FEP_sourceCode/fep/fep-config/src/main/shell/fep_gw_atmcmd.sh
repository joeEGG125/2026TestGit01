#!/bin/bash

optUser=syscom
optUserDir=/home/$optUser
jarFile=$optUserDir/fep-app/fep-gateway-atm/fep-gateway-atm.jar
programName=com.syscom.fep.gateway.cmd.ATMGatewayCommand

function usage() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  echo "Usage: $0 {ssllist|ssldeactivate|sslactivate|monitor|clientlist}"
  echo "Example: $0 ssllist"
  echo "Example: $0 ssldeactivate"
  echo "Example: $0 sslactivate"
  echo "Example: $0 monitor"
  echo "Example: $0 clientlist"
  exit 1
}

function ssllist() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f ssllist
}

function ssldeactivate() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f ssldeactivate
}

function sslactivate() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f sslactivate
}

function monitor() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f monitor -d "action=get&listClient=true"
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f monitor -d "action=get&listClient=false" -i 10.3.101.3 -p 8300
}

function clientlist() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f clientlist -d "atmStatus=$1"
}

case $1 in
ssllist)
  ssllist
  ;;

ssldeactivate)
  ssldeactivate
  ;;

sslactivate)
  sslactivate
  ;;

monitor)
  monitor
  ;;

clientlist)
  clientlist $2
  ;;

*)
  usage
  ;;
esac
