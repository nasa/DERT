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

package gov.nasa.arc.dert.test;

import gov.nasa.arc.dert.Dert;
import gov.nasa.arc.dert.action.edit.BackgroundColorDialog;
import gov.nasa.arc.dert.landscape.Landscape;
import gov.nasa.arc.dert.landscape.layer.DerivativeLayer;
import gov.nasa.arc.dert.landscape.quadtree.QuadTree;
import gov.nasa.arc.dert.landscape.quadtree.QuadTreeCache;
import gov.nasa.arc.dert.lighting.Lighting;
import gov.nasa.arc.dert.scene.featureset.FeatureSet;
import gov.nasa.arc.dert.scene.landmark.Figure;
import gov.nasa.arc.dert.scene.landmark.ImageBoard;
import gov.nasa.arc.dert.scene.landmark.Placemark;
import gov.nasa.arc.dert.scene.tool.CartesianGrid;
import gov.nasa.arc.dert.scene.tool.Path;
import gov.nasa.arc.dert.scene.tool.Plane;
import gov.nasa.arc.dert.scene.tool.Profile;
import gov.nasa.arc.dert.scene.tool.RadialGrid;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCamera;
import gov.nasa.arc.dert.scene.tool.fieldcamera.FieldCameraInfoManager;
import gov.nasa.arc.dert.scenegraph.text.BitmapFont;
import gov.nasa.arc.dert.scenegraph.text.BitmapText;
import gov.nasa.arc.dert.state.ConfigurationManager;
import gov.nasa.arc.dert.state.StateFactory;
import gov.nasa.arc.dert.util.StringUtil;
import gov.nasa.arc.dert.view.Console;
import gov.nasa.arc.dert.viewpoint.ViewpointController;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Main class for running unit tests of Desktop Exploration of Remote Terrain.
 *
 */
public class DertTest {

	public static final boolean isMac, isLinux, is64;
	public static final String DERT_HOME = "dertstash";
	public static final String testLoc = "/tmp/derttest";

	// Paths to DERT executable and user current working directory
	private static String path, userPath = System.getProperty("user.home");

	// Application properties
	private static Properties dertProperties;

	// Operating system flags
	static {
		String os = System.getProperty("os.name").toLowerCase();
		isMac = os.contains("mac");
		isLinux = os.contains("lin");
		String arch = System.getProperty("os.arch").toLowerCase();
		is64 = arch.contains("64");
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Start DERT.
		DertTest test = new DertTest(args);
		test.runTests(args);
	}

	/**
	 * Constructor
	 * 
	 * @param args
	 */
	public DertTest(String[] args) {

//		// Find the path to the executable.
//		String pathStr = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
//		if (pathStr.toLowerCase().endsWith(".jar")) {
//			int p = pathStr.lastIndexOf('/');
//			pathStr = pathStr.substring(0, p + 1);
//		} else {
//			pathStr += "../";
//		}
//		try {
//			pathStr = new File(pathStr).getCanonicalPath();
//			if (!pathStr.endsWith("/")) {
//				pathStr = pathStr + "/";
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
//
//		// Initialize DERT.
//		initialize(pathStr, args);
	}

