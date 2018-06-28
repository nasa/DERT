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

package gov.nasa.arc.dert.scene.tool.fieldcamera;

import gov.nasa.arc.dert.util.FileHelper;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

/**
 * Manages the FieldCameraInfo objects.
 *
 */
public class FieldCameraInfoManager {

	private static FieldCameraInfoManager INSTANCE;

	private static String fieldCameraDirectory = "camera";
	private String location, configLocation;
	private HashMap<String, String> infoMap;

	public static void createInstance(String loc) {
		INSTANCE = new FieldCameraInfoManager(loc);
	}

	public static FieldCameraInfoManager getInstance() {
		return (INSTANCE);
	}

	protected FieldCameraInfoManager(String location) {
		this.location = location;
	}

	/**
	 * Set the location of the directory containing the camera definitions
	 * 
	 * @param loc
	 */
	public void setConfigLocation(String loc) {
		configLocation = loc;
		getFieldCameraNames();
	}

	/**
	 * Get the names of the camera definition files
	 * 
	 * @return
	 */
	public String[] getFieldCameraNames() {
		infoMap = new HashMap<String, String>();
		File fieldCameraDir = new File(location, fieldCameraDirectory);
		String[] fieldCameraFiles = fieldCameraDir.list();
		if (fieldCameraFiles == null) {
			fieldCameraFiles = new String[0];
		}
		for (int i = 0; i < fieldCameraFiles.length; ++i) {
			String instPath = new File(fieldCameraDir.getAbsolutePath(), fieldCameraFiles[i]).getAbsolutePath();
			if (!instPath.endsWith(".properties")) {
				continue;
			}
			infoMap.put(StringUtil.getLabelFromFilePath(instPath), instPath);
		}
		fieldCameraDir = new File(configLocation, fieldCameraDirectory);
		fieldCameraFiles = fieldCameraDir.list();
		if (fieldCameraFiles == null) {
			fieldCameraFiles = new String[0];
		}
		for (int i = 0; i < fieldCameraFiles.length; ++i) {
			String instPath = new File(fieldCameraDir.getAbsolutePath(), fieldCameraFiles[i]).getAbsolutePath();
			if (!instPath.endsWith(".properties")) {
				continue;
			}
			infoMap.put(StringUtil.getLabelFromFilePath(instPath), instPath);
		}

		String[] names = new String[infoMap.size()];
		infoMap.keySet().toArray(names);
		Arrays.sort(names);
		return (names);
	}

	/**
	 * Get a camera info object by name
	 * 
	 * @param name
	 * @return
	 */
	public FieldCameraInfo getFieldCameraInfo(String name) {
		try {
			String filename = infoMap.get(name);
			Properties properties = new Properties();
			properties.load(new FileInputStream(filename));
			return (new FieldCameraInfo(name, properties));
		} catch (Exception e) {
			e.printStackTrace();
			Console.println("Error loading camera " + name + ".  See log.");
			return (null);
		}
	}

	/**
	 * Copy all of the camera definition files
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public void copyFieldCameras(String src, String dest) throws IOException {
		File inDir = new File(src, fieldCameraDirectory);
		File outDir = new File(dest, fieldCameraDirectory);
		if (!outDir.exists()) {
			outDir.mkdir();
		}
		String[] files = inDir.list();
		if (files == null) {
			files = new String[0];
		}
		for (int i = 0; i < files.length; ++i) {
			if (files[i].endsWith(".properties")) {
				File inFile = new File(inDir, files[i]);
				File outFile = new File(outDir, files[i]);
				FileHelper.copyFile(inFile, outFile);
			}
		}
	}

}
