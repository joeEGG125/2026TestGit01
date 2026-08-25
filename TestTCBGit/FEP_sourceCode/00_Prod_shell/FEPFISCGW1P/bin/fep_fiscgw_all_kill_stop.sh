#!/bin/sh
clear
local appId=`ps -ef | grep -i fepap1 | grep fep-gateway-fisc.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
local appId=`ps -ef | grep -i fepap1 | grep fep-gateway-fisc-agent.jar | grep -v grep | grep -v wasadmin | grep -v itmadm | pg | awk '{print $2}'`
kill -9 $appId
echo "Stopped FISCGW All successfully"


