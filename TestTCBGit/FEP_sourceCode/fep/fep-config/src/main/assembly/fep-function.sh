#!/bin/bash

# 傳入的jar檔名稱, 例如fep-gateway-atm.jar
appName=$2
# 傳入JAVA OPTS參數, 記憶體最小值-Xms
argXms=$3
# 傳入JAVA OPTS參數, 記憶體最大值-Xmx
argXmx=$4
# 傳入JAVA OPTS參數, 元空間大小-XX:MetaspaceSize
argMetaspaceSize=$5
# 取當前伺服器的Host Name
hn=`hostname`
# 執行shell檔使用者
optUser=syscom
# 執行shell檔使用者所在的主目錄
optUserDir=/home/$optUser
# FEP Standalone程式所在的主目錄
fepAppDir=$optUserDir/fep-app
# FEP Standalone程式產出的log檔目錄
fepAppLogsDir=$fepAppDir/logs
# 其他用於批量操控FEP程式, 以及操控Gateway的shell所在目錄
binDir=$optUserDir/bin
# 取執行shell時當前的系統日期, format為yyyy-MM-dd, 主要用於抓log檔
currentDate=`date -d today +"%Y-%m-%d"`

# 設定JAVA_OPTS參數, 用於JVM
if [ "x$JAVA_OPTS" = "x" ]; then
  # 設定編碼
  JAVA_OPTS="-Dfile.encoding=UTF-8"
  # 設定記憶體初始值
  if [ "x$argXms" != "x" ]; then
    JAVA_OPTS="$JAVA_OPTS -Xms$argXms"
  else
    JAVA_OPTS="$JAVA_OPTS -Xms256m"
  fi
  # 設定記憶體最大值
  if [ "x$argXmx" != "x" ]; then
    JAVA_OPTS="$JAVA_OPTS -Xmx$argXmx"
  else
    JAVA_OPTS="$JAVA_OPTS -Xmx512m"
  fi
  # 設定元空間大小
  if [ "x$argMetaspaceSize" != "x" ]; then
    JAVA_OPTS="$JAVA_OPTS -XX:MetaspaceSize=$argMetaspaceSize -XX:MaxMetaspaceSize=$argMetaspaceSize"
  else
    JAVA_OPTS="$JAVA_OPTS -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m"
  fi
  # 設定其他JVM參數
  JAVA_OPTS="$JAVA_OPTS -XX:-UseGCOverheadLimit -XX:+HeapDumpOnOutOfMemoryError"
else
  echo "JAVA_OPTS already set in environment; overriding default settings with values: $JAVA_OPTS"
fi

# 下面這段if, 用於如果執行shell時沒有輸入$appName, 則預設抓與當前shell檔在同一目錄下的jar檔
if [ -z $appName ];then
  appName=`ls -t |grep .jar$ |head -n1`
fi

