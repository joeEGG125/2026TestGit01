@echo off
setlocal enabledelayedexpansion
set LIB_PATH=.
for %%i in (*.jar) do (
set LIB_PATH=!LIB_PATH!;%%i
)
java -cp "%LIB_PATH%" -Dfile.encoding=UTF-8 com.syscom.fep.batch.task.cmn.ArchivingTableFEPTXN /TxDate:20240625 /DetachPartitionSh:feptxn_detachpartition.sh /ExportDataSh:feptxn_exportdata.sh /ImportDataSh:feptxn_importdata.sh
pause