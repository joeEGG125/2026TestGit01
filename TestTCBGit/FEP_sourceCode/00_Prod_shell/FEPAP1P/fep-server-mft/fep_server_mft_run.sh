#!/bin/bash


optUserDir=/fep
jarFileName=fep-server-mft.jar


count=`ps -ef |grep java|grep $jarFileName|wc -l`
if [ $count != 0 ];then
   echo "The $jarFileName is already running..."
   exit 1
fi

argXms=$1
argXmx=$2
fepAppDir=$optUserDir/fep-app/fep-server-mft

cd $fepAppDir
if [ -z $argXms ] || [ -z $argXmx ];then
  java -jar -Dfile.encoding=UTF-8 $jarFileName
else
  java -jar -Dfile.encoding=UTF-8 $jarFileName -Xms$argXms -Xmx$argXmx
fi
