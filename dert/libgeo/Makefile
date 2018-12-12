# Build the libgeo native component of DERT
#
# 1.  Change to the libgeo directory.
# 2.  Edit the Makefile. Uncomment the setup for your platform and comment out the other.
# 3.  Type make clean.
# 4.  Type make.

# Setup for Mac
PLATFORM = macosx
#JAVA_INCLUDE = -I/System/Library/Frameworks/JavaVM.Framework/Headers
JAVA_INCLUDE = -I/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers
LIBEXT = jnilib
CFLAGS = -arch x86_64 -O2
LFLAGS = -arch x86_64 -fPIC -bundle -framework JavaVM
LIBS = -lz

# Setup for Linux
#PLATFORM = linux
# Redhat include directory for JNI
#JAVA_INCLUDE = -I/usr/lib/gcc/x86_64-redhat-linux/4.4.7/include
# Ubuntu include directory for JNI
#JAVA_INCLUDE = -I/usr/lib/jvm/java-8-openjdk-amd64/include -I/usr/lib/jvm/java-8-openjdk-amd64/include/linux
#LIBEXT = so
#CFLAGS = -O2 -fPIC
#LFLAGS = -shared -fPIC
#LIBS = -lz

# Native library
LIBGEO   = libgeo.$(LIBEXT)

# Definitions          
CC        = /usr/bin/cc
GCC       = /usr/bin/gcc

# Object library directory
LIBDIR = $(PLATFORM)

# Includes directory
INCDIR = include

#
#  Targets
#

all: $(LIBGEO)

clean:
	rm -f *.o
	rm -f $(LIBGEO)

GTIFJNI.o: GTIFJNI.c GTIFJNI.h
	rm -f $@
	$(GCC) -c $(CFLAGS) -I. -I$(INCDIR) $(JAVA_INCLUDE) GTIFJNI.c -o GTIFJNI.o
	@echo "$@ build is complete"

Proj4JNI.o: Proj4JNI.c Proj4JNI.h
	rm -f $@
	$(GCC) -c $(CFLAGS) -I. -I$(INCDIR) $(JAVA_INCLUDE) Proj4JNI.c -o Proj4JNI.o
	@echo "$@ build is complete"

$(LIBGEO): GTIFJNI.o Proj4JNI.o
	rm -f $@
	$(GCC) $(LFLAGS) GTIFJNI.o Proj4JNI.o $(LIBDIR)/libtiff.a $(LIBDIR)/libjpeg.a $(LIBDIR)/libproj.a -o $(LIBGEO) $(LIBS)
	@echo "$@ build is complete"
