#!/bin/sh


optUserDir=/fep
jarFile=$optUserDir/fep-app/fep-gateway-atm/fep-gateway-atm.jar
programName=com.syscom.fep.gateway.cmd.ATMGatewayCommand

function usage {

  echo "Usage: $0  {ssllist|ssldeactivate|sslactivate}"
  echo "Example: $0 ssllist"
  echo "Example: $0 ssldeactivate"
  echo "Example: $0 sslactivate"
  exit 1
}

function ssllist {
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f ssllist -i 127.0.0.1 -p 8300
}

function ssldeactivate {
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f ssldeactivate -i 127.0.0.1 -p 8300
}

function sslactivate {
  java -Dfile.encoding=UTF-8 -cp $jarFile $programName -f sslactivate -i 127.0.0.1 -p 8300
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

*)
  usage
  ;;
esac
