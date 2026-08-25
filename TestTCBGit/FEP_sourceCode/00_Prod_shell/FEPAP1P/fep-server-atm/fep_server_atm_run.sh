#!/bin/sh

optUserDir=/fep/fep-app
fepAppDir=$optUserDir/fep-server-atm
jarFileName=fep-server-atm.jar

count=`ps -ef |grep java|grep $optUserDir|grep $jarFileName|wc -l`
if [ $count != 0 ];then
   echo "The $jarFileName is already running..."
   exit 1
fi

#argXms=$1
#argXmx=$2

#if [ -z $argXms ] || [ -z $argXmx ];then
  java -jar -Dfile.encoding=UTF-8 $fepAppDir/$jarFileName
#else
#  java -jar -Dfile.encoding=UTF-8 $fepAppDir/$jarFileName -Xms$argXms -Xmx$argXmx
#fi



