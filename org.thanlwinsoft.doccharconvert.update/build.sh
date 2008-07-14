export CLASSPATH=../org.tigris.subversion.svnant/libs/javasvn.jar:../org.tigris.subversion.svnant/libs/svnant.jar:../org.tigris.subversion.svnant/libs/svnClientAdapter.jar:../org.tigris.subversion.svnant/libs/svnjavahl.jar:$CLASSPATH
#export EQUINOX_JAR=/opt/eclipse/plugins/org.eclipse.equinox.launcher_1.0.1.R33x_v20070828.jar
export EQUINOX_JAR=/opt/eclipse/plugins/org.eclipse.equinox.launcher_1.0.1.R33x_v20080118.jar
rm -rf plugins features
mkdir -p plugins
mkdir -p features
if ! test -f ../org.thanlwinsoft.doccharconvert/lib/DocCharConvertSchema.jar
then
    cd ../org.thanlwinsoft.doccharconvert
    ./genSchemaJar.sh
    cd -
fi
# run create ant build file inside eclipse for each feature and run ant build before running this
java -jar $EQUINOX_JAR -application org.eclipse.ant.core.antRunner -buildfile build-svn.xml

