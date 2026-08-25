@echo off

java -Dfile.encoding=UTF-8 -Xms${assembly-jvm-xms} -Xmx${assembly-jvm-xmx} -XX:MetaspaceSize=${assembly-jvm-metaspaceSize} -XX:MaxMetaspaceSize=${assembly-jvm-metaspaceSize} -XX:-UseGCOverheadLimit -XX:+HeapDumpOnOutOfMemoryError -jar ${project.artifactId}${assembly-func}.jar

pause