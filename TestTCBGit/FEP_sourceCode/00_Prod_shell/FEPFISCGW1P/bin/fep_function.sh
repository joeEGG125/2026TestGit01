#/usr/bin/sh

appName=$2
argXms=$3
argXmx=$4
AppD=$5
hn=`hostname`
optUser=fep
optUserDir=/$optUser
fepAppDir=$optUserDir/fep-app
fepAppLogsDir=$optUserDir/logs
binDir=$fepAppDir/bin
date=`date +%d`
currentDate=`date +"%Y-%m-%d"`
TIMESTAMP=`date +%Y-%m-%d" "%H:%M:%S`
USERNAME=${USER}
appNameVersion=""
procName=$(echo $appName | sed 's/\.jar$//')
function start
{
    set -A appNameArr -- $(echo $appName | tr "-" " ")
    local name=${appNameArr[1]}
    if [ $name = "batch.jar" ];then
        appNameVersion=${appNameArr[0]}-${appNameArr[1]}
    elif [ "${appNameArr[3]}" = "agent.jar" ];then
        appNameVersion=${appNameArr[0]}-${appNameArr[1]}-${appNameArr[2]}-${appNameArr[3]}
    else
        appNameVersion=${appNameArr[0]}-${appNameArr[1]}-${appNameArr[2]}
    fi
        local appDirName=${appName%.*}
        local appPath=$fepAppDir/$appDirName
    local appLogsPath=$fepAppLogsDir/$currentDate/$appDirName
#    local logFilename=$appDirName-all
        local logFilename=$appDirName-boot
        local appJarPath=$appPath/$appName
        local mainClass=${appNameArr[1]}
    local firstLetter1="$(echo $mainClass | cut -c 1-1)"
    local firstLetter=`echo ${firstLetter1} | awk '{print toupper($0)}'`
    local length="${#mainClass}"
    local otherLetter="$(echo $mainClass | cut -c 2-$length)"
        local mainClass=SyscomFep"$firstLetter""$otherLetter"Application
        local count=`ps -ef |grep java|grep $fepAppDir|grep $appName|wc -l`
        if [ $count != 0 ];then
                echo "The $procName is already running..."
        else
        if [ ! -e "$appJarPath" ];then
                   echo "Warning! Cannot start $procName, file $appJarPath not exist!!"
                   exit 1
        fi
    if [[ -d "$appLogsPath" ]];then
        set -A logFiles -- $(echo $appLogsPath/$logFilename*.log)
            if [[ -n $logFiles ]];then
                        local logFilesCnt=${#logFiles[*]}
                        if [ $logFilesCnt != 0 ];then
                                local latestLogFile=${logFiles[0]}
                                echo "Warning! Found latest log file $latestLogFile"
                                local current=`date +%Y-%m-%d\ %H:%M`

                                local result=`grep "$current" $latestLogFile | grep "Started $mainClass"`
                        else
                                echo "Warning! The directory $appLogsPath is empty"
                        fi
                else
                        echo "Warning! The directory $appLogsPath is empty"
                fi
        else
                echo "Warning! The directory $appLogsPath is not exist"
        fi

        echo "The $procName is starting on host $hn at: ${TIMESTAMP}, please wait..."
    #    echo "${AppD}"
       nohup java -jar -Dfile.encoding=UTF-8 $appJarPath > /dev/null 2>&1 &
       #nohup java $AppD -jar -Dfile.encoding=UTF-8 $appJarPath > /dev/null 2>&1 &
        local counter=0
        while [ ! -d "$appLogsPath" ] && [ $counter -lt 10 ];
        do
                sleep 1
                let counter=counter+1
        done
        local counter=0
        while [ "$logFiles"="" ] && [ $counter -lt 10 ];
        do
                sleep 1
                let counter=counter+1
        set -A logFiles $(ls $appLogsPath/$logFilename-*.log)
        done
        if [[ -z $logFiles ]];then
                echo "Cannot check $procName status according to any log files"
        else
                local latestLogFile=${logFiles[*]}
                echo "Checking $procName status according to ${latestLogFile}"
                local counter=0
                local StartTime=`date +%Y-%m-%d\ %H:%M`
                while [ $counter -lt 5 ];
                do
                        local result=`grep "$StartTime" $latestLogFile | grep "Started $mainClass"`
                        if [[ -n $result ]];then
                                break
                        fi
                        local before=`date +%Y-%m-%d\ %H:%M`
                        sleep 5
                        let counter=counter+1
                        local result=`grep "$before" $latestLogFile | grep "Started $mainClass"`
                        if [[ -n $result ]];then
                                break
                        fi
                        echo "Waiting for the $procName to start in 5 seconds...loop_count = $counter"
                done
        fi
        local count=`ps -ef |grep java|grep $fepAppDir|grep $appName|wc -l`
        if [ $count != 0 ];then
                local appId=`ps -ef |grep java|grep $fepAppDir|grep $appName|awk '{print $2}'`
                if [[ -z $result ]];then
                        echo "Warning! The $procName maybe started successfully on host $hn with pid = [$appId]"
                        echo "But, it is highly recommended to check status according to ${latestLogFile}"
                else
                        local finishTimeStamp=`date +%Y-%m-%d" "%H:%M:%S`
                        echo "The $procName started successfully on host $hn at: ${finishTimeStamp} by user: ${USERNAME} with pid = [$appId]"
                fi
        else
        if [ $appNameVersion = "fep-gateway-atm" ]; then
            echo The $appNameVersion started failed!!! Please check if Atm-Server is activated!!!
        else
                    echo "The $procName started failed!!!"
        fi
        fi
        fi
}
function Stop
{
        local appId=`ps -ef |grep java|grep $fepAppDir|grep $appName|awk '{print $2}'`
    set -A appNameArr -- $(echo $appName | tr "-" " ")
    local name=${appNameArr[1]}
    if [ $name = "batch.jar" ]; then
        appNameVersion=${appNameArr[0]}-${appNameArr[1]}
    elif [ "${appNameArr[3]}" = "agent.jar" ];then
        appNameVersion=${appNameArr[0]}-${appNameArr[1]}-${appNameArr[2]}-${appNameArr[3]}
    else
        appNameVersion=${appNameArr[0]}-${appNameArr[1]}-${appNameArr[2]}
    fi
        if [[ -z $appId ]];then
                echo "The $procName is not running"
        else
                echo "The $procName is stopping on host $hn at: ${TIMESTAMP}, please wait..."
        kill $appId

                local counter=0
                while [ $counter -lt 20 ];
                do
                        sleep 5
                        let counter=counter+1

                        appId=`ps -ef |grep java|grep $fepAppDir|grep $appName|awk '{print $2}'`
                if [[ -z $appId ]];then
                                local finishTimeStamp=`date +%Y-%m-%d" "%H:%M:%S`
                                echo "The $procName stopped successfully on host $hn at: ${finishTimeStamp} by user: ${USERNAME}"
                                exit 0
                fi
                        echo "Waiting for the $procName to stop in 5 seconds...loop_count = $counter"
                done

        kill $appId
                sleep 1

                appId=`ps -ef |grep java|grep $fepAppDir|grep $appName|awk '{print $2}'`
                if [[ -z $appId ]];then
                        local finishTimeStamp=`date +%Y-%m-%d" "%H:%M:%S`
                        echo "The $procName stopped successfully on host $hn at: ${finishTimeStamp} by user: ${USERNAME}"
                else
                        echo "The $procName stopped failed!!!"
                fi
        fi
}

function status
{
    set -A appNameArr -- $(echo $appName | tr "-" " ")
        local name=${appNameArr[1]}
        if [ $name = "batch.jar" ];then
        appNameVersion=${appNameArr[0]}-${appNameArr[1]}
    elif [ "${appNameArr[3]}" = "agent.jar" ];then
        appNameVersion=${appNameArr[0]}-${appNameArr[1]}-${appNameArr[2]}-${appNameArr[3]}
    else
        appNameVersion=${appNameArr[0]}-${appNameArr[1]}-${appNameArr[2]}
    fi

        local appId=`ps -ef |grep java|grep $fepAppDir|grep $appName|awk '{print $2}'`
    if [[ -z "$appId" ]]; then
        echo "The $procName is not running"
        else
        echo "The $procName is running on host $hn with pid = [$appId]"
        fi

    set -A appNameArr -- $(echo $appName | tr "-" "")
        local appDirName=${appName%.*}
}

function usage
{
        echo "Usage: $0 {start|Stop|status -f}"
        echo "Example: $0 start"
        exit 1
}

case $1 in
        start)
        start;;

        Stop)
        Stop;;

        status)
        status;;

        *)
        usage;;
esac
