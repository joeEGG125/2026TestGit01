#!/bin/sh
local appId=`ps -ef | grep -i fepap1 | grep fep-service-appmon.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId

local appId=`ps -ef | grep -i fepap1 | grep fep-gateway-cbs.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-imsgw.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-atm.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-fisc.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-service-cbstimeoutrerun.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId

local appId=`ps -ef | grep -i fepap1 | grep fep-server-eatm.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-posgw.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-hce.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-mft.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-mb.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-nb.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-mch.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-eoi.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-eip.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-pyBatch.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-vo.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-vip.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-nonvip.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-bizloan.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-digital.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-fido.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-onl.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-sso.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-server-graylist.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId

local appId=`ps -ef | grep -i fepap1 | grep fep-batch.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-notify.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep suipsrv1 | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep suipsrv | grep -v grep | grep -v suipsrv1 | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId

local appId=`ps -ef | grep -i fepap1 | grep fep-service-ems.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-service-log.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
echo "Stopped All successfully"