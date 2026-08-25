@echo off
setlocal enabledelayedexpansion
set LIB_PATH=.
for %%i in (lib\*.jar) do (
set LIB_PATH=!LIB_PATH!;%%i
)
java -Dfile.encoding=UTF-8 -cp "%LIB_PATH%" com.syscom.fep.common.tools.JasyptTool
pause