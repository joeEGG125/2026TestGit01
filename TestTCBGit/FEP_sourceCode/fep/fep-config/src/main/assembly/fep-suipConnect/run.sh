#!/bin/bash

optUser=syscom
optUserDir=/home/$optUser
jarFile=$optUserDir/fep-app/${project.artifactId}${assembly-func}/${project.artifactId}${assembly-func}.jar

java -jar -Dfile.encoding=UTF-8 $jarFile