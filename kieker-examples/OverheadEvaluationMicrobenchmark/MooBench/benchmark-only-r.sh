#!/bin/bash

JAVABIN=""

RSCRIPTDIR=r/
BASEDIR=./
RESULTSDIR="${BASEDIR}tmp/results-kieker/"

SLEEPTIME=30            ## 30
NUM_LOOPS=10            ## 10
THREADS=1               ## 1
RECURSIONDEPTH=10       ## 10
TOTALCALLS=2000000      ## 2000000
METHODTIME=0       ## 500000

MOREPARAMS="--quickstart"
MOREPARAMS="${MOREPARAMS} -r kieker.Logger"

TIME=`expr ${METHODTIME} \* ${TOTALCALLS} / 1000000000 \* 4 \* ${RECURSIONDEPTH} \* ${NUM_LOOPS} + ${SLEEPTIME} \* 4 \* ${NUM_LOOPS}  \* ${RECURSIONDEPTH} + 50 \* ${TOTALCALLS} / 1000000000 \* 4 \* ${RECURSIONDEPTH} \* ${NUM_LOOPS} `
echo "Experiment will take circa ${TIME} seconds."

echo "Removing and recreating '$RESULTSDIR'"
(rm -rf ${RESULTSDIR}) && mkdir ${RESULTSDIR}
#mkdir ${RESULTSDIR}stat/

# Clear kieker.log and initialize logging
rm -f ${BASEDIR}kieker.log
touch ${BASEDIR}kieker.log

RAWFN="${RESULTSDIR}raw"

JAVAARGS="-server"
JAVAARGS="${JAVAARGS} -d64"
JAVAARGS="${JAVAARGS} -Xms1G -Xmx4G"
#JAVAARGS="${JAVAARGS} -verbose:gc -XX:+PrintCompilation"
#JAVAARGS="${JAVAARGS} -XX:+PrintInlining"
#JAVAARGS="${JAVAARGS} -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation"
#JAVAARGS="${JAVAARGS} -Djava.compiler=NONE"
JAR="-jar MooBench.jar -a mooBench.monitoredApplication.MonitoredClassSimple"

JAVAARGS_NOINSTR="${JAVAARGS}"
JAVAARGS_LTW="${JAVAARGS} -javaagent:${BASEDIR}lib/kieker-1.12-SNAPSHOT-aspectj.jar -Dorg.aspectj.weaver.showWeaveInfo=false -Daj.weaving.verbose=false -Dkieker.monitoring.skipDefaultAOPConfiguration=true -Dorg.aspectj.weaver.loadtime.configuration=META-INF/kieker.aop.xml"
JAVAARGS_KIEKER_DEACTV="${JAVAARGS_LTW} -Dkieker.monitoring.enabled=false -Dkieker.monitoring.writer=kieker.monitoring.writer.DummyWriter"
JAVAARGS_KIEKER_NOLOGGING="${JAVAARGS_LTW} -Dkieker.monitoring.writer=kieker.monitoring.writer.DummyWriter"
JAVAARGS_KIEKER_LOGGING_ASCII="${JAVAARGS_LTW} -Dkieker.monitoring.writer=kieker.monitoring.writer.filesystem.AsyncFsWriter -Dkieker.monitoring.writer.filesystem.AsyncFsWriter.customStoragePath=${BASEDIR}tmp"
JAVAARGS_KIEKER_LOGGING_BIN="${JAVAARGS_LTW} -Dkieker.monitoring.writer=kieker.monitoring.writer.filesystem.AsyncBinaryFsWriter -Dkieker.monitoring.writer.filesystem.AsyncBinaryFsWriter.customStoragePath=${BASEDIR}tmp"
JAVAARGS_KIEKER_LOGGING_TCP="${JAVAARGS_LTW} -Dkieker.monitoring.writer=kieker.monitoring.writer.tcp.TCPWriter"

## Write configuration
uname -a >${RESULTSDIR}configuration.txt
${JAVABIN}java ${JAVAARGS} -version 2>>${RESULTSDIR}configuration.txt
echo "JAVAARGS: ${JAVAARGS}" >>${RESULTSDIR}configuration.txt
echo "" >>${RESULTSDIR}configuration.txt
echo "Runtime: circa ${TIME} seconds" >>${RESULTSDIR}configuration.txt
echo "" >>${RESULTSDIR}configuration.txt
echo "SLEEPTIME=${SLEEPTIME}" >>${RESULTSDIR}configuration.txt
echo "NUM_LOOPS=${NUM_LOOPS}" >>${RESULTSDIR}configuration.txt
echo "TOTALCALLS=${TOTALCALLS}" >>${RESULTSDIR}configuration.txt
echo "METHODTIME=${METHODTIME}" >>${RESULTSDIR}configuration.txt
echo "THREADS=${THREADS}" >>${RESULTSDIR}configuration.txt
echo "RECURSIONDEPTH=${RECURSIONDEPTH}" >>${RESULTSDIR}configuration.txt
sync

## Generate Results file
R --vanilla --silent <<EOF
results_fn="${RAWFN}"
outtxt_fn="${RESULTSDIR}results-text.txt"
outcsv_fn="${RESULTSDIR}results-text.csv"
configs.loop=${NUM_LOOPS}
configs.recursion=c(${RECURSIONDEPTH})
configs.labels=c("No Probe","Deactivated Probe","Collecting Data","Writer (ASCII)", "Writer (Bin)", "Writer (TCP)")
results.count=${TOTALCALLS}
results.skip=${TOTALCALLS}/2
source("${RSCRIPTDIR}stats.csv.r")
EOF

