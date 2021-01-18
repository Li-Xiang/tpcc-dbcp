#!/bin/bash

# ===== 设置程序的类库目录, 工作目录 ======
#JAVA_HOME=/usr/local/jdk-11.0.9.1+1
#JAVA_HOME=/usr/local/jdk1.8.0_251
#JAVACMD=$JAVA_HOME/bin/java
JAVACMD=java

PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
  PRG="$link"
  else
  PRG=`dirname "$PRG"`"/$link"
  fi
done

APP_HOME=`dirname "$PRG"`
APP_LIB="${APP_HOME}/*:.:${APP_HOME}/lib/*"
MAIN_CLASS=org.littlestar.tpcc.Tpcc

cd $APP_HOME

# ===== 获取JAVA的版本信息, 并检查Java版本 =====
JAVA_VER_STR=$("$JAVACMD" -version 2>&1 | awk -F[\"\-] '/version/ {print $2}')

MAJOR_JAVA_VER="${JAVA_VER_STR%%.*}"

if [ ${MAJOR_JAVA_VER} -eq 1 ]; then
  MAJOR_JAVA_VER=$(awk -F[\"\.] '{print $2}' <<< ${JAVA_VER_STR})
fi

# 检查Java版本.
if [ $MAJOR_JAVA_VER -lt 8 ]; then
  echo $0: Java version '$JAVA_VER_STR' is too low, needs Java 8 or later.
  exit 1
fi

# ===== JVM参数设置 =====

# Always dump on OOM.
DUMP="-XX:+HeapDumpOnOutOfMemoryError"

# Generate GC verbose file with Java 9 and above.
VERBOSE_GC_A9="-Xlog:gc*,gc+age=trace,gc+heap=debug:file=tpcc_gc_%%p.log"

# Generate GC verbose file with Java prior to 9.
VERBOSE_GC_P9="-verbose:gc -Xloggc:your_app_name_gc_%%p.log -XX:+PrintGCDetails -XX:+PrintGCCause -XX:+PrintTenuringDistribution -XX:+PrintHeapAtGC -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDateStamps -XX:+PrintAdaptiveSizePolicy"

# JAVA 8,9,10, with G1GC.
JAVA8_OPTS="-XX:+UseG1GC -XX:+AggressiveOpts"

# JAVA 11,12,13,14, with ZGC.
JAVA11_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseZGC"

# Basic Options for all JAVA version.
JVM_OPTS="-Xmx4G -Xms1G -XX:MaxMetaspaceSize=256m -server -XX:+UseStringDeduplication" 

if [ $MAJOR_JAVA_VER -ge 8 ] && [ $MAJOR_JAVA_VER -lt 11 ]; then
    JVM_OPTS="${JVM_OPTS} ${JAVA8_OPTS} ${DUMP}"
elif [ $MAJOR_JAVA_VER -ge 11 ] && [ $MAJOR_JAVA_VER -lt 15 ]; then
    JVM_OPTS="${JVM_OPTS} ${JAVA11_OPTS} ${DUMP}"
fi

eval ${JAVACMD} ${JVM_OPTS} -cp \"${APP_LIB}\" ${MAIN_CLASS} $@
