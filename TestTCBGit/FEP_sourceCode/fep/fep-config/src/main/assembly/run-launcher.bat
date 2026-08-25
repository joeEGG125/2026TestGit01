@echo off

java -Dfile.encoding=UTF-8 -cp ${project.artifactId}-${assembly-func}.jar -Dloader.path=. org.springframework.boot.loader.PropertiesLauncher

pause