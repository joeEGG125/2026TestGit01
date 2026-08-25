#!/bin/sh

# 執行shell檔使用者
optUser=fep
# 執行shell檔使用者所在的主目錄
optUserDir=/$optUser
# JAR檔名稱
jarFileName=fep-service-appmon.jar

# 下面這段if, 用於判斷執行shell的使用者是否為$optUser, 如不是則跳出當前方法
if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
   echo "Warning! you must be user $optUser to start $jarFileName..."
   exit 1
fi

count=`ps -ef |grep java|grep $jarFileName|wc -l`
if [ $count != 0 ];then
   # 有在運行
   echo "The $jarFileName is already running..."
   exit 1
fi

# 傳入JAVA OPTS參數, 記憶體最小值-Xms
#argXms=$1
# 傳入JAVA OPTS參數, 記憶體最大值-Xmx
#argXmx=$2
# FEP Standalone程式所在的目錄
fepAppDir=$optUserDir/fep-app/fep-service-appmon

# cd到FEP Standalone程式所在的目錄
cd $fepAppDir
#if [ -z $argXms ] || [ -z $argXmx ];then
  # 啟動程式, 不含-Xms和-Xmx
  java -jar -Dfile.encoding=UTF-8 $jarFileName
#else
  # 啟動程式, 含有-Xms和-Xmx
#  java -jar -Dfile.encoding=UTF-8 $jarFileName -Xms$argXms -Xmx$argXmx
#fi