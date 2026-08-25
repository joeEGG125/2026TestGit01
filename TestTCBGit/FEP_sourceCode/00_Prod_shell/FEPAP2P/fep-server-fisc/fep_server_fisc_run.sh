#!/bin/sh

optUser=fep
optUserDir=/$optUser/fep-app
jarFileName=fep-server-fisc.jar

count=`ps -ef |grep java|grep $optUserDir|grep $jarFileName|wc -l`
if [ $count != 0 ];then
   echo "The $jarFileName is already running..."
   exit 1
fi

argXms=$1
argXmx=$2
fepAppDir=$optUserDir/fep-server-fisc

cd $fepAppDir
if [ -z $argXms ] || [ -z $argXmx ];then
  java -jar -Dfile.encoding=UTF-8 $jarFileName
else
  java -jar -Dfile.encoding=UTF-8 $jarFileName -Xms$argXms -Xmx$argXmx
fi

