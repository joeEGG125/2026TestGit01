#!/bin/sh

# 執行shell檔使用者
optUser=fepap1
# 執行shell檔使用者所在的主目錄
optUserDir=/fep
# JAR檔名稱
jarFileName=fep-batch-cmdline.jar

# 下面這段if, 用於判斷執行shell的使用者是否為$optUser, 如不是則跳出當前方法
#if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
#   echo "Warning! you must be user $optUser to start $jarFileName..."
#   exit 1
#fi

# 程式名稱
programName=$1
# 傳入參數
argments=$2
# 是否送Queue訊息給批次服務平台
callJob=$3
# 傳入JAVA OPTS參數, 記憶體最小值-Xms
argXms=$4
# 傳入JAVA OPTS參數, 記憶體最大值-Xmx
argXmx=$5
# FEP Standalone程式所在的目錄
fepAppDir=$optUserDir/fep-app/fep-batch-cmdline

# cd到FEP Standalone程式所在的目錄
cd $fepAppDir

if [ "$programName" = "" ] \
  || [ "$programName" = "h" ] \
  || [ "$programName" = "help" ] \
  || [ "$programName" = "?" ]; then
  java -Dfile.encoding=UTF-8 -jar $fepAppDir/$jarFileName -h
elif [ -z $argXms ] || [ -z $argXmx ];then
  # 啟動程式, 不含-Xms和-Xmx
  nohup java -Dfile.encoding=UTF-8 -jar $fepAppDir/$jarFileName -p $programName -a "$argments" -c $callJob > /dev/null 2>&1 &
else
  # 啟動程式, 含有-Xms和-Xmx
  nohup java -Dfile.encoding=UTF-8 -jar $fepAppDir/$jarFileName -p $programName -a "$argments" -c $callJob -Xms$argXms -Xmx$argXmx > /dev/null 2>&1 &
fi