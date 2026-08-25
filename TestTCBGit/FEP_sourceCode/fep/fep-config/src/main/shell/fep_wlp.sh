#!/bin/bash

hn=`hostname`
optUser=syscom
optUserDir=/home/$optUser
wlpDir=$optUserDir/wlp
wlpServerCmd=$wlpDir/bin/server
fepAppDir=$wlpDir/usr/servers
fepAppLogsDir=$optUserDir/fep-app/waslogs
currentDate=`date -d today +"%Y-%m-%d"`

function startwlp()
{
	if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
		echo "Warning! you must be user $optUser to start WebSphere Application Server..."
		exit 1
	fi

	local appName=$1
	local appNameArr=(${appName//-/ })
	local appDirName=$appName
	local appPath=$fepAppDir/$appDirName

	# local appLogsPath=$appPath/waslogs/$currentDate
	# local appLogsPath=$fepAppLogsDir/$appDirName/$currentDate
	local appLogsPath=$fepAppLogsDir/$currentDate/$appDirName

	# local logFilename=${appNameArr[0]}-${appNameArr[1]}-boot
	local logFilename=$appDirName-boot

	local mainClass=${appNameArr[1]}
	local firstLetter=`echo ${mainClass:0:1} | awk '{print toupper($0)}'`
	local otherLetter=${mainClass:1}
	local mainClass=SyscomFep"$firstLetter""$otherLetter"Application

	local count=`ps -ef |grep java|grep $appName|wc -l`
	if [ $count != 0 ];then
		echo "The $appName is already running..."
	else

		if [ -d "$appLogsPath" ];then
			local logFiles=($(ls -vr $appLogsPath/$logFilename-*.log 2> /dev/null))
			if [ -n $logFiles ];then
				local logFilesCnt=${#logFiles[*]}
				if [ $logFilesCnt != 0 ];then
					local latestLogFile=${logFiles[0]}
					echo "Warning! Found latest log file $latestLogFile"
					local current=`date +%Y-%m-%d\ %H:%M`
					local result=`grep "$current" $latestLogFile | grep "Started $mainClass"`
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

		echo "The $appName is starting on host $hn, please wait..."
		nohup $wlpServerCmd start $appName > /dev/null 2>&1 &

		local counter=0
		while [ ! -d "$appLogsPath" ] && [ $counter -lt 15 ];
		do
			sleep 1
			let counter=counter+1
		done

		local counter=0
		while [ -z $logFiles ] && [ $counter -lt 15 ];
		do
			sleep 1
			let counter=counter+1
			local logFiles=($(ls -vr $appLogsPath/$logFilename-*.log 2> /dev/null))
		done

		if [ -z $logFiles ];then
			echo "Cannot check $appName status according to any log files"
		else
			local latestLogFile=${logFiles[0]}
			echo "Checking $appName status according to ${latestLogFile}"
			local counter=0
			while [ $counter -lt 20 ];
			do
				local before=`date +%Y-%m-%d\ %H:%M`
				sleep 5
				local after=`date +%Y-%m-%d\ %H:%M`
				let counter=counter+1

				local result=`grep "$before" $latestLogFile | grep "Started $mainClass"`
				if [[ -n $result ]];then
					break
				fi

				local result=`grep "$after" $latestLogFile | grep "Started $mainClass"`
				if [[ -n $result ]];then
					break
				fi

				echo "Waiting for the $appName to start in 5 seconds...loop_count = $counter"

				local hasNewLogFiles=($(ls -vr $appLogsPath/$logFilename-*.log 2> /dev/null))
				if [ -n $hasNewLogFiles ];then
					if [ "${hasNewLogFiles[0]}" != "${latestLogFile}" ];then
						local latestLogFile=${hasNewLogFiles[0]}
						local counter=0
						echo "Warning! Checking $appName status switch to ${latestLogFile}"
					fi
				fi
			done
		fi

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

function stopwlp()
{
	if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
		echo "Warning! you must be user $optUser to stop WebSphere Application Server..."
		exit 1
	fi

	local appName=$1

	$wlpServerCmd stop $appName
}

function statuswlp()
{
	if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
		echo "Warning! you must be user $optUser to check WebSphere Application Server status..."
		exit 1
	fi

	local appName=$1

	$wlpServerCmd status $appName
}

function usagewlp()
{
	if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
		echo "Warning! you must be user $optUser..."
		exit 1
	fi

	echo "Usage: $0 {start|stop|status AppName}"
	echo "Example: $0 start fep-web"
	exit 1
}

case $1 in
	start)
	startwlp $2;;

	stop)
	stopwlp $2;;

	status)
	statuswlp $2;;

	*)
	usagewlp $2;;
esac
