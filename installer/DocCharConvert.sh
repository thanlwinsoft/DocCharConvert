#!/bin/sh
export OPENOFFICE_BASE=%{oo_home}
export CLASSPATH=%{INSTALL_PATH}/DocCharConvert.jar:$CLASSPATH
for i in $OPENOFFICE_BASE/program/classes/*.jar;
do CLASSPATH=$CLASSPATH:$i ;
done
%{JAVA_HOME}/java DocCharConvert/MainForm $@

