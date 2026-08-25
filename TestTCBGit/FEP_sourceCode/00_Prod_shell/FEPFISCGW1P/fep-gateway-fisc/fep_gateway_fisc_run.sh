#!/bin/sh

optUser=fepap1
optUserDir=/fep/fep-app

jarFileName=fep-gateway-fisc.jar

count=`ps -ef |grep java|grep $jarFileName|wc -l`
if [ $count != 0 ];then
   echo "The $jarFileName is already running..."
   exit 1
fi

argXms=$1
argXmx=$2

fepAppDir=$optUserDir/fep-gateway-fisc

if [ -z $argXms ] || [ -z $argXmx ];then
  # 啟動程式, 不含-Xms和-Xmx
  java -jar -Dfile.encoding=UTF-8 $fepAppDir/$jarFileName
else
  # 啟動程式, 含有-Xms和-Xmx
  java -jar -Dfile.encoding=UTF-8 $fepAppDir/$jarFileName -Xms$argXms -Xmx$argXmx
fi
