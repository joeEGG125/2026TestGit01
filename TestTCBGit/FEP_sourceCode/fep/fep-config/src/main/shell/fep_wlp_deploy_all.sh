#!/bin/bash

optUser=syscom
optUserDir=/home/$optUser

function deployWlpSevice() {
  if [ "$USER" != "$optUser" -o "$HOME" != "$optUserDir" ]; then
    echo "Warning! you must be user $optUser to deploy WARS..."
    exit 1
  fi
  local appName=$1
  if [ -e /home/$optUser/fep-app/war/$appName.war ]; then
    /home/$optUser/wlp/bin/server stop $appName
    cp -a /home/$optUser/fep-app/war/$appName.war /home/$optUser/wlp/usr/servers/$appName/apps/$appName.war
    rm -rf /home/$optUser/wlp/usr/servers/$appName/apps/$appName
    unzip /home/$optUser/wlp/usr/servers/$appName/apps/$appName.war -d /home/$optUser/wlp/usr/servers/$appName/apps/$appName
    rm /home/$optUser/wlp/usr/servers/$appName/apps/$appName.war
    rm -rf /home/$optUser/wlp/usr/servers/$appName/logs/
    rm -rf /home/$optUser/wlp/usr/servers/$appName/workarea/
    /home/$optUser/wlp/bin/server start $appName
  else
    echo "File /home/$optUser/fep-app/war/$appName.war not exist!!!"
  fi
}

#deployWlpSevice fep-server
#deployWlpSevice fep-service-ems
#deployWlpSevice fep-service-appmon
deployWlpSevice fep-web
#deployWlpSevice fep-batch
