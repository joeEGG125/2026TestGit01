#!/bin/sh

bootStatus=""
today=`date +%Y-%m-%d`
log_base="/fep/logs/$today"

HostName=`hostname`
optUserDir=/fep/fep-app

TimesTamp=`date +%Y-%m-%d" "%H:%M:%S`

fepSystemService="fep-service-ems fep-notify fep-batch fep-service-appmon fep-service-cbstimeoutrerun fep-service-log"
fepPrimServer="fep-gateway-cbs fep-server-imsgw fep-server-atm fep-server-fisc"
fepOtherService="fep-server-eatm fep-server-posgw fep-server-hce fep-server-mft fep-server-mb fep-server-nb fep-server-mch fep-server-eoi fep-server-eip fep-server-pyBatch fep-server-vo fep-server-vip fep-server-nonvip fep-server-bizloan fep-server-digital fep-server-fido fep-server-onl fep-server-sso fep-server-graylist"

suipsrvUT=`ps -ef | grep suipsrv | grep -v grep | grep -v suipsrv1 | awk '{print $5" "$6}'`
suipsrv1UT=`ps -ef | grep suipsrv1 | grep -v grep | awk '{print $5" "$6 }'`

suipsrvPid=`ps -ef | grep suipsrv | grep -v grep | grep -v suipsrv1 | awk '{print $2}'`
suipsrv1Pid=`ps -ef | grep suipsrv1 | grep -v grep | awk '{print $2}'`

suipsrvUpBy=`ps -ef | grep suipsrv | grep -v grep | grep -v suipsrv1 | awk '{print $1}'`
suipsrv1UpBy=`ps -ef | grep suipsrv1 | grep -v grep | awk '{print $1}'`


echo "Current time: ${TimesTamp} Machine: ${HostName} Login ID: ${LOGIN}"
echo '========================= FEP System Management Service ==========================='
printf "|    Process Name     |  Up by  |     Uptime     |    PID    |  Restarted Today?  |\n"
echo '-----------------------------------------------------------------------------------'
if [[ -z $suipsrvPid ]]; then
   printf "| %-20s| is not running!\n" "suipA"
else
   printf "| %-20s| %-8s| %-15s| %-10s| %-19s|\n" "suipA" "$suipsrvUpBy" "$suipsrvUT" "$suipsrvPid" "-"
fi
if [[ -z $suipsrv1Pid ]]; then
   printf "| %-20s| is not running!\n" "suipB"
else
   printf "| %-20s| %-8s| %-15s| %-10s| %-19s|\n" "suipB" "$suipsrv1UpBy" "$suipsrv1UT" "$suipsrv1Pid" "-"
fi

function check {
for svc in $fepSystemService
do
	checkDetail "$svc"
done
echo ''
echo '======================== FEP Primary Transaction Server ==========================='
printf "|    Process Name     |  Up by  |     Uptime     |    PID    |  Restarted Today?  |\n"
echo '-----------------------------------------------------------------------------------'
for svc in $fepPrimServer
do
	checkDetail "$svc"
done
echo ''
echo '======================== FEP NON-ATM Transaction Server ==========================='
printf "|    Process Name     |  Up by  |     Uptime     |    PID    |  Restarted Today?  |\n"
echo '-----------------------------------------------------------------------------------'
for svc in $fepOtherService
do
	checkDetail "$svc"
done

}

function checkDetail {
	serverName=$1
    log_file="$log_base/$serverName/${serverName}-boot-${today}-0.log"
	SUT=`ps -ef | grep $optUserDir/$serverName | grep -v grep | awk '{print $5" "$6}'`
	Pid=`ps -ef | grep $optUserDir/$serverName | grep -v grep | awk '{print $2}'`
	UpBy=`ps -ef | grep $optUserDir/$serverName | grep -v grep | awk '{print $1}'`
	
	if [ "$serverName" = "fep-batch" ]; then
		SUT=`ps -ef | grep $optUserDir/$serverName | grep -v grep | grep -v fep-batch-cmdline | grep -v fep-batch-task | awk '{print $5" "$6}'`
		Pid=`ps -ef | grep $optUserDir/$serverName | grep -v grep | grep -v fep-batch-cmdline | grep -v fep-batch-task | awk '{print $2}'`
		UpBy=`ps -ef | grep $optUserDir/$serverName | grep -v grep | grep -v fep-batch-cmdline | grep -v fep-batch-task | awk '{print $1}'`
	fi
	
	if [ "$serverName" = "fep-service-cbstimeoutrerun" ]; then
		serverName="fep-cbstimeoutrerun"
	fi
	

	if [[ -z $Pid ]]; then
	# server is not running
		printf "| %-20s| is not running!\n" "$serverName"
		return
	else
	# server is running
    if [ ! -f "$log_file" ]; then
        bootStatus="No"
		printf "| %-20s| %-8s| %-15s| %-10s| %-19s|\n" "$serverName" "$UpBy" "$SUT" "$Pid" "$bootStatus"
        return
    fi
    if [ ! -s "$log_file" ]; then
        bootStatus="No"
		printf "| %-20s| %-8s| %-15s| %-10s| %-19s|\n" "$serverName" "$UpBy" "$SUT" "$Pid" "$bootStatus"
        return
    fi
    success_line=$(grep -E "Started .*Application" "$log_file" | tail -1)

    if [ -z "$success_line" ]; then
        bootStatus="Fail"
		printf "| %-20s| %-8s| %-15s| %-10s| %-19s|\n" "$serverName" "$UpBy" "$SUT" "$Pid" "$bootStatus"
        return
    fi

    log_time=`echo "$success_line" | sed -n 's/^\[\(.*\)\]\[main\].*/\1/p' | cut -d '.' -f1 | cut -d ' ' -f2`
	bootStatus="Yes - $log_time"
			
		printf "| %-20s| %-8s| %-15s| %-10s| %-19s|\n" "$serverName" "$UpBy" "$SUT" "$Pid" "$bootStatus"
	fi
}

check