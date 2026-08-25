#!/bin/bash

APPHOME=/home/syscom/BatchTask
Partition=$1
TxDate=$2

CLASSPATH=.
for i in $(ls $APPHOME/*.jar); do
	CLASSPATH="$CLASSPATH":"$i"
done

if [ -z $Partition ];then
	echo "Please input Partition"
	read Partition
fi

if [ -z $TxDate ];then
	echo "Please input TxDate"
	read TxDate
fi

cd $APPHOME
java -Dfile.encoding=UTF-8 -cp "$CLASSPATH" com.syscom.fep.batch.task.cmn.CreateFEPTXNPartition /TxDate:$TxDate /Partition:$Partition