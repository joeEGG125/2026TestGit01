#!/bin/bash

APPHOME=/home/syscom/BatchTask
TxDate=$1
TABLENAME=$2
ExporttableSh=$3
CopytableSh=$4
ImporttableSh=$5

CLASSPATH=.
for i in $(ls $APPHOME/*.jar); do
	CLASSPATH="$CLASSPATH":"$i"
done

if [ -z $TxDate ];then
	echo "Please input TxDate"
	read TxDate
fi

if [ -z $TABLENAME ];then
	echo "Please input TABLENAME"
	read TABLENAME
fi

if [ -z $ExporttableSh ];then
	echo "Please input Export Table *.sh file path"
	read ExporttableSh
fi

if [ -z $CopytableSh ];then
	echo "Please input Copy Table *.sh file path"
	read CopytableSh
fi

if [ -z $ImporttableSh ];then
	echo "Please input Import Table *.sh file path"
	read ImporttableSh
fi

cd $APPHOME
java -Dfile.encoding=UTF-8 -cp "$CLASSPATH" com.syscom.fep.batch.task.cmn.ArchivingTable /TxDate:$TxDate /TABLENAME:$TABLENAME /ExporttableSh:$ExporttableSh /CopytableSh:$CopytableSh /ImporttableSh:$ImporttableSh