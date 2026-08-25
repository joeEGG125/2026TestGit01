#!/bin/bash

hn=`hostname`
optUser=syscom
optUserDir=/home/$optUser
fepAppDir=$optUserDir/fep-app
binDir=$optUserDir/bin

function doActionWithFepApp()
{
	local appName=$1
	local action=$2

	if [ "$action" = "stop" ]; then
		if [ "$all_flag" != "y" ]; then
			echo "Are you sure you want to $action $appName [a(all)|y(yes)|n(no)|e(exit)]..."
			read choice

			if [ "$choice" = "e" ]; then
				echo "Warning! Exit commond $action for all fep app..."
				echo
				exit 1
			fi

			if [ "$choice" = "n" ]; then
				echo "Warning! Don not $action $appName..."
				echo
				return
			fi

			if [ "$choice" = "a" ]; then
				all_flag="y"
			fi
		fi
	fi

:<<!
	if [ "$appName" = "fep-server" ] \
		|| [ "$appName" = "fep-service-ems" ] \
		|| [ "$appName" = "fep-service-appmon" ] \
		|| [ "$appName" = "fep-web" ] \
		|| [ "$appName" = "fep-batch" ]; then
!
	if [ "$appName" = "fep-web" ]; then
		$binDir/fep_wlp.sh $action $appName
	else
		local fepAppShell=${appName//-/_}_$action.sh
		cd $fepAppDir/$appName
		$fepAppDir/$appName/$fepAppShell
	fi
	echo
}
