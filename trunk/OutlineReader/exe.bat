@echo off

java -jar "C:\Users\sroux\Documents\NetBeansProjects\OutlineReader\dist\OutlineReader.jar" -u admin -p password -s localhost -a sample_u -d basic > output.txt

java -jar "C:\Users\sroux\Documents\NetBeansProjects\OutlineReader\dist\OutlineReader.jar" -h >> output.txt

rem java -jar "C:\Users\sroux\Documents\NetBeansProjects\OutlineReader\dist\OutlineReader.jar" -u admin -p password -s localhost -a sample -d basic -v "http://localhost:13080/aps/JAPI"> output.txt

pause