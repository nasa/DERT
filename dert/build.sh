#!/bin/tcsh

# This script sets the JAVACMD environment variable for Ant to the Java 1.6 JDK on a Mac.
# Change the path for your machine.

# Set the JDK to 1.6 to make DERT compatible
setenv JAVACMD /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/java

echo $JAVACMD

set DERT_VERSION = "1.0b4"

ant -v -buildfile build_linux.xml clean -Ddertversion=$DERT_VERSION
ant -v -buildfile build_linux.xml main -Ddertversion=$DERT_VERSION

ant -v -buildfile build_osx.xml clean -Ddertversion=$DERT_VERSION
ant -v -buildfile build_osx.xml main -Ddertversion=$DERT_VERSION

# Set back to Java 1.8 to create the Mac app bundle.
setenv JAVACMD

echo $JAVACMD

ant -v -buildfile build_macapp.xml -Ddertversion=$DERT_VERSION

zip -r dert_${DERT_VERSION}_linux.zip dert_${DERT_VERSION}_linux
chmod 644 dert_${DERT_VERSION}_linux.zip
zip -r dert_${DERT_VERSION}_osx.zip dert_${DERT_VERSION}_osx
chmod 644 dert_${DERT_VERSION}_osx.zip

echo DERT build complete
