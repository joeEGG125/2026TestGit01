@echo off
SET JAVA_HOME=C:\Program Files\Java\jdk-17.0.5+8
SET MAVEN_HOME=C:\Program Files\apache-maven-3.8.5
SET PATH=%PATH%;%JAVA_HOME%/bin;%MAVEN_HOME%/bin
call mvn clean install -Dmaven.repo.local=D:/maven/repository -f pom.xml
rem call mvn clean install -Dmaven.repo.local=D:/maven/repository -pl fep-web -Pwar
pause