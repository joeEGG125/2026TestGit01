#!/bin/sh

local appId=`ps -ef | grep suipsrv | grep -v grep | grep -v suipsrv1 | awk '{print $2}'`
if [[ ! -z "$appId" ]]; then
   kill -9 $appId
else 
   echo "The suipA is not running"
   exit 0
fi
local appId1=`ps -ef | grep suipsrv | grep -v grep | grep -v suipsrv1 | awk '{print $2}'`
if [[ -z "$appId1" ]]; then
   echo "The suipA stopped successfully"
else
   kill -kill $appId1
   local appId2=`ps -ef | grep suipsrv | grep -v grep | grep -v suipsrv1 | awk '{print $2}'`
   if [[ -z "$appId2" ]]; then
      echo "The suipA stopped successfully"
   else 
      echo "The suipA stopped failed"
   fi
fi
