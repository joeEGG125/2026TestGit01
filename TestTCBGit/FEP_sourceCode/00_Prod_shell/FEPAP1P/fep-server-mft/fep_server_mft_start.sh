#!/bin/sh

optUser=fep
fepFunctionPath=/$optUser/fep-app/bin/fep_function.sh
$fepFunctionPath start fep-server-mft.jar 2048m 2048m '' '' ''
