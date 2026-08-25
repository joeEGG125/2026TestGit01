#!/bin/sh

optUser=fepap1
optUserDir=/fep/fep-app
jarFile=$optUserDir/fep-server-fisc/fep-server-fisc.jar
httpReadTimeout=180000

function usage {
  if [ "$USER" != "$optUser"]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  echo "Usage: $0 {opcChangeKey (KeyId)}"
  echo "Example: $0 opcChangeKey 04"
  exit 1
}

function opcChangeKey {
  if [ "$USER" != "$optUser" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -Dhttp.read.timeout=$httpReadTimeout -cp $jarFile com.syscom.fep.server.cmd.FISCOpcChangeKey /Host:$1 /Port:$2 /KeyId:$3
}

function opcCheckin1000 {
  if [ "$USER" != "$optUser" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -Dhttp.read.timeout=$httpReadTimeout -cp $jarFile com.syscom.fep.server.cmd.FISCOpcCheckin1000 /Host:$1 /Port:$2 /KeyId:$3
}

function opcCheckout1000 {
  if [ "$USER" != "$optUser" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
   java -Dfile.encoding=UTF-8 -Dhttp.read.timeout=$httpReadTimeout -cp $jarFile com.syscom.fep.server.cmd.FISCOpcCheckout1000 /Host:$1 /Port:$2 /KeyId:$3
}


function opcNotice {
  if [ "$USER" != "$optUser" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  echo "Notice calling fisc server..."
  java -Dfile.encoding=UTF-8 -Dhttp.read.timeout=$httpReadTimeout -cp $jarFile com.syscom.fep.server.cmd.FISCOpcNotice /Host:$1 /Port:$2 /KeyId:$3
  echo "finish calling."
}

case $1 in
opcChangeKey)
  opcChangeKey 10.0.54.34 8933 $2
  ;;
opcCheckin1000)
  opcCheckin1000 10.0.54.34 8933 $2
  ;;
opcCheckout1000)
  opcCheckout1000 10.0.54.34 8933 $2
  ;;
opcNotice)
  echo "Notice calling..."
  opcNotice 10.0.54.34 8933 $2
  ;;

*)
  usage
  ;;
esac
