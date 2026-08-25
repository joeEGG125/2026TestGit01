#!/bin/bash


# 執行shell檔使用者所在的主目錄
optUserDir=/fep
# JAR檔名稱
jarFileName=fep-server-imsgw.jar



count=`ps -ef |grep java|grep $jarFileName|wc -l`
if [ $count != 0 ];then
   # 有在運行
   echo "The $jarFileName is already running..."
   exit 1
fi

# 傳入JAVA OPTS參數, 記憶體最小值-Xms
argXms=$1
# 傳入JAVA OPTS參數, 記憶體最大值-Xmx
argXmx=$2
# FEP Standalone程式所在的目錄
fepAppDir=$optUserDir/fep-app/fep-server-imsgw

# cd到FEP Standalone程式所在的目錄
cd $fepAppDir
if [ -z $argXms ] || [ -z $argXmx ];then
  # 啟動程式, 不含-Xms和-Xmx
  java -jar -Dfile.encoding=UTF-8 $jarFileName
else
  # 啟動程式, 含有-Xms和-Xmx
  java -jar -Dfile.encoding=UTF-8 $jarFileName -Xms$argXms -Xmx$argXmx
fi