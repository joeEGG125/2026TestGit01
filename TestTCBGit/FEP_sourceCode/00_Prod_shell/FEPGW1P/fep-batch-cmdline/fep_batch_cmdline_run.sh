#!/bin/sh

optUser=/fep
jarFile=$optUser/fep-app/fep-batch-cmdline/fep-batch-cmdline.jar

if [ "$1" = "" ] \
		|| [ "$1" = "h" ] \
		|| [ "$1" = "help" ] \
		|| [ "$1" = "?" ]; then
		java -jar -Dfile.encoding=UTF-8 $jarFile -h
else
		java -jar -Dfile.encoding=UTF-8 $jarFile -p $1 -a "$2"
fi
