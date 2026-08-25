#!/bin/bash

APPHOME=/home/syscom/BatchTask
TxDate=$1
DetachPartitionSh=$2
ExportDataSh=$3

CLASSPATH=.
for i in $(ls $APPHOME/*.jar); do
	CLASSPATH="$CLASSPATH":"$i"
done

if [ -z $TxDate ];then
	echo "Please input TxDate"
	read TxDate
fi

if [ -z $DetachPartitionSh ];then
	echo "Please input DetachPartition *.sh file path"
	read DetachPartitionSh
fi

if [ -z $ExportDataSh ];then
	echo "Please input ExportDataSh *.sh file path"
	read ExportDataSh
fi

cd $APPHOME
java -Dfile.encoding=UTF-8 -cp "$CLASSPATH" com.syscom.fep.batch.task.cmn.ArchivingTableFEPTXNHIS /TxDate:$TxDate /DetachPartitionSh:$DetachPartitionSh /ExportDataSh:$ExportDataSh