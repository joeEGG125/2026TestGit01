#!/bin/sh
optUser=fep
fepFunctionPath=/$optUser/fep-app/bin/fep_function.sh
startTime=$(date '+%Y-%m-%d %H:%M:%S')
echo "========================================================================================"
echo "$(date '+%Y-%m-%d %H:%M:%S')"
counter=0
echo "========================================================================================"
echo "======================== Begin to Start Main Transaction Server ========================"
echo "========================================================================================"
sleep 2

local appId=`ps -ef | grep suipsrv | grep -v grep | grep -v suipsrv1 | awk '{print $2}'`

optUser=fep
cd /$optUser/fep-app/suip/exe
sudo -u fepap1 ./suipsrv

if [[ -z "$appId" ]]; then
  ps -ef | grep suipsrv | grep -v grep | grep -v suipsrv1 >/dev/null && echo "The suipA started successfully" || echo "The suipA started failed"
fi

local appId=`ps -ef | grep suipsrv1 | grep -v grep | awk '{print $2}'`

optUser=fep
cd /$optUser/fep-app/suip/exe
sudo -u fepap1 ./suipsrv1

if [ -z "$appId" ]; then
   ps -ef | grep suipsrv1 | grep -v grep >/dev/null && echo "The suipB started successfully" || echo "The suipB started failed"
fi

su - fepap1 -c "/fep/fep-app/fep-service-ems/fep_service_ems_start.sh" & 
su - fepap1 -c "/fep/fep-app/fep-service-log/fep_service_log_start.sh"
su - fepap1 -c "/fep/fep-app/fep-notify/fep_notify_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-batch/fep_batch_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-service-cbstimeoutrerun/fep_service_cbstimeoutrerun_start.sh"

su - fepap1 -c "/fep/fep-app/fep-gateway-cbs/fep_gateway_cbs_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-graylist/fep_server_graylist_start.sh"
#su - fepap1 -c "/fep/fep-app/fep-server-imsgw/fep_server_imsgw_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-atm/fep_server_atm_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-fisc/fep_server_fisc_start.sh"
clear
echo "========================================================================================"
echo "======================== Finished start main transaction Server ========================"
echo "========================================================================================"
sleep 2
echo "================ Wait 60s for Main Transaction Server Starting complete ================"

#waiting main standAlone start complete
	echo "========================================================================================"
while [ $counter -lt 20 ]
do
	sleep 3
	let counter=counter+1
    echo "Wait 30s for Main Transaction Server Starting complete loop_count = $counter"
done
echo "========================================================================================"
sleep 1
clear
echo "========================================================================================"
echo "======================== Begin to Start Other Transaction Server ======================="
echo "========================================================================================"
sleep 2
su - fepap1 -c "/fep/fep-app/fep-server-eatm/fep_server_eatm_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-posgw/fep_server_posgw_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-hce/fep_server_hce_start.sh"
echo "======================================= sleep 30s ======================================"
sleep 30
su - fepap1 -c "/fep/fep-app/fep-server-mft/fep_server_mft_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-mb/fep_server_mb_start.sh"
su - fepap1 -c "/fep/fep-app/fep-server-nb/fep_server_nb_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-mch/fep_server_mch_start.sh"
echo "======================================= sleep 30s ======================================"
sleep 30
su - fepap1 -c "/fep/fep-app/fep-server-eoi/fep_server_eoi_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-eip/fep_server_eip_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-pyBatch/fep_server_pyBatch_start.sh"
echo "======================================= sleep 30s ======================================"
sleep 30
su - fepap1 -c "/fep/fep-app/fep-server-vo/fep_server_vo_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-vip/fep_server_vip_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-nonvip/fep_server_nonvip_start.sh" 
echo "======================================= sleep 30s ======================================"
sleep 30
su - fepap1 -c "/fep/fep-app/fep-server-bizloan/fep_server_bizloan_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-digital/fep_server_digital_start.sh" 
su - fepap1 -c "/fep/fep-app/fep-server-fido/fep_server_fido_start.sh" &
su - fepap1 -c "/fep/fep-app/fep-server-onl/fep_server_onl_start.sh"
su - fepap1 -c "/fep/fep-app/fep-server-sso/fep_server_sso_start.sh"
echo "========================================================================================"

counter=0
clear
echo "======================== Finished All Start Method ====================================="
echo "run Start time: $startTime"
echo "Finish time: $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================================================================"
while [ $counter -lt 10 ]
do
	sleep 3
	let counter=counter+1
    echo "Waiting 30s for Other Transaction Servers starting complete... loop_count = $counter"
done
echo "========================================================================================"
sleep 2
clear
su - fepap1 -c "/fep/fep-app/bin/fep_all_status.sh"
echo "========================================================================================"

#su - fepap1 -c "/fep/fep-app/fep-service-appmon/fep_service_appmon_start.sh"