	/**
	 * Initialize the DERT session.
	 * 
	 * @param pathStr
	 *            path to the DERT executable
	 * @param args
	 *            the command line arguments
	 */
	public static void initialize(String pathStr, String[] args) {

		// Initialize global path related fields.
		path = pathStr;

		// Create the user's DERT_HOME directory if it doesn't exist.
		File file = new File(userPath, DERT_HOME);
		try {
			if (!file.exists()) {
				file.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Load default and session properties.
		installDertProperties();

		// Get current local date.
		String dateStr = new Date().toString();

		// Set time zone to UTC.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		System.err.println("DERT Test");
		System.err.println("Date: " + dateStr);
		System.err.println("DERT Version: " + dertProperties.getProperty("Dert.Version"));
		System.err.println("Path to executable: " + path);
		System.err.println("Path to user files: " + userPath);
		System.err.println("OS Name: " + System.getProperty("os.name"));
		System.err.println("OS Version: " + System.getProperty("os.version"));
		System.err.println("OS Arch: " + System.getProperty("os.arch"));
		System.err.println("Java Version: " + System.getProperty("java.version"));
		System.err.println();
		
		BitmapText.DEFAULT_FONT = new BitmapFont(StringUtil.getStringValue(dertProperties, "RasterText.Font", "Courier New", false), Font.BOLD, StringUtil.getIntegerValue(dertProperties, "RasterText.FontSize", true, 24, false));
		ConfigurationManager.createInstance(dertProperties, new StateFactory());
		FieldCameraInfoManager.createInstance(path);
		Console.createInstance();
	}

	private static void installDertProperties() {
		dertProperties = new Properties();
		try {
			// Load properties from file with executable.
			File file = new File(path, "dert.properties");
			dertProperties.load(new FileInputStream(file));
			// Add properties from user's dertstash directory.
			file = new File(userPath, DERT_HOME);
			file = new File(file, "properties");
			if (file.exists()) {
				dertProperties.load(new FileInputStream(file));
			}

//			RasterText.setFont(StringUtil.getIntegerValue(dertProperties, "RasterText.Font", true, 18, false));
			Lighting.loadProperties(dertProperties);
			QuadTreeCache.MAX_CACHE_MEMORY = StringUtil.getLongValue(dertProperties, "QuadTree.MaxCacheSize", true,
				QuadTreeCache.MAX_CACHE_MEMORY, false);
			DerivativeLayer.defaultColorMapName = dertProperties.getProperty("ColorMap.Default", "default0");
			QuadTree.CELL_SIZE = StringUtil.getIntegerValue(dertProperties, "MeshCellSize", true, QuadTree.CELL_SIZE,
				false);
			ViewpointController.mouseScrollDirection = StringUtil.getIntegerValue(dertProperties,
				"MouseScrollDirection", false, -1, false);
			BackgroundColorDialog.setPredefinedBackgroundColors(dertProperties);
			Landscape.MAX_LEVELS = StringUtil.getIntegerValue(dertProperties, "Landscape.MaximumLevels", true,
					Landscape.MAX_LEVELS, false);

			// Get map element preferences.
			Placemark.setDefaultsFromProperties(dertProperties);
			Figure.setDefaultsFromProperties(dertProperties);
			ImageBoard.setDefaultsFromProperties(dertProperties);
			FieldCamera.setDefaultsFromProperties(dertProperties);
			Path.setDefaultsFromProperties(dertProperties);
			Plane.setDefaultsFromProperties(dertProperties);
			CartesianGrid.setDefaultsFromProperties(dertProperties);
			RadialGrid.setDefaultsFromProperties(dertProperties);
			Profile.setDefaultsFromProperties(dertProperties);
			FeatureSet.setDefaultsFromProperties(dertProperties);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void runTests(String[] args) {
		TestDemFactory demFactory = new TestDemFactory(1024);
		if (!demFactory.createLandscape(testLoc))
			System.exit(1);
		
		new Dert(new String[] {"-debug", "-config", testLoc});
		
		// Wait for drawing surface to realize.
		while (Landscape.getInstance() == null) {
			System.err.println("DertTest wait");
			try { Thread.sleep(1000); } catch (Exception e) {}
		}
		
		System.err.println("\nLandscape Tests\n");
		LandscapeTest lt = new LandscapeTest();
		if (!lt.testLandscape(demFactory))
			System.exit(2);
		
		System.err.println("\nMath Tests\n");
		MathUtilTest mut = new MathUtilTest();
		if (!mut.testMathUtil())
			System.exit(3);
		
		System.err.println("\nPersistence Tests\n");
		PersistenceTest pt = new PersistenceTest();
		if (!pt.testPersistence(testLoc))
			System.exit(4);
		
		System.err.println("\nAll tests passed.");
		
		System.exit(0);
	}
}
