call mvn package
if %errorlevel% neq 0 goto :eof

java -jar target\jad-1.0-SNAPSHOT-jar-with-dependencies.jar %*
