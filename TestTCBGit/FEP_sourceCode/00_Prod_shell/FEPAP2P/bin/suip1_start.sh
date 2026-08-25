#!/bin/sh

local appId=`ps -ef | grep suipsrv1 | grep -v grep | awk '{print $2}'`

optUser=fep
cd /$optUser/fep-app/suip/exe
./suipsrv1

if [ -z "$appId" ]; then
   ps -ef | grep suipsrv1 | grep -v grep >/dev/null && echo "The suipB started successfully" || echo "The suipB started failed"
fi
