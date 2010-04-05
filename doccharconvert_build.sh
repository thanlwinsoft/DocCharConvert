#!/bin/bash

if test -z "${ECLIPSE_HOME}";
then
    if test -d /home/keith/eclipse/eclipse;
    then
        ECLIPSE_HOME=/home/keith/eclipse/eclipse
    elif test -d /opt/eclipse-3.5;
    then
        ECLIPSE_HOME=/opt/eclipse-3.5
    elif test -d /usr/lib/eclipse;
    then
        ECLIPSE_HOME=/usr/lib/eclipse;
    else
        echo "Eclipse Home not found. Please set ECLIPSE_HOME";
        exit -1
    fi
fi

# DOCCHARCONVERT=/home/keith/DocCharConvert
if ! test -d ${ECLIPSE_HOME};
then
    ECLIPSE_HOME=/opt/eclipse-3.5
fi
DOCCHARCONVERT=`dirname $0`
WEBDIR=/var/www/ThanLwinSoft/Downloads/Converters

cd $DOCCHARCONVERT
hg pull -u > /dev/null
export JAVA_HOME=/usr/lib/jvm/java-6-sun
PATH=/usr/lib/jvm/java-6-sun/bin:$PATH
if  ! test -f build.version || test `hg id -n` -gt `cat build.version`; 
then
    if nice ant -f ${DOCCHARCONVERT}/org.thanlwinsoft.doccharconvert.update/headless-build/build.xml -Dosgi.os=linux -Dosgi.arch=x86 -Dosgi.ws=gtk -Declipse.home=${ECLIPSE_HOME} -Dtimestamp=`date +%Y%m%d%H%m` $@;
    then
        if test -d ${WEBDIR};
        then
            cp doccharconvert/*.exe $WEBDIR
            cp doccharconvert/*.tar.bz2 $WEBDIR
            cp doccharconvert/I.DocCharConvert/*.tar.bz2 $WEBDIR
        else
            echo ${WEBDIR} not found.
        fi
        hg id -n > build.version
    fi
fi

