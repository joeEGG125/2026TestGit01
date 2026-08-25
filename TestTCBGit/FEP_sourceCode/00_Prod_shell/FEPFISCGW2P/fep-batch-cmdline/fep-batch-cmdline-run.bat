@echo off

set programName=%1
set arguments=%2
set jarFile=fep-batch-cmdline.jar

if "%programName%"=="" goto help
if "%programName%"=="h" goto help
if "%programName%"=="help" goto help
if "%programName%"=="?" goto help

java -jar %jarFile% -p %programName% -a %arguments%

goto finish

:help
java -jar %jarFile% -h
goto finish

:finish
pause