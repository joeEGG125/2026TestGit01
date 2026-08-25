#!/bin/bash

optUser=syscom
optUserDir=/home/$optUser
jarFile=$optUserDir/fep-app/fep-gateway-fisc/fep-gateway-fisc.jar
programName=com.syscom.fep.gateway.cmd.FISCGatewayCommand

function usage() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  echo "Usage: $0 {monitor action|startChannel primary/secondary/all|stopChannel primary/secondary/all|checkStatus}"
  echo "Example: $0 monitor get"
  echo "Example: $0 startChannel primary"
  echo "Example: $0 startChannel secondary"
  echo "Example: $0 startChannel all"
  echo "Example: $0 stopChannel primary"
  echo "Example: $0 stopChannel secondary"
  echo "Example: $0 stopChannel all"
  echo "Example: $0 checkStatus"
  exit 1
}

function monitor() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f monitor -d "action=$1"
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f monitor -d "action=$1" -i 10.3.101.3 -p 8301
}

function operate() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f operate -d "mode=$1&action=$2"
}

function checkStatus () {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f operate -d "action=check"
}

case $1 in
monitor)
  monitor $2
  ;;

startChannel)
  operate $2 start
  ;;

stopChannel)
  operate $2 stop
  ;;

checkStatus)
  checkStatus
  ;;

*)
  usage
  ;;
esac
