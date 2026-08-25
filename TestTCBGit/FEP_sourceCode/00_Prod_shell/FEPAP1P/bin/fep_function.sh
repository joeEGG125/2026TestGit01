#/usr/bin/sh

appName=$2
argXms=""
argXmx=""
AppD=$5
xx=""
xxMax=""

if [ -n "$3" ]; then
	argXms="-Xms$3"
fi

if [ -n "$4" ]; then
	argXmx="-Xmx$4"
fi

if [ -n "$6" ]; then
	xx="-XX:MetaspaceSize=$6"
fi

if [ -n "$7" ]; then
	xxMax="-XX:MaxMetaspaceSize=$7"
fi

hn=`hostname`
optUser=fep
optUserDir=/$optUser
fepAppDir=$optUserDir/fep-app
fepAppLogsDir=$optUserDir/logs
binDir=$fepAppDir/bin
date=`date +%d`
currentDate=`date +"%Y-%m-%d"`
appNameVersion=""
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
		echo "The $appNameVersion is already running..."
	else
        if [ ! -e "$appJarPath" ];then
		   echo "Warning! Cannot start $appNameVersion, file $appJarPath not exist!!"
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

	echo "The $appNameVersion is starting on host $hn, please wait..."
	
        echo "nohup java $argXms $argXmx $xx $xxMax -jar -Dfile.encoding=UTF-8 $appJarPath > /dev/null 2>&1"
        #echo "nohup java $argXms $argXmx $xx $xxMax $AppD -jar -Dfile.encoding=UTF-8 $appJarPath > /dev/null 2>&1" 
         nohup java $argXms $argXmx $xx $xxMax -jar -Dfile.encoding=UTF-8 $appJarPath > /dev/null 2>&1 & 
         #nohup java $argXms $argXmx $xx $xxMax $AppD -jar -Dfile.encoding=UTF-8 $appJarPath > /dev/null 2>&1 &
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
		echo "Cannot check $appNameVersion status according to any log files"
	else
		local latestLogFile=${logFiles[*]}
		echo "Checking $appNameVersion status according to ${latestLogFile}"
		local counter=0
		while [ $counter -lt 5 ];
		do
			local before=`date +%Y-%m-%d\ %H:%M`
			sleep 5
			local after=`date +%Y-%m-%d\ %H:%M`
			let counter=counter+1
			local result=`grep "$before" $latestLogFile | grep "Started $mainClass"`
			if [[ -n $result ]];then
				break
			fi
			echo "Waiting for the $appNameVersion to start in 5 seconds...loop_count = $counter"
		done
	fi
	local count=`ps -ef |grep java|grep $fepAppDir|grep $appName|wc -l`
	if [ $count != 0 ];then
		local appId=`ps -ef |grep java|grep $fepAppDir|grep $appName|awk '{print $2}'`
		if [[ -z $result ]];then
			echo "Warning! The $appNameVersion maybe started successfully on host $hn with pid = [$appId]"
			echo "But, it is highly recommended to check status according to ${latestLogFile}"
		else
			echo "The $appNameVersion started successfully on host $hn with pid = [$appId]"
		fi
	else
        if [ $appNameVersion = "fep-gateway-atm" ]; then
            echo The $appNameVersion started failed!!! Please check if Atm-Server is activated!!!
        else 
		    echo "The $appNameVersion started failed!!!"
        fi
	fi
	fi
	AppD=''
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
		echo "The $appNameVersion is not running"
	else
		echo "The $appNameVersion is stopping on host $hn, please wait..."
        kill $appId

		local counter=0
		while [ $counter -lt 20 ];
		do
			sleep 5
			let counter=counter+1

			appId=`ps -ef |grep java|grep $fepAppDir|grep $appName|awk '{print $2}'`
	        if [[ -z $appId ]];then
				echo "The $appNameVersion stopped successfully on host $hn"
				exit 0
	        fi
			echo "Waiting for the $appNameVersion to stop in 5 seconds...loop_count = $counter"
		done

        kill -9 $appId
		sleep 1

		appId=`ps -ef |grep java|grep $fepAppDir|grep $appName|awk '{print $2}'`
		if [[ -z $appId ]];then
			echo "The $appNameVersion stopped successfully on host $hn."
		else
			echo "The $appNameVersion stopped failed!!!"
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
        echo "The $appNameVersion is not running"
	else
        echo "The $appNameVersion is running on host $hn with pid = [$appId]"
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
