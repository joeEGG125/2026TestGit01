#!/bin/sh


optUser=fep
optUserDir=/$optUser
jarFileName=fep-gateway-atm-agent.jar

if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
   echo "Warning! you must be user $optUser to start $jarFileName..."
   exit 1
fi

count=`ps -ef |grep java|grep $jarFileName|wc -l`
if [ $count != 0 ];then
   echo "The $jarFileName is already running..."
   exit 1
fi

fepAppDir=$optUserDir/fep-app/fep-gateway-atm-agent

cd $fepAppDir
#if [ -z $argXms ] || [ -z $argXmx ];then
  java -jar -Dfile.encoding=UTF-8 $jarFileName
#else
#  java -jar -Dfile.encoding=UTF-8 $jarFileName -Xms$argXms -Xmx$argXmx
#fi