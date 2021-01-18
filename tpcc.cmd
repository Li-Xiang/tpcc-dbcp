@echo off
setlocal
mode con cols=176 lines=60

rem ===== 设置程序的类库目录, 工作目录 ======
rem 设置覆盖环境变量设定的JAVA信息
rem set JAVA_HOME=D:\Program Files\Java\jdk1.8.0_261
rem set JAVA_HOME=D:\Program Files\jdk-11.0.7
rem set PATH=%JAVA_HOME%\bin;%PATH%

rem 设置批处理程序所在的目录为当前目录
set APP_HOME=%~dp0
set APP_LIB="%APP_HOME%*;.;%APP_HOME%lib\*"
set MAIN_CLASS=org.littlestar.tpcc.Tpcc

cd %APP_HOME%

rem ===== 获取JAVA的版本信息, 并检查Java版本 =====
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VER_STR=%%g
)

rem 删除可能有的空白符
set JAVA_VER_STR=%JAVA_VER_STR:"=%

for /f "delims=. tokens=1-3" %%v in ("%JAVA_VER_STR%") do (
	if "%%v"=="1" (
		set MAJOR_JAVA_VER=%%w 
	) else (
		set MAJOR_JAVA_VER=%%v
	)
)

rem 检查Java版本.
if %MAJOR_JAVA_VER% LSS 8 (
  echo %0: Java version '%JAVA_VER_STR%' is too low, needs Java 8 or later.
  goto :eof
)

rem =====> JVM参数设置 <=====

rem Always dump on OOM.
set DUMP=-XX:+HeapDumpOnOutOfMemoryError

rem Generate GC verbose file with Java 9 and above.
set VERBOSE_GC_A9=-Xlog:gc*,gc+age=trace,gc+heap=debug:file=tpcc_gc_%%p.log

rem Generate GC verbose file with Java prior to 9.
set VERBOSE_GC_P9=-verbose:gc -Xloggc:tpcc_gc_%%p.log -XX:+PrintGCDetails -XX:+PrintGCCause -XX:+PrintTenuringDistribution -XX:+PrintHeapAtGC -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDateStamps -XX:+PrintAdaptiveSizePolicy

rem JAVA 8,9,10, with G1GC.
set JAVA8_OPTS=-XX:+UseG1GC -XX:+AggressiveOpts -XX:MaxGCPauseMillis=100

rem JAVA 11,12,13,14, with ZGC.
set JAVA11_OPTS=-XX:+UnlockExperimentalVMOptions -XX:+UseZGC

rem Basic Options for all JAVA version.
set JVM_OPTS=-Xmx4G -Xms1G -XX:MaxMetaspaceSize=256m -server -XX:+UseStringDeduplication 

if %MAJOR_JAVA_VER% GEQ 8 if %MAJOR_JAVA_VER% LSS 11 (
  set JVM_OPTS=%JVM_OPTS% %JAVA8_OPTS% %DUMP%
)

if %MAJOR_JAVA_VER% GEQ 11 if %MAJOR_JAVA_VER% LSS 15 (
  set JVM_OPTS=%JVM_OPTS% %JAVA11_OPTS% %DUMP%
)

rem =====> 执行Java程序 <=====
rem "java %JVM_OPTS% -cp %APP_LIB% %MAIN_CLASS% %*"
java %JVM_OPTS% -cp %APP_LIB% %MAIN_CLASS% %*

