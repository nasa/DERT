#!/bin/sh

# Set the JAVACMD environment variable for Ant to a Java JDK other than the default.
#export JAVACMD=/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home/bin/java
#export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home

DERT_VERSION="1.0.2"

ant -v -buildfile build_linux.xml clean -Ddertversion=$DERT_VERSION
ant -v -buildfile build_linux.xml main -Ddertversion=$DERT_VERSION

ant -v -buildfile build_osx.xml clean -Ddertversion=$DERT_VERSION
ant -v -buildfile build_osx.xml main -Ddertversion=$DERT_VERSION

ant -v -buildfile build_macapp.xml -Ddertversion=$DERT_VERSION

zip -r dert_${DERT_VERSION}_linux.zip dert_${DERT_VERSION}_linux
chmod 644 dert_${DERT_VERSION}_linux.zip
zip -r dert_${DERT_VERSION}_osx.zip dert_${DERT_VERSION}_osx
chmod 644 dert_${DERT_VERSION}_osx.zip

echo DERT build complete
