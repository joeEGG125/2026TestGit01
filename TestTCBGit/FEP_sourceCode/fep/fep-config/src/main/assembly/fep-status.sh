#!/bin/bash

optUser=syscom
fepFunctionPath=/home/$optUser/fep-app/bin/fep_function.sh
$fepFunctionPath status ${project.artifactId}${assembly-func}.jar