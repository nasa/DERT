DERT
Desktop Exploration of Remote Terrain
2016


SYSTEM REQUIREMENTS:

DERT requires 64-bit Java 1.6, 1.7, or 1.8.  Mac OS X 10.8 and Red Hat Linux 6 have
been tested successfully.  A 3-button mouse is recommended.


INSTALLATION:

Install DERT by unzipping the zip file and placing the resulting directory in your desired location.
The directory can be placed anywhere but should remain intact. 
You may edit the dert script file to set the Java path and/or change the memory allocation.

DERT may be installed in a central location for multiple users. 
Put the dert directory in the user's path.

WARNING: The path to dert should not contain any spaces.

A script called "get_kernels" is included. Run this script to download SPICE kernels from NAIF if
the kernels subdirectory of the dert directory is empty or absent.


EXECUTION:

To execute DERT on a Mac with Java 1.7 or later double-click on the dert application or run the dert script found in the directory.
To execute DERT on a Mac with Java 1.6 only, run the dert script found in the directory.
To execute DERT on Linux, run the dert script.  See the user guide for instructions.

To execute LayerFactory run the layerfactory script found in the dert directory.
See the user guide for a description of parameters.


MEMORY ALLOCATION:

The maximum memory allocation for DERT is 2 GB and LayerFactory is set to 8 GB.
Java will try to allocate this much virtual memory.
The maximum memory can be modified by changing MAX_MEM in the dert and layerfactory scripts.
For the Mac app, right-click on dert.app, select "Show Package Contents" from the context menu.
Open the Contents directory and double-click on Info.plist to edit.
Open JVMOptions and change the -Xmx memory option. Save.


PREFERENCES:

There are a number of properties (defaults) defined in the dert.properties file.
This file is used by both applications.  Changes to this file require restarting the application.
The dert.properties file can be found in the installation directory on Linux and in
the dert.app/Contents/Java directory on the Mac.


ACKNOWLEDGEMENTS:

Funding:
    Mars Reconnaissance Orbiter Project (MRO)

Advice, Preliminary Testing, and Feedback:
    Malin Space Science Systems (MSSS)
    NASA Intelligent Systems Antares Team
    NASA Intelligent Systems MapMakers Team

Software techniques developed for and funded by:
    Applied Information Systems Research Program (AISRP)
    Phoenix Mars Lander Project (PML)
    Mars Reconnaissance Orbiter Project (MRO)
    Mars Science Laboratory Project (MSL)
    Lunar Atmosphere and Dust Environment Explorer Project (LADEE)
 
    
LICENSE:

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free, open source libraries. These libraries and their notices are listed below. Find the complete third party license listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the LICENSE folder.
 
JogAmp Ardor3D Continuation
Copyright © 2008-2012 Ardor Labs, Inc.
 
JogAmp
Copyright 2010 JogAmp Community. All rights reserved.
 
JOGL Portions Sun Microsystems
Copyright © 2003-2009 Sun Microsystems, Inc. All Rights Reserved.
 
JOGL Portions Silicon Graphics
Copyright © 1991-2000 Silicon Graphics, Inc.
 
Light Weight Java Gaming Library Project (LWJGL)
Copyright © 2002-2004 LWJGL Project All rights reserved.
 
Tile Rendering Library - Brain Paul 
Copyright © 1997-2005 Brian Paul. All Rights Reserved.
 
OpenKODE, EGL, OpenGL , OpenGL ES1 & ES2
Copyright © 2007-2010 The Khronos Group Inc.
 
Cg
Copyright © 2002, NVIDIA Corporation
 
Typecast - David Schweinsberg 
Copyright © 1999-2003 The Apache Software Foundation. All rights reserved.
 
PNGJ - Herman J. Gonzalez and Shawn Hartsock
Copyright © 2004 The Apache Software Foundation. All rights reserved.
 
Apache Harmony - Open Source Java SE
Copyright © 2006, 2010 The Apache Software Foundation.
 
Guava
Copyright © 2010 The Guava Authors
 
GlueGen Portions
Copyright © 2010 JogAmp Community. All rights reserved.
 
GlueGen Portions - Sun Microsystems
Copyright © 2003-2005 Sun Microsystems, Inc. All Rights Reserved.
 
XStream
Copyright © 2003-2006, Joe Walnes
Copyright © 2006-2009, 2011 XStream Committers
All rights reserved.
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam
 

Disclaimers

No Warranty: THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY KIND,
EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
THAT THE SUBJECT SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY
WARRANTY THAT THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE. THIS AGREEMENT
DOES NOT, IN ANY MANNER, CONSTITUTE AN ENDORSEMENT BY GOVERNMENT AGENCY OR ANY
PRIOR RECIPIENT OF ANY RESULTS, RESULTING DESIGNS, HARDWARE, SOFTWARE PRODUCTS OR
ANY OTHER APPLICATIONS RESULTING FROM USE OF THE SUBJECT SOFTWARE.  FURTHER,
GOVERNMENT AGENCY DISCLAIMS ALL WARRANTIES AND LIABILITIES REGARDING THIRD-PARTY
SOFTWARE, IF PRESENT IN THE ORIGINAL SOFTWARE, AND DISTRIBUTES IT "AS IS."

Waiver and Indemnity:  RECIPIENT AGREES TO WAIVE ANY AND ALL CLAIMS AGAINST THE UNITED
STATES GOVERNMENT, ITS CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR
RECIPIENT.  IF RECIPIENT'S USE OF THE SUBJECT SOFTWARE RESULTS IN ANY LIABILITIES,
DEMANDS, DAMAGES, EXPENSES OR LOSSES ARISING FROM SUCH USE, INCLUDING ANY DAMAGES
FROM PRODUCTS BASED ON, OR RESULTING FROM, RECIPIENT'S USE OF THE SUBJECT SOFTWARE,
RECIPIENT SHALL INDEMNIFY AND HOLD HARMLESS THE UNITED STATES GOVERNMENT, ITS
CONTRACTORS AND SUBCONTRACTORS, AS WELL AS ANY PRIOR RECIPIENT, TO THE EXTENT
PERMITTED BY LAW.  RECIPIENT'S SOLE REMEDY FOR ANY SUCH MATTER SHALL BE THE IMMEDIATE,
UNILATERAL TERMINATION OF THIS AGREEMENT.
