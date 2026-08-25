#!/bin/bash

# 執行shell檔使用者
optUser=syscom
# 執行shell檔使用者所在的主目錄
optUserDir=/home/$optUser
# JAR檔名稱
jarFileName=${project.artifactId}${assembly-func}.jar

# 下面這段if, 用於判斷執行shell的使用者是否為$optUser, 如不是則跳出當前方法
if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
   echo "Warning! you must be user $optUser to start $jarFileName..."
   exit 1
fi

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
# 傳入JAVA OPTS參數, 元空間大小-XX:MetaspaceSize
argMetaspaceSize=$6
# FEP Standalone程式所在的目錄
fepAppDir=$optUserDir/fep-app/${project.artifactId}${assembly-func}
# 取得jar檔所在的path
appJarPath=$fepAppDir/$jarFileName

# 設定JAVA_OPTS參數, 用於JVM
if [ "x$JAVA_OPTS" = "x" ]; then
  # 設定編碼
  JAVA_OPTS="-Dfile.encoding=UTF-8"
  # 設定記憶體初始值
  if [ "x$argXms" != "x" ]; then
    JAVA_OPTS="$JAVA_OPTS -Xms$argXms"
  else
    JAVA_OPTS="$JAVA_OPTS -Xms${assembly-jvm-xms}"
  fi
  # 設定記憶體最大值
  if [ "x$argXmx" != "x" ]; then
    JAVA_OPTS="$JAVA_OPTS -Xmx$argXmx"
  else
    JAVA_OPTS="$JAVA_OPTS -Xmx${assembly-jvm-xmx}"
  fi
  # 設定元空間大小
  if [ "x$argMetaspaceSize" != "x" ]; then
    JAVA_OPTS="$JAVA_OPTS -XX:MetaspaceSize=$argMetaspaceSize -XX:MaxMetaspaceSize=$argMetaspaceSize"
  else
    JAVA_OPTS="$JAVA_OPTS -XX:MetaspaceSize=${assembly-jvm-metaspaceSize} -XX:MaxMetaspaceSize=${assembly-jvm-metaspaceSize}"
  fi
  # 設定其他JVM參數
  JAVA_OPTS="$JAVA_OPTS -XX:-UseGCOverheadLimit -XX:+HeapDumpOnOutOfMemoryError"
else
  echo "JAVA_OPTS already set in environment; overriding default settings with values: $JAVA_OPTS"
fi

# cd到FEP Standalone程式所在的目錄
cd $fepAppDir

if [ "$programName" = "" ] \
  || [ "$programName" = "h" ] \
  || [ "$programName" = "help" ] \
  || [ "$programName" = "?" ]; then
  java $JAVA_OPTS -jar $appJarPath -h
else
  # 啟動程式
  nohup java $JAVA_OPTS -jar $appJarPath -p $programName -a "$argments" -c $callJob > /dev/null 2>&1 &
fi