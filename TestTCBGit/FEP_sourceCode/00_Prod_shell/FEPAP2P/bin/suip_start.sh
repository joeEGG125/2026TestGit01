#!/bin/sh
local appId=`ps -ef | grep suipsrv | grep -v grep | grep -v suipsrv1 | awk '{print $2}'`

optUser=fep
cd /$optUser/fep-app/suip/exe
./suipsrv

if [[ -z "$appId" ]]; then
  ps -ef | grep suipsrv | grep -v grep | grep -v suipsrv1 >/dev/null && echo "The suipA started successfully" || echo "The suipA started failed"
fi
