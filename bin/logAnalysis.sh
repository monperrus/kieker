#!/bin/bash

#
# Wrapper script needs improvement!
#

BINDIR=$(dirname $0)

JAVAARGS=-Dlog4j.configuration=${BINDIR}/log4j.properties
MAINCLASSNAME=kieker.loganalysis.LogAnalysisTool
CLASSPATH=$(ls lib/*.jar | tr "\n" ":")$(ls dist/*.jar | tr "\n" ":")

java ${JAVAARGS} -cp "${CLASSPATH}" ${MAINCLASSNAME} $*