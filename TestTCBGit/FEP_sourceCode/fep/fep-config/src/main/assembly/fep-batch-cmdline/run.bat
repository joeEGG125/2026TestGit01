@echo off

set programName=%1
set arguments=%2
set callJob=%3

set workingDir=%~dp0
set jarFile=%workingDir%/${project.artifactId}${assembly-func}.jar

cd %workingDir%

if "%programName%"=="" goto help
if "%programName%"=="h" goto help
if "%programName%"=="help" goto help
if "%programName%"=="?" goto help

java -Dfile.encoding=UTF-8 -Xms${assembly-jvm-xms} -Xmx${assembly-jvm-xmx} -XX:MetaspaceSize=${assembly-jvm-metaspaceSize} -XX:MaxMetaspaceSize=${assembly-jvm-metaspaceSize} -XX:-UseGCOverheadLimit -XX:+HeapDumpOnOutOfMemoryError -jar %jarFile% -p %programName% -a %arguments% -c %callJob%

goto finish

:help
java -Xms${assembly-jvm-xms} -Xmx${assembly-jvm-xmx} -XX:MetaspaceSize=${assembly-jvm-metaspaceSize} -XX:MaxMetaspaceSize=${assembly-jvm-metaspaceSize} -XX:-UseGCOverheadLimit -XX:+HeapDumpOnOutOfMemoryError -jar %jarFile% -h
goto finish

:finish
pause