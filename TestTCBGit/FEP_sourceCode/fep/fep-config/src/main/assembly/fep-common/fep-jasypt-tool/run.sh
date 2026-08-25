#!/bin/bash

APPHOME=/home/syscom/fep-app/${assembly-func}

CLASSPATH=.
for i in $(ls $APPHOME/lib/*.jar); do
	CLASSPATH="$CLASSPATH":"$i"
done

java -Dfile.encoding=UTF-8 -cp "$CLASSPATH" com.syscom.fep.common.tools.JasyptTool