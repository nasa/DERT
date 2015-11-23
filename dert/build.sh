#!/bin/tcsh

# This script sets the JAVACMD environment variable for Ant to the Java 1.6 JDK on a Mac.
# Change the path for your machine.

# Set the JDK to 1.6 to make DERT compatible
setenv JAVACMD /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/java

echo $JAVACMD

ant -v -buildfile build_linux.xml clean
ant -v -buildfile build_linux.xml main

ant -v -buildfile build_osx.xml clean
ant -v -buildfile build_osx.xml main

# Set back to Java 1.8 to create the Mac app bundle.
setenv JAVACMD

echo $JAVACMD

ant -v -buildfile build_macapp.xml
