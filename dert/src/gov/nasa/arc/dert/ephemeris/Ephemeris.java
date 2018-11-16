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

package gov.nasa.arc.dert.ephemeris;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.view.Console;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import spice.basic.BodyName;
import spice.basic.CSPICE;
import spice.basic.KernelDatabase;

/**
 * Provides methods for locating the position of the Sun relative to the
 * landscape using the JNISpice library.
 *
 */
public class Ephemeris {

	// Singleton instance
	protected static Ephemeris instance;

	protected final double[] zAxis = { 0, 0, 1 };
	protected SimpleDateFormat utcDateFormat;

	// Load the native libraries for JNISpice.
	static {
		if (Dert.isMac) {
			loadNativeLibrary("/libJNISpice.jnilib");
		} else if (Dert.isLinux) {
			loadNativeLibrary("/libJNISpice.so");
		}
	}

	protected static void loadNativeLibrary(String libName) {
		try {

			// copy the library from the jar file to /tmp
			final InputStream in = Ephemeris.class.getResource(libName).openStream();
			int p0 = libName.lastIndexOf('/');
			int p1 = libName.lastIndexOf('.');
			String tempName = libName.substring(p0, p1) + '_' + System.currentTimeMillis();
			final File libFile = File.createTempFile(tempName, ".jni");
			libFile.deleteOnExit();
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(libFile));
			int len = 0;
			byte[] buffer = new byte[32768];
			while ((len = in.read(buffer)) > -1) {
				out.write(buffer, 0, len);
			}
			out.close();
			in.close();

			// load the library into memory
			System.load(libFile.getAbsolutePath());
			libFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the singleton
	 * 
	 * @param path
	 * @param properties
	 */
	public static void createInstance(String path, Properties properties) {
		instance = new Ephemeris(path, properties);
	}

	/**
	 * Get the singleton
	 * 
	 * @return
	 */
	public static Ephemeris getInstance() {
		return (instance);
	}

	/**
	 * Constructor
	 * 
	 * @param path
	 * @param properties
	 */
	protected Ephemeris(String path, Properties properties) {
		initialize(path, properties);
	}

	protected void initialize(String path, Properties properties) {
		utcDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		File file = new File(path);
		if (!file.exists()) {
			throw new IllegalStateException("Unable to initialize ephemeris: " + path + " not found.");
		} else {
			try {
				// load SPICE kernels
				String[] fileName = getFileList(properties);
				for (int i = 0; i < fileName.length; ++i) {
					Console.println("Loading SPICE kernel "+fileName[i]);
					KernelDatabase.load(path + "kernels/" + fileName[i]);
				}
			} catch (Exception e) {
				Console.println("Unable to load SPICE kernels, see log.");
				e.printStackTrace();
				return;
			}
		}
	}

	private String[] getFileList(Properties properties) {
		Object[] key = properties.keySet().toArray();
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < key.length; ++i) {
			String name = (String)key[i];
			if (name.startsWith("SpiceKernel.")) {
				String str = properties.getProperty(name);
				if ((str != null) && !str.isEmpty())
					list.add(name);
			}
		}
		Collections.sort(list);
		String[] files = new String[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			String name = list.get(i);
			files[i] = properties.getProperty(name);
		}
		return (files);
	}

	/**
	 * Convert Unix time to the UTC format recognized by SPICE.
	 * 
	 * @param time
	 * @return
	 */
	public String time2UtcStr(long time) {
		Date date = new Date(time);
		return (utcDateFormat.format(date));
	}

	/**
	 * Get the vector to a target body from a point on an observer body.
	 * 
	 * @param observer
	 *            the name for the observer body (Mars, for example)
	 * @param target
	 *            the name for the target (Sun, for example)
	 * @param time
	 *            the UTC epoch at which the vector is to be computed
	 * @param lat
	 *            the latitude on the observer surface
	 * @param lon
	 *            the longitude on the observer surface
	 * @return the vector
	 */
	public static double[] getTargetVector(String observer, String target, String time, double lon, double lat, double alt) {
		// SPICE is not thread safe. Allow only one thread to use it at a time
		synchronized (instance) {
			double[] vector = instance.getTargetVectorAtLonLatAlt(observer, target, time, lon, lat, alt);
			return (vector);
		}
	}

	protected double[] getTargetVectorAtLonLatAlt(String observer, String target, String time, double lon, double lat, double alt) {
		observer = observer.toUpperCase();
		target = target.toUpperCase();		
		alt /= 1000;
		
//		System.err.println("Ephemeris.getTargetVectorAtLonLatAlt "+observer+" "+target+" "+time+" "+lon+" "+lat+" "+alt);
		
		try {
			// Convert epoch to ephemeris time
			double et = CSPICE.str2et(time);

			// Compute target state in observer body-fixed frame
			double[] trgPt = new double[3];
			double[] lt = new double[1];
			String obsName = observer;
//			if (obsName.equals("MARS"))
//				obsName += " BARYCENTER";
			CSPICE.spkpos(target, et, "IAU_"+observer, "LT+S", obsName, trgPt, lt);
			
			// Get the observer body
			BodyName obsBody = new BodyName(observer);

			// Get the observer radii
			double[] obsRadii = CSPICE.bodvcd(obsBody.getIDCode(), "RADII");

			// Compute surface point on observer body in body-fixed frame			
			double[] obsSurfacePt = CSPICE.georec(Math.toRadians(lon), Math.toRadians(lat), alt, obsRadii[0], (obsRadii[0]-obsRadii[2])/obsRadii[0]);

			// Compute surface normal at surface point
			double[] obsSurfaceNm = CSPICE.surfnm(obsRadii[0], obsRadii[1], obsRadii[2], obsSurfacePt);
			obsSurfaceNm = CSPICE.vhat(obsSurfaceNm);
			
			trgPt[0] -= obsSurfacePt[0];
			trgPt[1] -= obsSurfacePt[1];
			trgPt[2] -= obsSurfacePt[2];

			// Compute the matrix to tranform the body-fixed frame to the surface frame
			// make the Y axis Lon = 0
			double[][] obsMatrix = CSPICE.twovec(obsSurfaceNm, 3, zAxis, 2);
			double[] vec = CSPICE.mxv(obsMatrix, trgPt);
			vec = CSPICE.vhat(vec);
			
			double[] result = new double[6];
			result[0] = vec[0];
			result[1] = vec[1];
			result[2] = vec[2];
					
			// convert to radius, lon, and lat
			vec = CSPICE.reclat(trgPt);
			result[3] = vec[0];
			result[4] = vec[1];
			result[5] = vec[2];

			return (result);
		} catch (Exception e) {
			System.out.println("Unable to get target vector, see log.");
			e.printStackTrace();
			return (null);
		}
	}

}
