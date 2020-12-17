@echo off
mode con cols=176 lines=60 
set APP_HOME=%~dp0
set APP_LIB="%APP_HOME%*;.;%APP_HOME%lib\*"

"D:\Program Files\jdk-11.0.7\bin\java.exe" -Xms256m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication -cp %APP_LIB%  org.littlestar.tpcc.Tpcc %*