@echo off
SET JAVA_HOME=D:/Java/ibm-semeru-open-jdk_x64_windows_21.0.2_13_openj9-0.43.0
SET MAVEN_HOME=D:/Development/apache-maven-3.8.5
SET PATH=%PATH%;%JAVA_HOME%/bin;%MAVEN_HOME%/bin
call mvn clean install -Dmaven.repo.local=E:/maven/repository -pl fep-web -Pwar -am
pause