@echo off
SET JAVA_HOME=D:/MyData/other/jdk/jdk21/jdk-21.0.8
SET MAVEN_HOME=D:/maven/apache-maven-3.8.8
SET PATH=%PATH%;%JAVA_HOME%/bin;%MAVEN_HOME%/bin
call mvn clean install -Dmaven.repo.local=D:/maven/reponew3 -pl fep-batch -am
pause