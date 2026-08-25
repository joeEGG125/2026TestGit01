#!/bin/bash

optUser=syscom
optUserDir=/home/$optUser
jarFile=$optUserDir/fep-app/fep-server-fisc/fep-server-fisc.jar
httpReadTimeout=180000

function usage() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  echo "Usage: $0 {opcChangeKey (KeyId)}"
  echo "Example: $0 opcChangeKey 04"
  exit 1
}

function opcChangeKey() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -Dhttp.read.timeout=$httpReadTimeout -cp $jarFile com.syscom.fep.server.cmd.FISCOpcChangeKey /Host:$1 /Port:$2 /KeyId:$3
  }

function opcCheckin1000() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -Dhttp.read.timeout=$httpReadTimeout -cp $jarFile com.syscom.fep.server.cmd.FISCOpcCheckin1000 /Host:$1 /Port:$2 /KeyId:$3
}


function opcCheckout1000() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
   java -Dfile.encoding=UTF-8 -Dhttp.read.timeout=$httpReadTimeout -cp $jarFile com.syscom.fep.server.cmd.FISCOpcCheckout1000 /Host:$1 /Port:$2 /KeyId:$3
}


function opcNotice() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -Dhttp.read.timeout=$httpReadTimeout -cp $jarFile com.syscom.fep.server.cmd.FISCOpcNotice /Host:$1 /Port:$2 /KeyId:$3
}


case $1 in
opcChangeKey)
  opcChangeKey 10.3.101.3 8101 $2
  ;;
opcCheckin1000)
  opcCheckin1000 10.3.101.3 8101 $2
  ;;
opcCheckout1000)
  opcCheckout1000 10.3.101.3 8101 $2
  ;;
opcNotice)
  opcNotice 10.3.101.3 8101 $2
  ;;
*)
  usage
  ;;
esac
