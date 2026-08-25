@echo off
setlocal enabledelayedexpansion
set LIB_PATH=.
for %%i in (*.jar) do (
set LIB_PATH=!LIB_PATH!;%%i
)
java -cp "%LIB_PATH%" -Dfile.encoding=UTF-8 com.syscom.fep.batch.task.cmn.ArchivingTableFEPTXNHIS /TxDate:20240625 /DetachPartitionSh:feptxnhis_detachpartition.sh /ExportDataSh:feptxnhis_exportdata.sh
pause