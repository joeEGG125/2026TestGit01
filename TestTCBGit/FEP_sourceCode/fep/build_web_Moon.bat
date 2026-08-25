@echo off
SET JAVA_HOME=C:/Syscom/openjdk/jdk-17.0.5+8
SET MAVEN_HOME=C:/Users/bruce/apache-maven-3.8.5
SET PATH=%PATH%;%JAVA_HOME%/bin;%MAVEN_HOME%/bin
call mvn clean install -Dmaven.repo.local=C:/Syscom/builderRepository/builderWlp -pl fep-web -Pwar
pause