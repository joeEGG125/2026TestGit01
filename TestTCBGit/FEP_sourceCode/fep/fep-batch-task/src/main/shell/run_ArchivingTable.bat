@echo off
setlocal enabledelayedexpansion
set LIB_PATH=.
for %%i in (*.jar) do (
set LIB_PATH=!LIB_PATH!;%%i
)
java -cp "%LIB_PATH%" -Dfile.encoding=UTF-8 com.syscom.fep.batch.task.cmn.ArchivingTable /TxDate:20240822 /TABLENAME:BITMAPDEF /ExporttableSh:fepdb_exporttable.sh /CopytableSh:fepdb_copytable.sh /ImporttableSh:fepdb_importtable.sh
pause