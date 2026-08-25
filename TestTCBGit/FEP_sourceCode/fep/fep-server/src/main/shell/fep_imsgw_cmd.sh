#!/bin/bash

optUser=syscom
optUserDir=/home/$optUser
jarFile=$optUserDir/fep-app/fep-server-imsgw/fep-server-imsgw.jar
httpReadTimeout=180000

function usage() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  echo "Usage: $0 {startChannel primary|secondary}"
  echo "Usage: $0 {stopChannel primary|secondary}"
  echo "Usage: $0 {check}"
  echo "Example: $0 startChannel primary"
  echo "Example: $0 startChannel secondary"
  echo "Example: $0 stopChannel primary"
  echo "Example: $0 stopChannel secondary"
  echo "Example: $0 check"
  exit 1
}

function startChannel() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -Dhttp.read.timeout=$httpReadTimeout -cp $jarFile com.syscom.fep.server.cmd.IMSGatewayCommand /Host:$1 /Port:$2 /Mode:$3 /Action:start
}

function stopChannel() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -Dhttp.read.timeout=$httpReadTimeout -cp $jarFile com.syscom.fep.server.cmd.IMSGatewayCommand /Host:$1 /Port:$2 /Mode:$3 /Action:stop
}

function check() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -Dhttp.read.timeout=$httpReadTimeout -cp $jarFile com.syscom.fep.server.cmd.IMSGatewayCommand /Host:$1 /Port:$2 /Action:check
}

case $1 in
startChannel)
  startChannel 10.3.101.3 8213 $2
  ;;

stopChannel)
  startChannel 10.3.101.3 8213 $2
  ;;

check)
  check 10.3.101.3 8213
  ;;

*)
  usage
  ;;
esac
