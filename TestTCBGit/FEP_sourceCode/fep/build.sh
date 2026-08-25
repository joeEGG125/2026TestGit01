#!/bin/bash

JAVA_HOME=/home/syscom/ibm-semeru-open-jdk_x64_windows_17.0.5_8_openj9-0.35.0
export PATH=${PATH}:${JAVA_HOME}/bin

MAVEN_HOME=/home/syscom/apache-maven-3.8.5
export PATH=${PATH}:${MAVEN_HOME}/bin

mvn clean install -f pom.xml
mvn clean install -pl fep-web -Pwar