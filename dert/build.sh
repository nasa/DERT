#!/bin/tcsh

# This script sets the JAVACMD environment variable for Ant to the Java 1.6 JDK on a Mac.
# Change the path for your machine.

# Set the JDK to 1.6 to make DERT compatible
setenv JAVACMD /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/java

echo $JAVACMD

set VERSION = "1.0b2"

ant -v -buildfile build_linux.xml clean -Dversion=$VERSION
ant -v -buildfile build_linux.xml main -Dversion=$VERSION

ant -v -buildfile build_osx.xml clean -Dversion=$VERSION
ant -v -buildfile build_osx.xml main -Dversion=$VERSION

# Set back to Java 1.8 to create the Mac app bundle.
setenv JAVACMD

echo $JAVACMD

ant -v -buildfile build_macapp.xml -Dversion=$VERSION
