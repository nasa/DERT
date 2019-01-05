/**

DERT is a viewer for digital terrain models created from data collected during NASA missions.

DERT is Released in under the NASA Open Source Agreement (NOSA) found in the “LICENSE” folder where you
downloaded DERT.

DERT includes 3rd Party software. The complete copyright notice listing for DERT is:

Copyright © 2015 United States Government as represented by the Administrator of the National Aeronautics and
Space Administration.  No copyright is claimed in the United States under Title 17, U.S.Code. All Other Rights
Reserved.

Desktop Exploration of Remote Terrain (DERT) could not have been written without the aid of a number of free,
open source libraries. These libraries and their notices are listed below. Find the complete third party license
listings in the separate “DERT Third Party Licenses” pdf document found where you downloaded DERT in the
LICENSE folder.
 
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
 
Tile Rendering Library - Brian Paul 
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
 
SPICE
Copyright © 2003, California Institute of Technology.
U.S. Government sponsorship acknowledged.
 
LibTIFF
Copyright © 1988-1997 Sam Leffler
Copyright © 1991-1997 Silicon Graphics, Inc.
 
PROJ.4
Copyright © 2000, Frank Warmerdam

LibJPEG - Independent JPEG Group
Copyright © 1991-2018, Thomas G. Lane, Guido Vollbeding
 

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

**/

package gov.nasa.arc.dert.test;

import gov.nasa.arc.dert.landscape.srs.ProjectionInfo;
import gov.nasa.arc.dert.layerfactory.LayerFactory;
import gov.nasa.arc.dert.raster.geotiff.GTIF;
import gov.nasa.arc.dert.raster.geotiff.GeoKey;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Properties;

public class TestDemFactory {
	
	private int size, halfSize;
	private ByteBuffer bBuf;
	private float minValue, maxValue;
	
	public TestDemFactory(int size) {
		this.size = size;
		bBuf = ByteBuffer.allocate(size*size*4);
		bBuf.order(ByteOrder.nativeOrder());
		minValue = Float.MAX_VALUE;
		maxValue = -Float.MAX_VALUE;
		halfSize = size/2;
		for (int r=halfSize; r>-halfSize; --r) {
			for (int c=-halfSize; c<halfSize; ++c) {
				float z = getZ(c, r);
				bBuf.putFloat(z);
				if (!Float.isNaN(z)) {
					minValue = Math.min(minValue, z);
					maxValue = Math.max(maxValue, z);
				}
			}
		}
		bBuf.rewind();
		System.out.println("TestDemFactory min elev="+minValue+" max elev="+maxValue);
	}
	
	public float getZ(double c, double r) {
		double cc = 2*Math.PI*c/(double)halfSize;
		double rr = 2*Math.PI*r/(double)halfSize;
		double d = Math.sqrt(cc*cc+rr*rr);
		float z = (float)Math.sin(d)*100f;	
		return(z);
	}
	
	public boolean createDem(String filename) {
		try {
			Properties properties = new Properties();
			GTIF gtif = new GTIF(filename, properties);
			gtif.open("w");
			ProjectionInfo projInfo = ProjectionInfo.createDefault(size, size, 1);	
			projInfo.pcsCode = GeoKey.Code_UserDefined;
			projInfo.gcsCode = GeoKey.Code_GCS_WGS_84;
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_IMAGEWIDTH, size);
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_IMAGELENGTH, size);
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_PLANARCONFIG, GTIF.PLANARCONFIG_CONTIG);
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_ROWSPERSTRIP, 1);
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_BITSPERSAMPLE, 32);
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_SAMPLEFORMAT, GTIF.SAMPLEFORMAT_IEEEFP);
			gtif.setTIFFFieldInt(GTIF.TIFFTAG_SAMPLESPERPIXEL, 1);
			gtif.setTIFFFieldDouble(GTIF.TIFFTAG_SMINSAMPLEVALUE, minValue);
			gtif.setTIFFFieldDouble(GTIF.TIFFTAG_SMAXSAMPLEVALUE, maxValue);
			gtif.setProjectionInfo(projInfo);
			
			byte[] bytes = new byte[size*4];
			ByteBuffer outBuf = ByteBuffer.allocateDirect(size*4);
			for (int r=0; r<size; ++r) {
				bBuf.position(r*size*4);
				bBuf.get(bytes);
				outBuf.put(bytes);
				outBuf.rewind();
				long n = gtif.writeStrip(r, outBuf, size*4);
				if (n != size*4) {
					System.err.println("TestDemFactory.writeDem invalid return from writeStrip. Bytes written = "+n);
					break;
				}
			}

			gtif.close();
			return(true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return(false);
	}
	
	public boolean createLandscape(String testLoc) {
		
		System.err.println("Create "+testLoc);
		File testDir = new File(testLoc);
		if (!testDir.exists()) {
			if (!testDir.mkdir())
				return(false);
			File dertDir = new File(testLoc, "dert");
			if (!dertDir.mkdir())
				return(false);
		}
		
		System.err.println("Create DEM");
		if (!createDem(testLoc+"/testdem.tif"))
			return(false);
		
		System.err.println("Create layer");
		String[] args = new String[] {"-landscape="+testLoc, "-file="+testLoc+"/testdem.tif", "-tilesize=128", "-type=elevation"};
		LayerFactory lf = new LayerFactory(args);
		if (!lf.createLayer())
			return(false);
		
		System.err.println("Created layer successfully.\n");
		return(true);
	}
	
	public static void main(String[] arg) {
		TestDemFactory factory = new TestDemFactory(2048);
		factory.createDem("/tmp/testdem.tif");
	}

}
