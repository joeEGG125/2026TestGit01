#!/bin/sh

optUser=fepap1
optUserDir=/fep/fep-app/fep-gateway-fisc-agent
jarFile=$optUserDir/fep-gateway-fisc-agent.jar
programName=com.syscom.fep.gateway.cmd.FISCGatewayAgentCommand

function usage {
  if [ "$USER" != "$optUser" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  echo "Usage: $0 {start|stop|check|startChannel primary/secondary/all|stopChannel primary/secondary/all|showlog tx/rsm/disconn/txcurrent|changefepap (host)|resetfepap|checkfepap}"
  echo "Example: $0 start"
  echo "Example: $0 stop"
  echo "Example: $0 check"
  echo "Example: $0 startChannel primary"
  echo "Example: $0 startChannel secondary"
  echo "Example: $0 startChannel all"
  echo "Example: $0 stopChannel primary"
  echo "Example: $0 stopChannel secondary"
  echo "Example: $0 stopChannel all"
  echo "Example: $0 tx"
  echo "Example: $0 rsm"
  echo "Example: $0 disconn"
  echo "Example: $0 disconn 20240611"
  echo "Example: $0 txcurrent"
  echo "Example: $0 changefepap 127.0.0.1"
  echo "Example: $0 resetfepap"
  echo "Example: $0 checkfepap"
  exit 1
}

function start {
  if [ "$USER" != "$optUser" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f start
}

function stop {
  if [ "$USER" != "$optUser" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f stop
}

function check {
  if [ "$USER" != "$optUser" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f check -i "$1" -p "$2"
}

function channel {
  if [ "$USER" != "$optUser" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f channel -d "mode=$1&action=$2" -i "$3" -p "$4"
}

function showlogtx {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f showlog -d "logKind=tx" -i "$1" -p "$2"
}

function showlogrsm {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f showlog -d "logKind=rsm" -i "$1" -p "$2"
}

function showlogdisconn {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f showlog -d "logKind=disconn&date=$1" -i "$2" -p "$3"
}

function showlogtxcurrent {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f showlog -d "logKind=txcurrent" -i "$1" -p "$2"
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
start)
  start
  ;;

stop)
  stop
  ;;

check)
  check $2 $3
  ;;

startChannel)
  channel $2 start $3 $4
  ;;

stopChannel)
  channel $2 stop $3 $4
  ;;

tx)
  showlogtx $2 $3
  ;;

rsm)
  showlogrsm $2 $3
  ;;

disconn)
  showlogdisconn $2 $3 $4
  ;;

txcurrent)
  showlogtxcurrent $2 $3
  ;;

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