# 啟動FEP Standalone程式的方法
function start()
{
  # 下面這段if, 用於判斷執行shell的使用者是否為$optUser, 如不是則跳出當前方法
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser to start fep app..."
    exit 1
  fi

  # 將$appName中的-替換為space, 並生成一個array
  # 例如fep-gateway-atm.jar
  # 1. 先將-替換為space, 則為fep gateway atm .jar
  # 2. 用()包起來產出一個array, 則
  #    array[0] = fep
  #    array[1] = gateway
  #    array[2] = atm
  #    array[3] = .jar
  local appNameArr=(${appName//-/ })
  # 取得程式所在的主目錄名稱, 及從$appName的右邊開始, 刪除最後一個.以及右邊的字符
  # 例如fep-gateway-atm.jar, 刪除.jar, 則得到fep-gateway-atm
  local appDirName=${appName%.*}
  # 取得程式所在的path, 例如/home/syscom/fep-app/fep-gateway-atm/
  local appPath=$fepAppDir/$appDirName
  # 取得log所在的path, 例如/home/syscom/fep-app/logs/yyyy-MM-dd/fep-gateway-atm, yyyy-MM-dd為當前系統日期
  # local appLogsPath=$appPath/logs/$currentDate
  # local appLogsPath=$fepAppLogsDir/$appDirName/$currentDate
  local appLogsPath=$fepAppLogsDir/$currentDate/$appDirName
  # 取得log檔的prefix名稱, 例如fep-gateway-atm-boot
  # local logFilename=${appNameArr[0]}-${appNameArr[1]}-boot
  local logFilename=$appDirName-boot
  # 取得jar檔所在的path, 例如/home/syscom/fep-app/fep-gateway-atm/fep-gateway-atm.jar
  local appJarPath=$appPath/$appName

  # 這裡要取得程式的main class名稱, 先取出gateway
  local mainClass=${appNameArr[1]}
  # 將上一步取出的名稱第一個字母轉大寫, 例如根據上一步gateway的首字母g, 得到G
  local firstLetter=`echo ${mainClass:0:1} | awk '{print toupper($0)}'`
  # 取出剩下的字符, 例如gateway, 得到ateway
  local otherLetter=${mainClass:1}
  # 最後得到最終的main class名稱, 例如SyscomFepGatewayApplication
  local mainClass=SyscomFep"$firstLetter""$otherLetter"Application

  # 這段主要是根據$appName名稱, 判斷是否已經有在運行
  local count=`ps -ef |grep java|grep $appName|wc -l`
  if [ $count != 0 ];then
    # 有在運行
    echo "The $appName is already running..."
  else
    # 沒有在運行, 則先判斷jar檔是否存在, 不存在則退出
    if [ ! -e "$appJarPath" ];then
      echo "Warning! Cannot start $appName, file $appJarPath not exist!!"
      exit 1
    fi
    # 以下這段主要是取出最後一個log檔, 並根據log檔內容找出上一次啟動成功的內容
    # 先判斷log檔所在目錄是否存在
    if [ -d "$appLogsPath" ];then
      # 取出所有的log檔名稱, 注意這裡返回的是一個array, 並且是根據log檔名稱進行倒序排序
      # 例如有fep-gateway-boot-yyyy-MM-dd-0.log, fep-gateway-boot-yyyy-MM-dd-1.log
      # 則取出的array為
      # array[0] = fep-gateway-boot-yyyy-MM-dd-1.log
      # array[1] = fep-gateway-boot-yyyy-MM-dd-0.log
      local logFiles=($(ls -vr $appLogsPath/$logFilename-*.log 2> /dev/null))
      # 如果有取到log檔
      if [ -n $logFiles ];then
        # 取出log檔的總數
        local logFilesCnt=${#logFiles[*]}
        # 如果log檔總數不為0
        if [ $logFilesCnt != 0 ];then
          # 取出最後的一個log檔名稱, 例如fep-gateway-boot-yyyy-MM-dd-1.log
          local latestLogFile=${logFiles[0]}
          # 列印訊息, 有找到最後一個log檔
          echo "Warning! Found latest log file $latestLogFile"
          # 再一次取出當前系統日期時間, 格式為yyyy-MM-dd HH:mm, 主要用於從log中抓取啟動成功的訊息
          local current=`date +%Y-%m-%d\ %H:%M`
          # 從log檔中抓取啟動成功的訊息
          # 例如啟動成功的話, log中會出現類似下面的內容
          # [yyyy-MM-dd HH:mm:ss.SSS][main][INFO ][com.syscom.fep.gateway.SyscomFepGatewayApplication]Started SyscomFepGatewayApplication in XXXXXX seconds (JVM running for XXXXX)
          # 則下面這行代碼的意思, 從log檔中, 找出含有yyyy-MM-dd HH:mm和Started SyscomFepGatewayApplication內容的這一行log
          local result=`grep "$current" $latestLogFile | grep "Started $mainClass"`
          # 如果有找到, 表示距上一次成功啟動程式的時間不到一分鐘, 則請下一分鐘再啟動, 並退出
          if [[ -n $result ]];then
            echo "Warning! The $appName was started at ${result:1:23} last time, please wait and start at next minute"
            exit 1
          fi
        else
          echo "Warning! The directory $appLogsPath is empty"
        fi
      else
        echo "Warning! The directory $appLogsPath is empty"
      fi
    else
      echo "Warning! The directory $appLogsPath is not exist"
    fi
    # 以上這段if, 只要就是為了防止在一分鐘內重新啟動程式, 因為如果兩次啟動的時間太近, 就會誤將上一次啟動成功的log內容抓出來, 並判斷成此次啟動成功
    # 為什麼是一分鐘內呢? 因為查找成功啟動的log, 是根據yyyy-MM-dd HH:mm來查找, 也就是最小時間間隔是分鐘
    # 如果按照yyyy-MM-dd HH:mm:ss, 即最小時間間隔為秒, 則有可能會漏掉, 所以雖然會重複查找, 但是可以防止漏掉, 只是需要避免一分鐘內再次啟動程式

    # 列印訊息, 開始啟動程式
    echo "The $appName is starting on host $hn, please wait..."
    # 啟動程式
    nohup java $JAVA_OPTS -jar $appJarPath > /dev/null 2>&1 &

    # 以下loop & wait 10次/秒, 主要是wait一下讓程式產出log檔
    local counter=0
    while [ ! -d "$appLogsPath" ] && [ $counter -lt 10 ];
    do
      sleep 1
      let counter=counter+1
    done

    # 以下loop & wait最多10次/秒, 判斷是否有log檔產出, 如果有產出則exit loop
    local counter=0
    while [ -z $logFiles ] && [ $counter -lt 10 ];
    do
      sleep 1
      let counter=counter+1
      # 這裡跟前面一樣, 取出所有的log檔名稱, 注意這裡返回的是一個array, 並且是根據log檔名稱進行倒序排序
      # 例如有fep-gateway-boot-yyyy-MM-dd-0.log, fep-gateway-boot-yyyy-MM-dd-1.log
      # 則取出的array為
      # array[0] = fep-gateway-boot-yyyy-MM-dd-1.log
      # array[1] = fep-gateway-boot-yyyy-MM-dd-0.log
      local logFiles=($(ls -vr $appLogsPath/$logFilename-*.log 2> /dev/null))
    done

    # 如果沒有log檔產出, 或者是可能(前面的設置不正確導致)path不對找不到log檔
    if [ -z $logFiles ];then
      # 列印訊息
      echo "Cannot check $appName status according to any log files"
    else
      # 取出最後一個log檔
      local latestLogFile=${logFiles[0]}
      echo "Checking $appName status according to ${latestLogFile}"
      local counter=0
      # loop & wait 最多20次/5秒
      while [ $counter -lt 20 ];
      do
        # 取出第一個日期時間點
        local before=`date +%Y-%m-%d\ %H:%M`
        # wait上5秒
        sleep 5
        # 取出第二個日期時間點
        local after=`date +%Y-%m-%d\ %H:%M`
        # 次數累加
        let counter=counter+1
        # 根據第一個日期時間點, 從log中找啟動成功的訊息
        local result=`grep "$before" $latestLogFile | grep "Started $mainClass"`
        if [[ -n $result ]];then
          # 找到則exit loop
          break
        fi
        # 根據第二個日期時間點, 從log中找啟動成功的訊息
        local result=`grep "$after" $latestLogFile | grep "Started $mainClass"`
        if [[ -n $result ]];then
          # 找到則exit loop
          break
        fi
        # 列印訊息
        echo "Waiting for the $appName to start in 5 seconds...loop_count = $counter"
        # 下面這段主要是判斷是否有新的log檔產出
        # 例如當前最後一個log檔為fep-gateway-boot-yyyy-MM-dd-1.log, 此時產出新的log檔, fep-gateway-boot-yyyy-MM-dd-2.lo
        # 這裡跟前面一樣, 取出所有的log檔名稱, 注意這裡返回的是一個array, 並且是根據log檔名稱進行倒序排序
        # 例如有fep-gateway-boot-yyyy-MM-dd-0.log, fep-gateway-boot-yyyy-MM-dd-1.log, fep-gateway-boot-yyyy-MM-dd-2.log
        # 則取出的array為
        # array[0] = fep-gateway-boot-yyyy-MM-dd-2.log
        # array[1] = fep-gateway-boot-yyyy-MM-dd-1.log
        # array[2] = fep-gateway-boot-yyyy-MM-dd-0.log
        local hasNewLogFiles=($(ls -vr $appLogsPath/$logFilename-*.log 2> /dev/null))
        if [ -n $hasNewLogFiles ];then
          if [ "${hasNewLogFiles[0]}" != "${latestLogFile}" ];then
            # 如果有產出新的log檔, 則重新設置最後的log檔名
            local latestLogFile=${hasNewLogFiles[0]}
            # 將counter設置為0, 重新開始loop
            local counter=0
            echo "Warning! Checking $appName status switch to ${latestLogFile}"
          fi
        fi
      done
    fi
    # 下面這段主要是判斷是否有啟動成功
    # 因為有可能無法根據log檔判斷是否有啟動成功, 則列印訊息提示人工check一下log檔
    local count=`ps -ef |grep java|grep $appName|wc -l`
    if [ $count != 0 ];then
      local appId=`ps -ef |grep java|grep $appName|awk '{print $2}'`
      if [[ -z $result ]];then
        # kill -9 $appId
        # echo "The $appName started failed!!!"
        echo "Warning! The $appName maybe started successfully on host $hn with pid = [$appId]"
        echo "But, it is highly recommended to check status according to ${latestLogFile}"
      else
        echo $result
        echo "The $appName started successfully on host $hn with pid = [$appId]"
      fi
    else
      echo "The $appName started failed!!!"
    fi

  fi
}
# 停止FEP Standalone程式的方法
function stop()
{
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser to stop fep app..."
    exit 1
  fi

  local appId=`ps -ef |grep java|grep $appName|awk '{print $2}'`
  if [ -z $appId ];then
    echo "The $appName is not running"
  else
    echo "The $appName is stopping on host $hn, please wait..."
    # 注意, 這裡先-15 kill程式, 是安全的做法
    kill -15 $appId

    local counter=0
    while [ $counter -lt 20 ];
    do
      sleep 5
      let counter=counter+1

      local appId=`ps -ef |grep java|grep $appName|awk '{print $2}'`
      if [ -z $appId ];then
        echo "The $appName stopped successfully on host $hn"
        exit 0
      fi

      echo "Waiting for the $appName to stop in 5 seconds...loop_count = $counter"
    done

    # 如果上面kill -15, loop & wait 20次/5秒, 都沒有成功, 則只能force to kill, 但是這樣可能會有問題
    kill -9 $appId
    sleep 1

    local appId=`ps -ef |grep java|grep $appName|awk '{print $2}'`
    if [ -z $appId ];then
      echo "The $appName stopped successfully on host $hn"
    else
      echo "The $appName stopped failed!!!"
    fi

  fi
}
# 查看FEP Standalone程式運行狀態的方法
function status()
{
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser to check fep app status..."
    exit 1
  fi

  local appId=`ps -ef |grep java|grep $appName|awk '{print $2}'`
  if [ -z $appId ] 
  then
    echo -e "The $appName is not running"
  else
    echo -e "The $appName is running on host $hn with pid = [$appId]"
  fi

  local appNameArr=(${appName//-/ })
  local appDirName=${appName%-*}

  # 如果是ATM GW, 則需要另外執行shell檔獲取GW的運行狀態
  if [ "$appDirName" = "fep-gateway-atm" ]; then
    echo ""
    $binDir/fep_gw_atmcmd.sh monitor
  fi

  # 如果是FISC GW, 則需要另外執行shell檔獲取GW的運行狀態
  if [ "$appDirName" = "fep-gateway-fisc" ]; then
    echo ""
    $binDir/fep_gw_fisccmd.sh monitor
  fi
}
# 使用幫助
function usage()
{
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser..."
    exit 1
  fi

  echo "Usage: $0 {start|stop|status -f}"
  echo "Example: $0 start"
  exit 1
}

case $1 in
  start)
  start;;

  stop)
  stop;;

  status)
  status;;

  *)
  usage;;
esac
