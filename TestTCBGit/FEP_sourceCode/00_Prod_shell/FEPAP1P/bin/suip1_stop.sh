#!/bin/sh

local appId=`ps -ef | grep suipsrv1 | grep -v grep | awk '{print $2}'`
if [[ ! -z "$appId" ]]; then
   kill -9 $appId
else 
   echo "The suipB is not running"
   exit 0
fi
local appId1=`ps -ef | grep suipsrv1 | grep -v grep | awk '{print $2}'`
if [[ -z "$appId1" ]]; then
   echo "The suipB stopped successfully"
else
   kill -kill $appId1
   local appId2=`ps -ef | grep suipsrv1 | grep -v grep | awk '{print $2}'`
   if [[ -z "$appId2" ]]; then
      echo "The suipB stopped successfully"
   else 
      echo "The suipB stopped failed"
   fi
fi
