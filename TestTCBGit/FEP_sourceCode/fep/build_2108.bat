@echo off
echo Hello1
echo %TIME%
echo Hello2
SET JAVA_HOME=D:/MyData/other/jdk/jdk21/jdk-21.0.8
SET MAVEN_HOME=D:/maven/apache-maven-3.8.8
SET PATH=%PATH%;%JAVA_HOME%/bin;%MAVEN_HOME%/bin
echo Hello3
echo %TIME%
echo Hello4
call mvn clean install -Dmaven.repo.local=D:/maven/reponew3 -f pom.xml
echo Hello5
echo %TIME%
echo Hello6
::call mvn clean install -Dmaven.repo.local=D:/maven/reponew3 -pl fep-common -f pom.xml
call mvn clean install -Dmaven.repo.local=D:/maven/reponew3 -pl fep-web -Pwar
pause