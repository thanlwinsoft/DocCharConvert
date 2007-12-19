#!/bin/bash

#export CLASSPATH=../org.tigris.subversion.svnant/libs/javasvn.jar:../org.tigris.subversion.svnant/libs/svnant.jar:../org.tigris.subversion.svnant/libs/svnClientAdapter.jar:../org.tigris.subversion.svnant/libs/svnjavahl.jar:/opt/eclipse/plugins/org.eclipse.pde.build.svn_1.0.1.v20070222.jar:$CLASSPATH 
export CLASSPATH=/opt/eclipse/plugins/org.eclipse.pde.build.svn_1.0.1.v20070222.jar:$CLASSPATH
export EQUINOX_JAR=/opt/eclipse/plugins/org.eclipse.equinox.launcher_1.0.1.R33x_v20070828.jar
rm -rf tmp
if ! test -d tmp
then
mkdir tmp
cp -r maps tmp/
fi
#java -jar $EQUINOX_JAR -application org.eclipse.ant.core.antRunner -buildfile build-svn.xml

if java -cp $CLASSPATH -jar $EQUINOX_JAR -application org.eclipse.ant.core.antRunner -buildfile /opt/eclipse/plugins/org.eclipse.pde.build_3.3.2.R331_v20071019/scripts/build.xml -Dcomponent=svn-pde-build -Dbuilder=`pwd` $@
then
java -cp $CLASSPATH -jar $EQUINOX_JAR -application org.eclipse.ant.core.antRunner -buildfile /opt/eclipse/plugins/org.eclipse.pde.build_3.3.2.R331_v20071019/scripts/productBuild/productBuild.xml -Dbuilder=`pwd` $@
fi
